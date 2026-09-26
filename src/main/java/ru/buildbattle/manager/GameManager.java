package ru.buildbattle.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.buildbattle.BuildBattlePlugin;
import ru.buildbattle.model.*;
import ru.buildbattle.util.Msg;

import java.util.*;

public class GameManager {

    private final BuildBattlePlugin plugin;

    private final Map<GameMode, List<UUID>> queues = new EnumMap<>(GameMode.class);
    private final Map<GameMode, Integer> countdowns = new EnumMap<>(GameMode.class);

    private final Map<String, GameSession> sessions = new HashMap<>();
    private final Map<UUID, String> playerSession = new HashMap<>();

    private int sessionCounter = 0;

    public GameManager(BuildBattlePlugin plugin) {
        this.plugin = plugin;
        for (GameMode m : GameMode.values()) {
            queues.put(m, new ArrayList<>());
            countdowns.put(m, -1);
        }
    }

    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                tickQueues();
                tickSessions();
                tickBorders();
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    // ================= QUEUE =================

    public void joinQueue(Player player, GameMode mode) {
        if (isInGame(player.getUniqueId())) {
            Msg.send(player, "&cВы уже находитесь в игре!");
            return;
        }
        leaveAllQueues(player.getUniqueId());
        queues.get(mode).add(player.getUniqueId());
        Msg.send(player, "&aВы встали в очередь на режим: &f" + mode.getDisplayName()
                + " &7(" + queues.get(mode).size() + " игроков)");
        broadcastQueue(mode, "&e" + player.getName() + " &7присоединился к очереди &f" + mode.getDisplayName());
    }

    public void leaveAllQueues(UUID uuid) {
        for (List<UUID> q : queues.values()) q.remove(uuid);
    }

    public int getQueueSize(GameMode mode) {
        return queues.get(mode).size();
    }

    private void broadcastQueue(GameMode mode, String msg) {
        for (UUID u : queues.get(mode)) {
            Player p = Bukkit.getPlayer(u);
            if (p != null) Msg.send(p, msg);
        }
    }

    private void tickQueues() {
        for (GameMode mode : GameMode.values()) {
            List<UUID> queue = queues.get(mode);
            int teamSize = mode.getTeamSize();
            int minTeams = plugin.getConfigManager().getMinTeams();
            int readyTeams = queue.size() / teamSize;

            int cd = countdowns.get(mode);
            if (readyTeams >= minTeams) {
                if (cd < 0) {
                    countdowns.put(mode, plugin.getConfigManager().getForceStartSeconds());
                } else if (cd == 0) {
                    attemptStart(mode);
                    countdowns.put(mode, -1);
                } else {
                    if (cd == 15 || cd == 10 || cd <= 5) {
                        broadcastQueue(mode, "&aИгра &f" + mode.getDisplayName() + " &aначнётся через &f" + cd + " &aсек!");
                    }
                    countdowns.put(mode, cd - 1);
                }
            } else {
                countdowns.put(mode, -1);
            }
        }
    }

    private void attemptStart(GameMode mode) {
        List<UUID> queue = queues.get(mode);
        int teamSize = mode.getTeamSize();
        int availableTeams = queue.size() / teamSize;
        if (availableTeams < plugin.getConfigManager().getMinTeams()) return;

        List<Arena> candidates = plugin.getArenaManager().getArenasForMode(mode);
        Arena chosen = null;
        for (Arena a : candidates) {
            if (a.getPlotCount() >= availableTeams) {
                chosen = a;
                break;
            }
        }
        if (chosen == null) {
            for (Arena a : candidates) {
                if (chosen == null || a.getPlotCount() > chosen.getPlotCount()) chosen = a;
            }
        }
        if (chosen == null) {
            broadcastQueue(mode, "&cНет свободной арены для запуска игры. Попробуйте позже.");
            return;
        }

        int teamsToUse = Math.min(availableTeams, chosen.getPlotCount());
        int playersToUse = teamsToUse * teamSize;

        List<UUID> selected = new ArrayList<>(queue.subList(0, playersToUse));
        queue.subList(0, playersToUse).clear();

        startGame(chosen, mode, selected, teamsToUse);
    }

    private void startGame(Arena arena, GameMode mode, List<UUID> players, int teamsCount) {
        String sessionId = "bb-" + (++sessionCounter);
        String theme = plugin.getConfigManager().randomTheme();
        GameSession session = new GameSession(sessionId, arena, mode, theme);

        List<Plot> plots = new ArrayList<>(arena.getPlots());
        Collections.shuffle(plots);

        int teamSize = mode.getTeamSize();
        for (int i = 0; i < teamsCount; i++) {
            Team team = new Team();
            for (int j = 0; j < teamSize; j++) {
                UUID uuid = players.get(i * teamSize + j);
                team.getMembers().add(uuid);
                playerSession.put(uuid, sessionId);
            }
            team.setPlot(plots.get(i));
            session.getTeams().add(team);
        }

        session.setPhase(GamePhase.BUILDING);
        session.setBuildSecondsLeft(plugin.getConfigManager().getBuildTimeSeconds());
        sessions.put(sessionId, session);

        var world = Bukkit.getWorld(arena.getWorldName());

        for (Team team : session.getTeams()) {
            team.getPlot().clearBuildable(world);
            for (UUID uuid : team.getMembers()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p == null) continue;
                p.getInventory().clear();
                p.setGameMode(org.bukkit.GameMode.SURVIVAL);
                p.teleport(team.getPlot().getSpawnLocation(world));
                p.getInventory().setItem(4, plugin.getItemFactory().createBuilderTools());
                p.getInventory().setItem(8, plugin.getItemFactory().createMenuCompass());
                Msg.title(p, "&6BuildBattle", "&fТема: &e" + theme);
                Msg.send(p, "&aИгра началась! Тема постройки: &e" + theme
                        + " &7(" + mode.getDisplayName() + ", арена: " + arena.getId() + ")");
                plugin.getScoreboardManager().update(p, session);
            }
        }
    }

    // ================= TICK SESSIONS =================

    private void tickSessions() {
        List<GameSession> toEnd = new ArrayList<>();
        for (GameSession session : sessions.values()) {
            switch (session.getPhase()) {
                case BUILDING -> tickBuilding(session);
                case VOTING -> tickVoting(session, toEnd);
                default -> {}
            }
        }
    }

    private void tickBuilding(GameSession session) {
        int left = session.getBuildSecondsLeft() - 1;
        session.setBuildSecondsLeft(left);

        for (UUID uuid : session.getAllPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            plugin.getScoreboardManager().update(p, session);
            if (left == 60 || left == 30 || left == 10 || (left <= 5 && left > 0)) {
                Msg.actionBar(p, "&eДо конца постройки: &f" + left + " сек");
            }
            if (left == 30 || left == 10) {
                Msg.title(p, "&e" + left + " секунд", "&7до конца постройки");
            }
        }

        if (left <= 0) {
            startVoting(session);
        }
    }

    private void startVoting(GameSession session) {
        session.setPhase(GamePhase.VOTING);
        session.setCurrentVotingTeamIndex(-1);
        advanceVoting(session);
    }

    private void tickVoting(GameSession session, List<GameSession> toEnd) {
        int left = session.getVotingSecondsLeft() - 1;
        session.setVotingSecondsLeft(left);

        for (UUID uuid : session.getAllPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            plugin.getScoreboardManager().update(p, session);
            if (left <= 5 && left > 0) Msg.actionBar(p, "&eСледующая постройка через: &f" + left);
        }

        if (left <= 0) {
            advanceVoting(session);
        }
    }

    private void advanceVoting(GameSession session) {
        int next = session.getCurrentVotingTeamIndex() + 1;
        session.setCurrentVotingTeamIndex(next);

        if (next >= session.getTeams().size()) {
            endGame(session);
            return;
        }

        Team target = session.getTeams().get(next);
        session.setVotingSecondsLeft(plugin.getConfigManager().getVotingTimePerPlotSeconds());
        var world = Bukkit.getWorld(session.getArena().getWorldName());
        Location viewSpot = target.getPlot().getSpawnLocation(world);

        for (UUID uuid : session.getAllPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            p.teleport(viewSpot);
            p.getInventory().clear();
            boolean isOwner = target.isMember(uuid);
            if (!isOwner) {
                p.getInventory().setItem(4, plugin.getItemFactory().createRatingItem());
                Msg.title(p, "&6Оцените постройку", "&7Тема: &e" + session.getTheme());
            } else {
                Msg.title(p, "&aЭто ваша постройка", "&7Ожидайте оценок...");
            }
            plugin.getScoreboardManager().update(p, session);
        }
    }

    public void submitVote(Player voter, GameSession session, Rating rating) {
        int index = session.getCurrentVotingTeamIndex();
        if (index < 0 || index >= session.getTeams().size()) return;
        Team target = session.getTeams().get(index);
        if (target.isMember(voter.getUniqueId())) {
            Msg.send(voter, "&cВы не можете оценивать свою постройку!");
            return;
        }
        session.registerVote(index, voter.getUniqueId(), rating.getPoints());
        Msg.send(voter, "&aВы поставили оценку: " + rating.getDisplayName() + " &7(" + rating.getPoints() + " очков)");
    }

    private void endGame(GameSession session) {
        session.setPhase(GamePhase.ENDING);
        List<Team> ranking = session.getRanking();

        StringBuilder resultsMsg = new StringBuilder();
        resultsMsg.append("&6&l=== Результаты BuildBattle ===\n");
        for (int i = 0; i < ranking.size(); i++) {
            Team t = ranking.get(i);
            List<String> names = new ArrayList<>();
            for (UUID u : t.getMembers()) {
                var op = Bukkit.getOfflinePlayer(u);
                names.add(op.getName() == null ? "???" : op.getName());
            }
            resultsMsg.append("&e#").append(i + 1).append(" &f").append(String.join(", ", names))
                    .append(" &7- &b").append(t.getTotalScore()).append(" очков\n");
        }

        Team winner = ranking.isEmpty() ? null : ranking.get(0);
        var world = Bukkit.getWorld(session.getArena().getWorldName());

        for (UUID uuid : session.getAllPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            Team myTeam = session.getTeamOf(uuid);
            boolean won = winner != null && myTeam == winner;
            plugin.getStatsManager().addGameResult(uuid, won, myTeam == null ? 0 : myTeam.getTotalScore());

            if (p != null) {
                for (String line : resultsMsg.toString().split("\n")) {
                    p.sendMessage(Msg.c(line));
                }
                if (won) {
                    Msg.title(p, "&6&lПОБЕДА!", "&fВы набрали больше всего очков");
                } else {
                    Msg.title(p, "&7Игра окончена", "&fПосмотрите результаты в чате");
                }
                p.getInventory().clear();
                p.teleport(plugin.getLobbyLocation());
                p.getInventory().setItem(8, plugin.getItemFactory().createMenuCompass());
                plugin.getScoreboardManager().clear(p);
            }
            playerSession.remove(uuid);
        }

        for (Team t : session.getTeams()) {
            t.getPlot().clearBuildable(world);
        }

        plugin.getStatsManager().save();
        sessions.remove(session.getSessionId());
    }

    // ================= EXTRA FEATURE: границы частицами =================

    private void tickBorders() {
        for (GameSession session : sessions.values()) {
            if (session.getPhase() != GamePhase.BUILDING) continue;
            var world = Bukkit.getWorld(session.getArena().getWorldName());
            if (world == null) continue;
            for (Team team : session.getTeams()) {
                for (UUID uuid : team.getMembers()) {
                    if (!session.isBordersEnabled(uuid)) continue;
                    Player p = Bukkit.getPlayer(uuid);
                    if (p == null) continue;
                    outlinePlot(p, team.getPlot());
                }
            }
        }
    }

    private void outlinePlot(Player p, Plot plot) {
        int minX = plot.getInnerMinX(), maxX = plot.getInnerMaxX();
        int minZ = plot.getInnerMinZ(), maxZ = plot.getInnerMaxZ();
        int y = plot.getInnerMinY();
        double step = 1.0;
        for (double x = minX; x <= maxX + 1; x += step) {
            p.spawnParticle(org.bukkit.Particle.FLAME, x, y + 1, minZ, 1, 0, 0, 0, 0);
            p.spawnParticle(org.bukkit.Particle.FLAME, x, y + 1, maxZ + 1, 1, 0, 0, 0, 0);
        }
        for (double z = minZ; z <= maxZ + 1; z += step) {
            p.spawnParticle(org.bukkit.Particle.FLAME, minX, y + 1, z, 1, 0, 0, 0, 0);
            p.spawnParticle(org.bukkit.Particle.FLAME, maxX + 1, y + 1, z, 1, 0, 0, 0, 0);
        }
    }

    // ================= LOOKUP =================

    public boolean isInGame(UUID uuid) {
        return playerSession.containsKey(uuid);
    }

    public GameSession getSession(UUID uuid) {
        String id = playerSession.get(uuid);
        return id == null ? null : sessions.get(id);
    }

    public record PlotHit(GameSession session, Team team, Plot plot) {}

    public PlotHit findPlotAt(Location loc) {
        for (GameSession session : sessions.values()) {
            for (Team t : session.getTeams()) {
                if (t.getPlot().isInsideOuter(loc)) {
                    return new PlotHit(session, t, t.getPlot());
                }
            }
        }
        return null;
    }

    public void leaveGame(Player player) {
        leaveAllQueues(player.getUniqueId());
        GameSession session = getSession(player.getUniqueId());
        if (session != null) {
            playerSession.remove(player.getUniqueId());
            player.getInventory().clear();
            player.teleport(plugin.getLobbyLocation());
            player.getInventory().setItem(8, plugin.getItemFactory().createMenuCompass());
            plugin.getScoreboardManager().clear(player);
            Msg.send(player, "&cВы покинули игру.");
        }
    }
}

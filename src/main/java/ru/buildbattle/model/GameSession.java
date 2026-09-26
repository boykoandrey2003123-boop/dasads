package ru.buildbattle.model;

import java.util.*;

/**
 * Одна запущенная игра BuildBattle на конкретной арене.
 */
public class GameSession {

    private final String sessionId;
    private final Arena arena;
    private final GameMode mode;
    private final String theme;

    private final List<Team> teams = new ArrayList<>();
    private GamePhase phase = GamePhase.WAITING;

    private int buildSecondsLeft;
    private int votingSecondsLeft;
    private int currentVotingTeamIndex = -1;

    // блок, выбранный игроком для заливки (per player) - хранится тут для быстрого доступа
    private final Map<UUID, org.bukkit.Material> selectedFillBlock = new HashMap<>();
    // границы вкл/выкл (частицы) per player
    private final Set<UUID> bordersEnabled = new HashSet<>();
    // голоса: teamIndex -> voterUUID -> points, чтобы не дать проголосовать дважды
    private final Map<Integer, Map<UUID, Integer>> votes = new HashMap<>();

    public GameSession(String sessionId, Arena arena, GameMode mode, String theme) {
        this.sessionId = sessionId;
        this.arena = arena;
        this.mode = mode;
        this.theme = theme;
    }

    public String getSessionId() { return sessionId; }
    public Arena getArena() { return arena; }
    public GameMode getMode() { return mode; }
    public String getTheme() { return theme; }
    public List<Team> getTeams() { return teams; }
    public GamePhase getPhase() { return phase; }
    public void setPhase(GamePhase phase) { this.phase = phase; }
    public int getBuildSecondsLeft() { return buildSecondsLeft; }
    public void setBuildSecondsLeft(int v) { this.buildSecondsLeft = v; }
    public int getVotingSecondsLeft() { return votingSecondsLeft; }
    public void setVotingSecondsLeft(int v) { this.votingSecondsLeft = v; }
    public int getCurrentVotingTeamIndex() { return currentVotingTeamIndex; }
    public void setCurrentVotingTeamIndex(int i) { this.currentVotingTeamIndex = i; }

    public Team getCurrentVotingTeam() {
        if (currentVotingTeamIndex < 0 || currentVotingTeamIndex >= teams.size()) return null;
        return teams.get(currentVotingTeamIndex);
    }

    public Team getTeamOf(UUID uuid) {
        for (Team t : teams) if (t.isMember(uuid)) return t;
        return null;
    }

    public boolean hasVoted(int teamIndex, UUID voter) {
        return votes.getOrDefault(teamIndex, Collections.emptyMap()).containsKey(voter);
    }

    public void registerVote(int teamIndex, UUID voter, int points) {
        Map<UUID, Integer> map = votes.computeIfAbsent(teamIndex, k -> new HashMap<>());
        boolean isChange = map.containsKey(voter);
        map.put(voter, points);
        if (!isChange) {
            teams.get(teamIndex).addRating(points);
        } else {
            // пересчитать очки команды заново, т.к. игрок меняет голос
            recalcTeamScore(teamIndex);
        }
    }

    private void recalcTeamScore(int teamIndex) {
        Team t = teams.get(teamIndex);
        Map<UUID, Integer> map = votes.get(teamIndex);
        t.recalculate(map.values());
    }

    public org.bukkit.Material getSelectedFillBlock(UUID uuid) {
        return selectedFillBlock.getOrDefault(uuid, org.bukkit.Material.STONE);
    }

    public void setSelectedFillBlock(UUID uuid, org.bukkit.Material material) {
        selectedFillBlock.put(uuid, material);
    }

    public boolean isBordersEnabled(UUID uuid) {
        return bordersEnabled.contains(uuid);
    }

    public void toggleBorders(UUID uuid) {
        if (!bordersEnabled.add(uuid)) bordersEnabled.remove(uuid);
    }

    public List<UUID> getAllPlayers() {
        List<UUID> list = new ArrayList<>();
        for (Team t : teams) list.addAll(t.getMembers());
        return list;
    }

    public List<Team> getRanking() {
        List<Team> sorted = new ArrayList<>(teams);
        sorted.sort((a, b) -> Integer.compare(b.getTotalScore(), a.getTotalScore()));
        return sorted;
    }
}

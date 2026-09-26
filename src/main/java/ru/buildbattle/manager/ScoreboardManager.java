package ru.buildbattle.manager;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import ru.buildbattle.model.GameSession;
import ru.buildbattle.model.Team;
import ru.buildbattle.util.Msg;

import java.util.List;

public class ScoreboardManager {

    public void update(Player player, GameSession session) {
        Scoreboard board = org.bukkit.Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("bb", "dummy", Msg.c("&6&lBUILD BATTLE"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        int score = 15;
        obj.getScore(Msg.c("&7Режим: &f" + session.getMode().getDisplayName())).setScore(score--);
        obj.getScore(Msg.c("&7Тема: &e" + trim(session.getTheme()))).setScore(score--);
        obj.getScore(Msg.c(" ")).setScore(score--);

        switch (session.getPhase()) {
            case BUILDING -> {
                obj.getScore(Msg.c("&aСтройка!")).setScore(score--);
                obj.getScore(Msg.c("&fОсталось: &e" + formatTime(session.getBuildSecondsLeft()))).setScore(score--);
            }
            case VOTING -> {
                Team current = session.getCurrentVotingTeam();
                obj.getScore(Msg.c("&eОценивание")).setScore(score--);
                obj.getScore(Msg.c("&fОсталось: &e" + formatTime(session.getVotingSecondsLeft()))).setScore(score--);
                obj.getScore(Msg.c("&7Постройка: &f" + (session.getCurrentVotingTeamIndex() + 1) + "/" + session.getTeams().size())).setScore(score--);
            }
            default -> obj.getScore(Msg.c("&7Ожидание...")).setScore(score--);
        }

        obj.getScore(Msg.c("  ")).setScore(score--);
        obj.getScore(Msg.c("&6Рейтинг:")).setScore(score--);
        List<Team> ranking = session.getRanking();
        int shown = Math.min(ranking.size(), 5);
        for (int i = 0; i < shown; i++) {
            Team t = ranking.get(i);
            String label = teamLabel(t);
            obj.getScore(Msg.c("&f#" + (i + 1) + " " + label + " &7- &b" + t.getTotalScore())).setScore(score--);
        }

        player.setScoreboard(board);
    }

    private String teamLabel(Team t) {
        if (t.getMembers().isEmpty()) return "???";
        var op = org.bukkit.Bukkit.getOfflinePlayer(t.getMembers().get(0));
        String name = op.getName() == null ? "???" : op.getName();
        if (t.getMembers().size() > 1) name += " +" + (t.getMembers().size() - 1);
        return name;
    }

    private String trim(String s) {
        return s.length() > 20 ? s.substring(0, 20) + "…" : s;
    }

    private String formatTime(int seconds) {
        if (seconds < 0) seconds = 0;
        int m = seconds / 60;
        int s = seconds % 60;
        return String.format("%02d:%02d", m, s);
    }

    public void clear(Player player) {
        player.setScoreboard(org.bukkit.Bukkit.getScoreboardManager().getNewScoreboard());
    }
}

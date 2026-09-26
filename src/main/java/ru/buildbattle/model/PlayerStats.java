package ru.buildbattle.model;

public class PlayerStats {
    public int gamesPlayed = 0;
    public int wins = 0;
    public long totalScore = 0;

    public PlayerStats() {}

    public PlayerStats(int gamesPlayed, int wins, long totalScore) {
        this.gamesPlayed = gamesPlayed;
        this.wins = wins;
        this.totalScore = totalScore;
    }
}

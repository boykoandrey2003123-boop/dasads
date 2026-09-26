package ru.buildbattle.model;

public enum GameMode {
    SOLO("Соло", 1),
    DUO("Двое", 2),
    TRIO("Трое", 3);

    private final String displayName;
    private final int teamSize;

    GameMode(String displayName, int teamSize) {
        this.displayName = displayName;
        this.teamSize = teamSize;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getTeamSize() {
        return teamSize;
    }
}

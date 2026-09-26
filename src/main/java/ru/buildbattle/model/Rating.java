package ru.buildbattle.model;

import org.bukkit.Material;

public enum Rating {
    FIGNYA(Material.GRAY_DYE),
    PLOHO(Material.RED_DYE),
    NEPLOHO(Material.ORANGE_DYE),
    NORM(Material.YELLOW_DYE),
    HOROSHO(Material.LIME_DYE),
    OTLICHNO(Material.LIGHT_BLUE_DYE),
    BOZHESTVENNO(Material.NETHER_STAR);

    private final Material icon;
    // filled at runtime from config.yml (name + points)
    private String displayName;
    private int points;

    Rating(Material icon) {
        this.icon = icon;
    }

    public Material getIcon() {
        return icon;
    }

    public String getDisplayName() {
        return displayName == null ? name() : displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}

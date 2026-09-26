package ru.buildbattle.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Готовый мир BuildBattle с набором ячеек (Plot) под постройки.
 */
public class Arena {

    private final String id;
    private final String worldName;
    private final List<Plot> plots = new ArrayList<>();

    public Arena(String id, String worldName) {
        this.id = id;
        this.worldName = worldName;
    }

    public String getId() {
        return id;
    }

    public String getWorldName() {
        return worldName;
    }

    public List<Plot> getPlots() {
        return plots;
    }

    public void addPlot(Plot plot) {
        plots.add(plot);
    }

    public int getPlotCount() {
        return plots.size();
    }
}

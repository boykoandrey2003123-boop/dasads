package ru.buildbattle.manager;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import ru.buildbattle.BuildBattlePlugin;
import ru.buildbattle.model.Rating;

import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private final BuildBattlePlugin plugin;

    private int buildTimeSeconds;
    private int votingTimePerPlotSeconds;
    private int forceStartSeconds;
    private int minTeams;
    private final List<String> themes = new ArrayList<>();
    private final List<Material> fillBlocks = new ArrayList<>();

    public ConfigManager(BuildBattlePlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        var cfg = plugin.getConfig();

        buildTimeSeconds = cfg.getInt("build-time-seconds", 300);
        votingTimePerPlotSeconds = cfg.getInt("voting-time-per-plot-seconds", 20);
        forceStartSeconds = cfg.getInt("force-start-seconds", 60);
        minTeams = cfg.getInt("min-teams", 2);

        themes.clear();
        themes.addAll(cfg.getStringList("themes"));
        if (themes.isEmpty()) themes.add("Свободная тема");

        fillBlocks.clear();
        for (String s : cfg.getStringList("fill-blocks")) {
            Material m = Material.matchMaterial(s);
            if (m != null) fillBlocks.add(m);
        }
        if (fillBlocks.isEmpty()) fillBlocks.add(Material.STONE);

        ConfigurationSection ratingsSec = cfg.getConfigurationSection("ratings");
        if (ratingsSec != null) {
            for (Rating r : Rating.values()) {
                ConfigurationSection sec = ratingsSec.getConfigurationSection(r.name());
                if (sec != null) {
                    r.setDisplayName(sec.getString("name", r.name()));
                    r.setPoints(sec.getInt("points", r.ordinal() + 1));
                } else {
                    r.setDisplayName(r.name());
                    r.setPoints(r.ordinal() + 1);
                }
            }
        }
    }

    public int getBuildTimeSeconds() { return buildTimeSeconds; }
    public int getVotingTimePerPlotSeconds() { return votingTimePerPlotSeconds; }
    public int getForceStartSeconds() { return forceStartSeconds; }
    public int getMinTeams() { return minTeams; }
    public List<String> getThemes() { return themes; }
    public List<Material> getFillBlocks() { return fillBlocks; }

    public String randomTheme() {
        return themes.get((int) (Math.random() * themes.size()));
    }
}

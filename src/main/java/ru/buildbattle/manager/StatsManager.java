package ru.buildbattle.manager;

import org.bukkit.configuration.file.YamlConfiguration;
import ru.buildbattle.BuildBattlePlugin;
import ru.buildbattle.model.PlayerStats;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsManager {

    private final BuildBattlePlugin plugin;
    private final File file;
    private YamlConfiguration yaml;
    private final Map<UUID, PlayerStats> cache = new HashMap<>();

    public StatsManager(BuildBattlePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "stats.yml");
    }

    public void load() {
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException ignored) {
            }
        }
        yaml = YamlConfiguration.loadConfiguration(file);
    }

    public PlayerStats get(UUID uuid) {
        return cache.computeIfAbsent(uuid, id -> new PlayerStats(
                yaml.getInt(id + ".games", 0),
                yaml.getInt(id + ".wins", 0),
                yaml.getLong(id + ".score", 0)
        ));
    }

    public void addGameResult(UUID uuid, boolean won, int scoreGained) {
        PlayerStats s = get(uuid);
        s.gamesPlayed++;
        if (won) s.wins++;
        s.totalScore += scoreGained;
        yaml.set(uuid + ".games", s.gamesPlayed);
        yaml.set(uuid + ".wins", s.wins);
        yaml.set(uuid + ".score", s.totalScore);
    }

    public void save() {
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось сохранить stats.yml: " + e.getMessage());
        }
    }
}

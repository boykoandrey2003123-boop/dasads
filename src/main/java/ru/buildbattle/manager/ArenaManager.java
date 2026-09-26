package ru.buildbattle.manager;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.buildbattle.BuildBattlePlugin;
import ru.buildbattle.model.Arena;
import ru.buildbattle.model.Plot;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ArenaManager {

    private final BuildBattlePlugin plugin;
    private final File file;
    private YamlConfiguration yaml;

    private final Map<String, Arena> arenas = new LinkedHashMap<>();

    // ==== admin wand selection state: игрок -> [pos1, pos2] ====
    private final Map<UUID, Location> wandPos1 = new HashMap<>();
    private final Map<UUID, Location> wandPos2 = new HashMap<>();
    // ==== создание арены: игрок -> черновик ====
    private final Map<UUID, String> creatingArenaId = new HashMap<>();
    private final Map<UUID, List<PendingPlot>> pendingPlots = new HashMap<>();
    private final Set<UUID> awaitingArenaName = new HashSet<>();

    public static class PendingPlot {
        public Location outerMin, outerMax, innerMin, innerMax, spawn;
    }

    public ArenaManager(BuildBattlePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "arenas.yml");
    }

    public void load() {
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Не удалось создать arenas.yml: " + e.getMessage());
            }
        }
        yaml = YamlConfiguration.loadConfiguration(file);
        arenas.clear();
        ConfigurationSection arenasSec = yaml.getConfigurationSection("arenas");
        if (arenasSec == null) return;
        for (String arenaId : arenasSec.getKeys(false)) {
            ConfigurationSection aSec = arenasSec.getConfigurationSection(arenaId);
            String world = aSec.getString("world");
            Arena arena = new Arena(arenaId, world);
            ConfigurationSection plotsSec = aSec.getConfigurationSection("plots");
            if (plotsSec != null) {
                for (String plotIdStr : plotsSec.getKeys(false)) {
                    ConfigurationSection p = plotsSec.getConfigurationSection(plotIdStr);
                    Plot plot = new Plot(
                            Integer.parseInt(plotIdStr), world,
                            p.getInt("outerMinX"), p.getInt("outerMinY"), p.getInt("outerMinZ"),
                            p.getInt("outerMaxX"), p.getInt("outerMaxY"), p.getInt("outerMaxZ"),
                            p.getInt("innerMinX"), p.getInt("innerMinY"), p.getInt("innerMinZ"),
                            p.getInt("innerMaxX"), p.getInt("innerMaxY"), p.getInt("innerMaxZ"),
                            p.getDouble("spawnX"), p.getDouble("spawnY"), p.getDouble("spawnZ"),
                            (float) p.getDouble("spawnYaw"), (float) p.getDouble("spawnPitch")
                    );
                    arena.addPlot(plot);
                }
            }
            arenas.put(arenaId, arena);
        }
    }

    public void save() {
        yaml = new YamlConfiguration();
        for (Arena arena : arenas.values()) {
            String base = "arenas." + arena.getId();
            yaml.set(base + ".world", arena.getWorldName());
            for (Plot plot : arena.getPlots()) {
                String pBase = base + ".plots." + plot.getId();
                yaml.set(pBase + ".outerMinX", plot.getOuterMinX());
                yaml.set(pBase + ".outerMinY", plot.getOuterMinY());
                yaml.set(pBase + ".outerMinZ", plot.getOuterMinZ());
                yaml.set(pBase + ".outerMaxX", plot.getOuterMaxX());
                yaml.set(pBase + ".outerMaxY", plot.getOuterMaxY());
                yaml.set(pBase + ".outerMaxZ", plot.getOuterMaxZ());
                yaml.set(pBase + ".innerMinX", plot.getInnerMinX());
                yaml.set(pBase + ".innerMinY", plot.getInnerMinY());
                yaml.set(pBase + ".innerMinZ", plot.getInnerMinZ());
                yaml.set(pBase + ".innerMaxX", plot.getInnerMaxX());
                yaml.set(pBase + ".innerMaxY", plot.getInnerMaxY());
                yaml.set(pBase + ".innerMaxZ", plot.getInnerMaxZ());
                yaml.set(pBase + ".spawnX", plot.getSpawnX());
                yaml.set(pBase + ".spawnY", plot.getSpawnY());
                yaml.set(pBase + ".spawnZ", plot.getSpawnZ());
                yaml.set(pBase + ".spawnYaw", plot.getSpawnYaw());
                yaml.set(pBase + ".spawnPitch", plot.getSpawnPitch());
            }
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось сохранить arenas.yml: " + e.getMessage());
        }
    }

    public Collection<Arena> getArenas() {
        return arenas.values();
    }

    public Arena getArena(String id) {
        return arenas.get(id);
    }

    public void addArena(Arena arena) {
        arenas.put(arena.getId(), arena);
        save();
    }

    public void removeArena(String id) {
        arenas.remove(id);
        save();
    }

    /** Арены, у которых хватает готовых ячеек под режим. */
    public List<Arena> getArenasForMode(ru.buildbattle.model.GameMode mode) {
        List<Arena> list = new ArrayList<>();
        for (Arena a : arenas.values()) {
            if (a.getPlotCount() >= 2) list.add(a);
        }
        return list;
    }

    // ---- wand ----
    public void setPos1(UUID uuid, Location loc) { wandPos1.put(uuid, loc); }
    public void setPos2(UUID uuid, Location loc) { wandPos2.put(uuid, loc); }
    public Location getPos1(UUID uuid) { return wandPos1.get(uuid); }
    public Location getPos2(UUID uuid) { return wandPos2.get(uuid); }

    // ---- creation wizard ----
    public void startCreating(UUID uuid, String arenaId) {
        creatingArenaId.put(uuid, arenaId);
        pendingPlots.put(uuid, new ArrayList<>());
    }

    public String getCreatingArenaId(UUID uuid) { return creatingArenaId.get(uuid); }

    public List<PendingPlot> getPendingPlots(UUID uuid) {
        return pendingPlots.computeIfAbsent(uuid, k -> new ArrayList<>());
    }

    public void requestArenaName(UUID uuid) {
        awaitingArenaName.add(uuid);
    }

    public boolean isAwaitingArenaName(UUID uuid) {
        return awaitingArenaName.contains(uuid);
    }

    public void clearAwaitingArenaName(UUID uuid) {
        awaitingArenaName.remove(uuid);
    }

    public void finishCreating(UUID uuid) {
        creatingArenaId.remove(uuid);
        pendingPlots.remove(uuid);
        wandPos1.remove(uuid);
        wandPos2.remove(uuid);
    }
}

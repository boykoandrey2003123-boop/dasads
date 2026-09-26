package ru.buildbattle;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import ru.buildbattle.gui.AdminGuis;
import ru.buildbattle.gui.PlayerGuis;
import ru.buildbattle.listener.*;
import ru.buildbattle.manager.ArenaManager;
import ru.buildbattle.manager.ConfigManager;
import ru.buildbattle.manager.GameManager;
import ru.buildbattle.manager.ScoreboardManager;
import ru.buildbattle.manager.StatsManager;
import ru.buildbattle.util.ItemFactory;

public class BuildBattlePlugin extends JavaPlugin {

    private static BuildBattlePlugin instance;

    private ConfigManager configManager;
    private ArenaManager arenaManager;
    private GameManager gameManager;
    private StatsManager statsManager;
    private ScoreboardManager scoreboardManager;
    private ItemFactory itemFactory;
    private PlayerGuis playerGuis;
    private AdminGuis adminGuis;

    @Override
    public void onEnable() {
        instance = this;

        configManager = new ConfigManager(this);
        configManager.load();

        arenaManager = new ArenaManager(this);
        arenaManager.load();

        statsManager = new StatsManager(this);
        statsManager.load();

        scoreboardManager = new ScoreboardManager();
        itemFactory = new ItemFactory(this);
        playerGuis = new PlayerGuis(this);
        adminGuis = new AdminGuis(this);

        gameManager = new GameManager(this);
        gameManager.start();

        getServer().getPluginManager().registerEvents(new ConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new WandListener(this), this);
        getServer().getPluginManager().registerEvents(new ArenaNameChatListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiClickListener(this), this);

        var bbCommand = new BBCommand(this);
        getCommand("bb").setExecutor(bbCommand);

        getLogger().info("BuildBattle включен! Арен загружено: " + arenaManager.getArenas().size());
    }

    @Override
    public void onDisable() {
        if (arenaManager != null) arenaManager.save();
        if (statsManager != null) statsManager.save();
    }

    public Location getLobbyLocation() {
        var cfg = getConfig();
        var world = Bukkit.getWorld(cfg.getString("lobby-world", "world"));
        if (world == null) world = Bukkit.getWorlds().get(0);
        return new Location(world, cfg.getDouble("lobby-x", 0.5), cfg.getDouble("lobby-y", 100), cfg.getDouble("lobby-z", 0.5));
    }

    public static BuildBattlePlugin getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public ArenaManager getArenaManager() { return arenaManager; }
    public GameManager getGameManager() { return gameManager; }
    public StatsManager getStatsManager() { return statsManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public ItemFactory getItemFactory() { return itemFactory; }
    public PlayerGuis getPlayerGuis() { return playerGuis; }
    public AdminGuis getAdminGuis() { return adminGuis; }
}

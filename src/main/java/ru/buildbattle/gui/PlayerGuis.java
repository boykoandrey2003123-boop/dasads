package ru.buildbattle.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import ru.buildbattle.BuildBattlePlugin;
import ru.buildbattle.model.GameMode;
import ru.buildbattle.model.GameSession;
import ru.buildbattle.model.Rating;
import ru.buildbattle.util.ItemBuilder;
import ru.buildbattle.util.Msg;

import java.util.List;

public class PlayerGuis {

    private final BuildBattlePlugin plugin;

    public PlayerGuis(BuildBattlePlugin plugin) {
        this.plugin = plugin;
    }

    // ============== HOLDERS ==============

    public static class MainMenuHolder implements InventoryHolder {
        public Inventory inv;
        @Override public Inventory getInventory() { return inv; }
    }

    public static class ModeSelectHolder implements InventoryHolder {
        public Inventory inv;
        @Override public Inventory getInventory() { return inv; }
    }

    public static class SettingsHolder implements InventoryHolder {
        public Inventory inv;
        @Override public Inventory getInventory() { return inv; }
    }

    public static class BlockPickHolder implements InventoryHolder {
        public Inventory inv;
        @Override public Inventory getInventory() { return inv; }
    }

    public static class RatingHolder implements InventoryHolder {
        public Inventory inv;
        @Override public Inventory getInventory() { return inv; }
    }

    // ============== MAIN MENU ==============

    public void openMainMenu(Player player) {
        MainMenuHolder holder = new MainMenuHolder();
        Inventory inv = org.bukkit.Bukkit.createInventory(holder, 27, Msg.c("&6&lBuildBattle — Меню"));
        holder.inv = inv;

        inv.setItem(11, new ItemBuilder(Material.GRASS_BLOCK)
                .name("&a&lИграть")
                .lore("&7Выбрать режим и встать в очередь").build());

        inv.setItem(13, new ItemBuilder(Material.PAPER)
                .name("&e&lСтатистика")
                .lore(statsLore(player)).build());

        inv.setItem(15, new ItemBuilder(Material.BARRIER)
                .name("&c&lПокинуть игру/очередь")
                .lore("&7Выйти из текущей очереди или игры").build());

        if (player.hasPermission("buildbattle.admin")) {
            inv.setItem(22, new ItemBuilder(Material.COMMAND_BLOCK)
                    .name("&d&lАдмин-панель")
                    .lore("&7Управление аренами").build());
        }

        fillBorder(inv);
        player.openInventory(inv);
    }

    private List<String> statsLore(Player player) {
        var s = plugin.getStatsManager().get(player.getUniqueId());
        return List.of(
                "&7Игр сыграно: &f" + s.gamesPlayed,
                "&7Побед: &f" + s.wins,
                "&7Всего очков: &f" + s.totalScore
        );
    }

    // ============== MODE SELECT ==============

    public void openModeSelect(Player player) {
        ModeSelectHolder holder = new ModeSelectHolder();
        Inventory inv = org.bukkit.Bukkit.createInventory(holder, 27, Msg.c("&6Выбор режима"));
        holder.inv = inv;

        inv.setItem(11, new ItemBuilder(Material.IRON_SWORD)
                .name("&f&lСоло")
                .lore("&7Каждый сам за себя", "&7В очереди: &e" + plugin.getGameManager().getQueueSize(GameMode.SOLO)).build());
        inv.setItem(13, new ItemBuilder(Material.GOLDEN_SWORD)
                .name("&e&lДвое")
                .lore("&7Команды по 2 игрока", "&7В очереди: &e" + plugin.getGameManager().getQueueSize(GameMode.DUO)).build());
        inv.setItem(15, new ItemBuilder(Material.DIAMOND_SWORD)
                .name("&b&lТрое")
                .lore("&7Команды по 3 игрока", "&7В очереди: &e" + plugin.getGameManager().getQueueSize(GameMode.TRIO)).build());

        fillBorder(inv);
        player.openInventory(inv);
    }

    // ============== SETTINGS (builder tools) ==============

    public void openSettings(Player player, GameSession session) {
        SettingsHolder holder = new SettingsHolder();
        Inventory inv = org.bukkit.Bukkit.createInventory(holder, 27, Msg.c("&b&lНастройки площадки"));
        holder.inv = inv;

        Material selected = session.getSelectedFillBlock(player.getUniqueId());
        Material selectedIcon = selected == Material.AIR ? Material.STRUCTURE_VOID : selected;
        inv.setItem(10, new ItemBuilder(selectedIcon)
                .name("&e&lЗалить площадку")
                .lore("&7Текущий блок: &f" + selected.name(), "&7ЛКМ - выбрать блок", "&7ПКМ - залить площадку выбранным блоком").build());

        inv.setItem(12, new ItemBuilder(Material.TNT)
                .name("&c&lОчистить площадку")
                .lore("&7Удалить все построенные блоки").build());

        inv.setItem(14, new ItemBuilder(Material.MAP)
                .name("&a&lТелепорт на площадку")
                .lore("&7Вернуться на свою постройку").build());

        boolean borders = session.isBordersEnabled(player.getUniqueId());
        inv.setItem(16, new ItemBuilder(borders ? Material.GLOWSTONE_DUST : Material.GUNPOWDER)
                .name((borders ? "&a" : "&7") + "&lПодсветка границ: " + (borders ? "&aВКЛ" : "&cВЫКЛ"))
                .lore("&7Показывать частицами границы", "&7вашей площадки").build());

        fillBorder(inv);
        player.openInventory(inv);
    }

    public void openBlockPicker(Player player) {
        BlockPickHolder holder = new BlockPickHolder();
        List<Material> blocks = plugin.getConfigManager().getFillBlocks();
        int size = Math.max(9, ((blocks.size() / 9) + 1) * 9);
        Inventory inv = org.bukkit.Bukkit.createInventory(holder, size, Msg.c("&e&lВыбор блока"));
        holder.inv = inv;
        for (Material m : blocks) {
            inv.addItem(new ItemBuilder(m == Material.AIR ? Material.STRUCTURE_VOID : m)
                    .name("&f" + m.name())
                    .lore("&7Нажмите, чтобы выбрать").build());
        }
        player.openInventory(inv);
    }

    // ============== RATING GUI ==============

    public void openRating(Player player, GameSession session) {
        RatingHolder holder = new RatingHolder();
        Inventory inv = org.bukkit.Bukkit.createInventory(holder, 27, Msg.c("&6&lОцените постройку"));
        holder.inv = inv;

        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        Rating[] ratings = Rating.values();
        for (int i = 0; i < ratings.length; i++) {
            Rating r = ratings[i];
            inv.setItem(slots[i], new ItemBuilder(r.getIcon())
                    .name("&f" + r.getDisplayName())
                    .lore("&7Очки: &e" + r.getPoints(), "&7Нажмите, чтобы оценить").build());
        }
        fillBorder(inv);
        player.openInventory(inv);
    }

    // ============== UTIL ==============

    private void fillBorder(Inventory inv) {
        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) inv.setItem(i, filler);
        }
    }
}

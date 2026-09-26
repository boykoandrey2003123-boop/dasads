package ru.buildbattle.gui;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import ru.buildbattle.BuildBattlePlugin;
import ru.buildbattle.manager.ArenaManager;
import ru.buildbattle.model.Arena;
import ru.buildbattle.model.Plot;
import ru.buildbattle.util.ItemBuilder;
import ru.buildbattle.util.Msg;
import ru.buildbattle.util.RegionUtil;

import java.util.List;

public class AdminGuis {

    private final BuildBattlePlugin plugin;

    public AdminGuis(BuildBattlePlugin plugin) {
        this.plugin = plugin;
    }

    public static class AdminPanelHolder implements InventoryHolder {
        public Inventory inv;
        @Override public Inventory getInventory() { return inv; }
    }

    public static class ArenaListHolder implements InventoryHolder {
        public Inventory inv;
        @Override public Inventory getInventory() { return inv; }
    }

    public static class ArenaWizardHolder implements InventoryHolder {
        public Inventory inv;
        @Override public Inventory getInventory() { return inv; }
    }

    public void openAdminPanel(Player player) {
        AdminPanelHolder holder = new AdminPanelHolder();
        Inventory inv = Bukkit.createInventory(holder, 27, Msg.c("&d&lАдмин-панель BuildBattle"));
        holder.inv = inv;

        inv.setItem(11, new ItemBuilder(Material.EMERALD_BLOCK)
                .name("&a&lСоздать арену")
                .lore("&7Начать создание новой арены", "&7в текущем мире").build());
        inv.setItem(13, new ItemBuilder(Material.BOOK)
                .name("&e&lСписок арен")
                .lore("&7Всего арен: &f" + plugin.getArenaManager().getArenas().size()).build());
        inv.setItem(15, new ItemBuilder(Material.BLAZE_ROD)
                .name("&d&lПолучить инструмент выделения")
                .lore("&7Выдаёт wand для выделения ячеек").build());

        fillBorder(inv);
        player.openInventory(inv);
    }

    public void openArenaList(Player player) {
        ArenaListHolder holder = new ArenaListHolder();
        Inventory inv = Bukkit.createInventory(holder, 54, Msg.c("&e&lСписок арен"));
        holder.inv = inv;
        int slot = 0;
        for (Arena arena : plugin.getArenaManager().getArenas()) {
            if (slot >= 53) break;
            inv.setItem(slot++, new ItemBuilder(Material.GRASS_BLOCK)
                    .name("&f" + arena.getId())
                    .lore("&7Мир: &f" + arena.getWorldName(),
                          "&7Ячеек: &f" + arena.getPlotCount(),
                          "&cShift+ПКМ - удалить").build());
        }
        player.openInventory(inv);
    }

    public void openWizard(Player player) {
        String arenaId = plugin.getArenaManager().getCreatingArenaId(player.getUniqueId());
        ArenaWizardHolder holder = new ArenaWizardHolder();
        Inventory inv = Bukkit.createInventory(holder, 27, Msg.c("&a&lСоздание арены: " + arenaId));
        holder.inv = inv;

        Location p1 = plugin.getArenaManager().getPos1(player.getUniqueId());
        Location p2 = plugin.getArenaManager().getPos2(player.getUniqueId());
        int pending = plugin.getArenaManager().getPendingPlots(player.getUniqueId()).size();

        inv.setItem(10, new ItemBuilder(Material.BLAZE_ROD)
                .name("&d&lВыдать инструмент выделения")
                .lore("&7ЛКМ = точка 1, ПКМ = точка 2").build());

        inv.setItem(12, new ItemBuilder(p1 != null && p2 != null ? Material.LIME_CONCRETE : Material.RED_CONCRETE)
                .name("&e&lДобавить ячейку из выделения")
                .lore("&7Точка 1: " + (p1 == null ? "&cне задана" : "&a✓"),
                      "&7Точка 2: " + (p2 == null ? "&cне задана" : "&a✓"),
                      "&7Стены (1 блок по X/Z) станут защищены",
                      "&7Добавлено ячеек: &f" + pending).build());

        inv.setItem(14, new ItemBuilder(Material.NETHER_STAR)
                .name("&b&lЗавершить и сохранить арену")
                .lore("&7Сохранить все добавленные ячейки", "&7Ячеек готово: &f" + pending,
                      "&cМинимум 2 ячейки для сохранения").build());

        inv.setItem(16, new ItemBuilder(Material.BARRIER)
                .name("&c&lОтменить создание")
                .lore("&7Прогресс будет утерян").build());

        fillBorder(inv);
        player.openInventory(inv);
    }

    /** Создаёт PendingPlot на основе текущего выделения (pos1/pos2) игрока. */
    public boolean addPlotFromSelection(Player player) {
        ArenaManager am = plugin.getArenaManager();
        Location p1 = am.getPos1(player.getUniqueId());
        Location p2 = am.getPos2(player.getUniqueId());
        if (p1 == null || p2 == null) {
            Msg.send(player, "&cСначала выделите обе точки инструментом!");
            return false;
        }
        Location min = RegionUtil.min(p1, p2);
        Location max = RegionUtil.max(p1, p2);

        ArenaManager.PendingPlot pp = new ArenaManager.PendingPlot();
        pp.outerMin = min;
        pp.outerMax = max;

        int innerMinX = min.getBlockX() + 1;
        int innerMaxX = max.getBlockX() - 1;
        int innerMinZ = min.getBlockZ() + 1;
        int innerMaxZ = max.getBlockZ() - 1;
        if (innerMinX > innerMaxX) { innerMinX = min.getBlockX(); innerMaxX = max.getBlockX(); }
        if (innerMinZ > innerMaxZ) { innerMinZ = min.getBlockZ(); innerMaxZ = max.getBlockZ(); }

        Location innerMin = new Location(min.getWorld(), innerMinX, min.getBlockY(), innerMinZ);
        Location innerMax = new Location(min.getWorld(), innerMaxX, max.getBlockY(), innerMaxZ);
        pp.innerMin = innerMin;
        pp.innerMax = innerMax;

        double cx = (innerMinX + innerMaxX) / 2.0 + 0.5;
        double cz = (innerMinZ + innerMaxZ) / 2.0 + 0.5;
        double cy = min.getBlockY() + 1;
        pp.spawn = new Location(min.getWorld(), cx, cy, cz);

        am.getPendingPlots(player.getUniqueId()).add(pp);
        am.setPos1(player.getUniqueId(), null);
        am.setPos2(player.getUniqueId(), null);
        Msg.send(player, "&aЯчейка добавлена! Всего: &f" + am.getPendingPlots(player.getUniqueId()).size());
        return true;
    }

    public boolean finishArena(Player player) {
        ArenaManager am = plugin.getArenaManager();
        String arenaId = am.getCreatingArenaId(player.getUniqueId());
        if (arenaId == null) return false;
        List<ArenaManager.PendingPlot> pending = am.getPendingPlots(player.getUniqueId());
        if (pending.size() < 2) {
            Msg.send(player, "&cНужно минимум 2 ячейки, сейчас: &f" + pending.size());
            return false;
        }
        String worldName = pending.get(0).outerMin.getWorld().getName();
        Arena arena = new Arena(arenaId, worldName);
        int id = 1;
        for (ArenaManager.PendingPlot pp : pending) {
            Plot plot = new Plot(id++, worldName,
                    pp.outerMin.getBlockX(), pp.outerMin.getBlockY(), pp.outerMin.getBlockZ(),
                    pp.outerMax.getBlockX(), pp.outerMax.getBlockY(), pp.outerMax.getBlockZ(),
                    pp.innerMin.getBlockX(), pp.innerMin.getBlockY(), pp.innerMin.getBlockZ(),
                    pp.innerMax.getBlockX(), pp.innerMax.getBlockY(), pp.innerMax.getBlockZ(),
                    pp.spawn.getX(), pp.spawn.getY(), pp.spawn.getZ(), 0f, 0f);
            arena.addPlot(plot);
        }
        am.addArena(arena);
        am.finishCreating(player.getUniqueId());
        Msg.send(player, "&aАрена &f" + arenaId + " &aсохранена с &f" + arena.getPlotCount() + " &aячейками!");
        return true;
    }

    private void fillBorder(Inventory inv) {
        ItemStack filler = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) inv.setItem(i, filler);
        }
    }
}

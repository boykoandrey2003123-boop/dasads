package ru.buildbattle.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import ru.buildbattle.BuildBattlePlugin;
import ru.buildbattle.gui.AdminGuis;
import ru.buildbattle.gui.PlayerGuis;
import ru.buildbattle.model.GameMode;
import ru.buildbattle.model.GamePhase;
import ru.buildbattle.model.GameSession;
import ru.buildbattle.model.Rating;
import ru.buildbattle.util.Msg;

public class GuiClickListener implements Listener {

    private final BuildBattlePlugin plugin;

    public GuiClickListener(BuildBattlePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        var holder = event.getInventory().getHolder();
        if (holder == null) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (holder instanceof PlayerGuis.MainMenuHolder) {
            event.setCancelled(true);
            handleMainMenu(player, event.getSlot());
        } else if (holder instanceof PlayerGuis.ModeSelectHolder) {
            event.setCancelled(true);
            handleModeSelect(player, event.getSlot());
        } else if (holder instanceof PlayerGuis.SettingsHolder) {
            event.setCancelled(true);
            handleSettings(player, event.getSlot(), event.getClick());
        } else if (holder instanceof PlayerGuis.BlockPickHolder) {
            event.setCancelled(true);
            handleBlockPick(player, event.getCurrentItem());
        } else if (holder instanceof PlayerGuis.RatingHolder) {
            event.setCancelled(true);
            handleRating(player, event.getSlot());
        } else if (holder instanceof AdminGuis.AdminPanelHolder) {
            event.setCancelled(true);
            handleAdminPanel(player, event.getSlot());
        } else if (holder instanceof AdminGuis.ArenaListHolder) {
            event.setCancelled(true);
            handleArenaList(player, event.getCurrentItem(), event.getClick());
        } else if (holder instanceof AdminGuis.ArenaWizardHolder) {
            event.setCancelled(true);
            handleWizard(player, event.getSlot());
        }
    }

    private void handleMainMenu(Player player, int slot) {
        switch (slot) {
            case 11 -> plugin.getPlayerGuis().openModeSelect(player);
            case 15 -> { plugin.getGameManager().leaveGame(player); player.closeInventory(); }
            case 22 -> {
                if (player.hasPermission("buildbattle.admin")) plugin.getAdminGuis().openAdminPanel(player);
            }
            default -> {}
        }
    }

    private void handleModeSelect(Player player, int slot) {
        GameMode mode = switch (slot) {
            case 11 -> GameMode.SOLO;
            case 13 -> GameMode.DUO;
            case 15 -> GameMode.TRIO;
            default -> null;
        };
        if (mode == null) return;
        plugin.getGameManager().joinQueue(player, mode);
        player.closeInventory();
    }

    private void handleSettings(Player player, int slot, ClickType click) {
        GameSession session = plugin.getGameManager().getSession(player.getUniqueId());
        if (session == null) { player.closeInventory(); return; }
        var team = session.getTeamOf(player.getUniqueId());
        if (team == null) return;

        switch (slot) {
            case 10 -> {
                if (click.isRightClick()) {
                    if (session.getPhase() != GamePhase.BUILDING) {
                        Msg.send(player, "&cМожно заливать только во время постройки.");
                        return;
                    }
                    Material mat = session.getSelectedFillBlock(player.getUniqueId());
                    team.getPlot().fill(org.bukkit.Bukkit.getWorld(session.getArena().getWorldName()), mat);
                    Msg.send(player, "&aПлощадка залита блоком &f" + mat.name());
                    player.closeInventory();
                } else {
                    plugin.getPlayerGuis().openBlockPicker(player);
                }
            }
            case 12 -> {
                if (session.getPhase() != GamePhase.BUILDING) {
                    Msg.send(player, "&cМожно очищать только во время постройки.");
                    return;
                }
                team.getPlot().clearBuildable(org.bukkit.Bukkit.getWorld(session.getArena().getWorldName()));
                Msg.send(player, "&aПлощадка очищена.");
                player.closeInventory();
            }
            case 14 -> {
                player.teleport(team.getPlot().getSpawnLocation(org.bukkit.Bukkit.getWorld(session.getArena().getWorldName())));
                player.closeInventory();
            }
            case 16 -> {
                session.toggleBorders(player.getUniqueId());
                plugin.getPlayerGuis().openSettings(player, session);
            }
            default -> {}
        }
    }

    private void handleBlockPick(Player player, ItemStack clicked) {
        if (clicked == null || !clicked.hasItemMeta()) return;
        GameSession session = plugin.getGameManager().getSession(player.getUniqueId());
        if (session == null) return;
        Material mat = clicked.getType() == Material.STRUCTURE_VOID ? Material.AIR : clicked.getType();
        session.setSelectedFillBlock(player.getUniqueId(), mat);
        Msg.send(player, "&aВыбран блок для заливки: &f" + mat.name());
        plugin.getPlayerGuis().openSettings(player, session);
    }

    private void handleRating(Player player, int slot) {
        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        Rating[] ratings = Rating.values();
        int index = -1;
        for (int i = 0; i < slots.length; i++) if (slots[i] == slot) { index = i; break; }
        if (index < 0) return;

        GameSession session = plugin.getGameManager().getSession(player.getUniqueId());
        if (session == null) return;
        plugin.getGameManager().submitVote(player, session, ratings[index]);
        player.closeInventory();
    }

    private void handleAdminPanel(Player player, int slot) {
        switch (slot) {
            case 11 -> {
                player.closeInventory();
                plugin.getArenaManager().requestArenaName(player.getUniqueId());
                Msg.send(player, "&eНапишите название новой арены в чат:");
            }
            case 13 -> plugin.getAdminGuis().openArenaList(player);
            case 15 -> {
                player.getInventory().addItem(plugin.getItemFactory().createWand());
                Msg.send(player, "&aВыдан инструмент выделения.");
            }
            default -> {}
        }
    }

    private void handleArenaList(Player player, ItemStack clicked, ClickType click) {
        if (clicked == null || !clicked.hasItemMeta() || clicked.getItemMeta().getDisplayName().isEmpty()) return;
        String arenaId = org.bukkit.ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if (click == ClickType.SHIFT_RIGHT) {
            plugin.getArenaManager().removeArena(arenaId);
            Msg.send(player, "&cАрена &f" + arenaId + " &cудалена.");
            plugin.getAdminGuis().openArenaList(player);
        } else {
            var arena = plugin.getArenaManager().getArena(arenaId);
            if (arena != null) Msg.send(player, "&7Арена &f" + arenaId + "&7: мир=" + arena.getWorldName() + ", ячеек=" + arena.getPlotCount());
        }
    }

    private void handleWizard(Player player, int slot) {
        switch (slot) {
            case 10 -> {
                player.getInventory().addItem(plugin.getItemFactory().createWand());
                Msg.send(player, "&aВыдан инструмент выделения.");
            }
            case 12 -> {
                plugin.getAdminGuis().addPlotFromSelection(player);
                plugin.getAdminGuis().openWizard(player);
            }
            case 14 -> {
                if (plugin.getAdminGuis().finishArena(player)) player.closeInventory();
                else plugin.getAdminGuis().openWizard(player);
            }
            case 16 -> {
                plugin.getArenaManager().finishCreating(player.getUniqueId());
                Msg.send(player, "&cСоздание арены отменено.");
                player.closeInventory();
            }
            default -> {}
        }
    }
}

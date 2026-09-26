package ru.buildbattle.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.buildbattle.BuildBattlePlugin;
import ru.buildbattle.util.Msg;

public class ArenaNameChatListener implements org.bukkit.event.Listener {

    private final BuildBattlePlugin plugin;

    public ArenaNameChatListener(BuildBattlePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        var player = event.getPlayer();
        if (!plugin.getArenaManager().isAwaitingArenaName(player.getUniqueId())) return;

        event.setCancelled(true);
        String name = event.getMessage().trim().replaceAll("[^a-zA-Zа-яА-Я0-9_-]", "");
        plugin.getArenaManager().clearAwaitingArenaName(player.getUniqueId());

        if (name.isEmpty()) {
            Msg.send(player, "&cНекорректное название, попробуйте снова через меню.");
            return;
        }
        if (plugin.getArenaManager().getArena(name) != null) {
            Msg.send(player, "&cАрена с таким названием уже существует.");
            return;
        }

        plugin.getArenaManager().startCreating(player.getUniqueId(), name);
        Msg.send(player, "&aНачато создание арены &f" + name + "&a. Откройте меню инструментов.");
        org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
            player.getInventory().addItem(plugin.getItemFactory().createWand());
            plugin.getAdminGuis().openWizard(player);
        });
    }
}

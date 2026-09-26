package ru.buildbattle.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.buildbattle.BuildBattlePlugin;
import ru.buildbattle.util.Msg;

public class ConnectionListener implements Listener {

    private final BuildBattlePlugin plugin;

    public ConnectionListener(BuildBattlePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var p = event.getPlayer();
        if (!plugin.getGameManager().isInGame(p.getUniqueId())) {
            p.getInventory().setItem(8, plugin.getItemFactory().createMenuCompass());
            Msg.send(p, "&aДобро пожаловать! ПКМ по компасу, чтобы открыть меню BuildBattle.");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getGameManager().leaveAllQueues(event.getPlayer().getUniqueId());
        plugin.getArenaManager().finishCreating(event.getPlayer().getUniqueId());
        plugin.getArenaManager().clearAwaitingArenaName(event.getPlayer().getUniqueId());
    }
}

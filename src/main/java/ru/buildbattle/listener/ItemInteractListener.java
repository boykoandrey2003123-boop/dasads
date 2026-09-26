package ru.buildbattle.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.buildbattle.BuildBattlePlugin;
import ru.buildbattle.model.GameSession;
import ru.buildbattle.util.Msg;

public class ItemInteractListener implements Listener {

    private final BuildBattlePlugin plugin;

    public ItemInteractListener(BuildBattlePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        var item = event.getItem();
        if (item == null) return;
        var factory = plugin.getItemFactory();
        var player = event.getPlayer();

        if (factory.isTagged(item, "menu")) {
            event.setCancelled(true);
            plugin.getPlayerGuis().openMainMenu(player);
            return;
        }

        if (factory.isTagged(item, "tools")) {
            event.setCancelled(true);
            GameSession session = plugin.getGameManager().getSession(player.getUniqueId());
            if (session == null) {
                Msg.send(player, "&cВы не в игре.");
                return;
            }
            plugin.getPlayerGuis().openSettings(player, session);
            return;
        }

        if (factory.isTagged(item, "rating")) {
            event.setCancelled(true);
            GameSession session = plugin.getGameManager().getSession(player.getUniqueId());
            if (session == null) return;
            plugin.getPlayerGuis().openRating(player, session);
        }
    }
}

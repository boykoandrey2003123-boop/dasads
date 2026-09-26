package ru.buildbattle.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.buildbattle.BuildBattlePlugin;
import ru.buildbattle.util.Msg;

public class WandListener implements Listener {

    private final BuildBattlePlugin plugin;

    public WandListener(BuildBattlePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        var item = event.getItem();
        if (item == null || !plugin.getItemFactory().isTagged(item, "wand")) return;
        if (event.getClickedBlock() == null) return;

        var player = event.getPlayer();
        var loc = event.getClickedBlock().getLocation();

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            plugin.getArenaManager().setPos1(player.getUniqueId(), loc);
            Msg.send(player, "&aТочка 1 установлена: &f" + fmt(loc));
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            plugin.getArenaManager().setPos2(player.getUniqueId(), loc);
            Msg.send(player, "&aТочка 2 установлена: &f" + fmt(loc));
        }
    }

    private String fmt(org.bukkit.Location loc) {
        return loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
    }
}

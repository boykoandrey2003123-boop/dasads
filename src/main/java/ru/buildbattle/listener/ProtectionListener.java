package ru.buildbattle.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import ru.buildbattle.BuildBattlePlugin;
import ru.buildbattle.manager.GameManager;
import ru.buildbattle.model.Arena;
import ru.buildbattle.model.GamePhase;
import ru.buildbattle.model.Plot;
import ru.buildbattle.util.Msg;

public class ProtectionListener implements Listener {

    private final BuildBattlePlugin plugin;

    public ProtectionListener(BuildBattlePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (!check(event.getPlayer(), event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (!check(event.getPlayer(), event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    /** @return true, если игроку разрешено ломать/ставить блок в этом месте. */
    private boolean check(Player player, org.bukkit.Location loc) {
        GameManager gm = plugin.getGameManager();
        GameManager.PlotHit hit = gm.findPlotAt(loc);

        if (hit != null) {
            // активная игра
            if (hit.session().getPhase() != GamePhase.BUILDING) {
                Msg.send(player, "&cСейчас нельзя строить (не фаза постройки).");
                return false;
            }
            if (!hit.team().isMember(player.getUniqueId())) {
                Msg.send(player, "&cЭто не ваша площадка!");
                return false;
            }
            if (!hit.plot().isInsideBuildable(loc)) {
                Msg.send(player, "&cНельзя ломать стены вокруг площадки!");
                return false;
            }
            return true;
        }

        // площадка не используется активной игрой - проверим, не часть ли она вообще какой-то арены (защита от гриферства)
        for (Arena arena : plugin.getArenaManager().getArenas()) {
            for (Plot plot : arena.getPlots()) {
                if (plot.isInsideOuter(loc)) {
                    if (player.hasPermission("buildbattle.admin")) return true;
                    Msg.send(player, "&cЭта площадка сейчас не используется в игре.");
                    return false;
                }
            }
        }

        return true; // вне арен BuildBattle - обычные правила мира
    }
}

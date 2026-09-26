package ru.buildbattle.listener;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.buildbattle.BuildBattlePlugin;

public class BBCommand implements CommandExecutor {

    private final BuildBattlePlugin plugin;

    public BBCommand(BuildBattlePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только для игроков.");
            return true;
        }
        plugin.getPlayerGuis().openMainMenu(player);
        return true;
    }
}

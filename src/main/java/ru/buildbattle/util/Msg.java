package ru.buildbattle.util;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Msg {

    public static final String PREFIX = ChatColor.GOLD + "" + ChatColor.BOLD + "BuildBattle " + ChatColor.GRAY + "» " + ChatColor.RESET;

    public static String c(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static void send(Player p, String s) {
        p.sendMessage(PREFIX + c(s));
    }

    public static void title(Player p, String title, String subtitle) {
        p.sendTitle(c(title), c(subtitle), 5, 40, 10);
    }

    public static void actionBar(Player p, String s) {
        p.sendActionBar(net.kyori.adventure.text.Component.text(c(s)));
    }
}

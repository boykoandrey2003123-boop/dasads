package ru.buildbattle.util;

import org.bukkit.Location;

public class RegionUtil {

    public static Location min(Location a, Location b) {
        return new Location(a.getWorld(),
                Math.min(a.getBlockX(), b.getBlockX()),
                Math.min(a.getBlockY(), b.getBlockY()),
                Math.min(a.getBlockZ(), b.getBlockZ()));
    }

    public static Location max(Location a, Location b) {
        return new Location(a.getWorld(),
                Math.max(a.getBlockX(), b.getBlockX()),
                Math.max(a.getBlockY(), b.getBlockY()),
                Math.max(a.getBlockZ(), b.getBlockZ()));
    }
}

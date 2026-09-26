package ru.buildbattle.model;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * Одна ячейка (площадка) под постройку внутри арены.
 * outerMin/outerMax - весь объём ячейки, включая стены (границы защиты).
 * innerMin/innerMax - зона, где реально можно строить/ломать (внутри стен).
 * spawn - точка, куда телепортируется игрок/команда в начале игры.
 */
public class Plot {

    private final int id;
    private final String worldName;

    private final int outerMinX, outerMinY, outerMinZ;
    private final int outerMaxX, outerMaxY, outerMaxZ;

    private final int innerMinX, innerMinY, innerMinZ;
    private final int innerMaxX, innerMaxY, innerMaxZ;

    private final double spawnX, spawnY, spawnZ;
    private final float spawnYaw, spawnPitch;

    public Plot(int id, String worldName,
                int outerMinX, int outerMinY, int outerMinZ,
                int outerMaxX, int outerMaxY, int outerMaxZ,
                int innerMinX, int innerMinY, int innerMinZ,
                int innerMaxX, int innerMaxY, int innerMaxZ,
                double spawnX, double spawnY, double spawnZ,
                float spawnYaw, float spawnPitch) {
        this.id = id;
        this.worldName = worldName;
        this.outerMinX = outerMinX; this.outerMinY = outerMinY; this.outerMinZ = outerMinZ;
        this.outerMaxX = outerMaxX; this.outerMaxY = outerMaxY; this.outerMaxZ = outerMaxZ;
        this.innerMinX = innerMinX; this.innerMinY = innerMinY; this.innerMinZ = innerMinZ;
        this.innerMaxX = innerMaxX; this.innerMaxY = innerMaxY; this.innerMaxZ = innerMaxZ;
        this.spawnX = spawnX; this.spawnY = spawnY; this.spawnZ = spawnZ;
        this.spawnYaw = spawnYaw; this.spawnPitch = spawnPitch;
    }

    public int getId() {
        return id;
    }

    public String getWorldName() {
        return worldName;
    }

    public boolean isInsideOuter(Location loc) {
        if (!loc.getWorld().getName().equals(worldName)) return false;
        int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
        return x >= outerMinX && x <= outerMaxX && y >= outerMinY && y <= outerMaxY && z >= outerMinZ && z <= outerMaxZ;
    }

    public boolean isInsideBuildable(Location loc) {
        if (!loc.getWorld().getName().equals(worldName)) return false;
        int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
        return x >= innerMinX && x <= innerMaxX && y >= innerMinY && y <= innerMaxY && z >= innerMinZ && z <= innerMaxZ;
    }

    public void clearBuildable(World world) {
        for (int x = innerMinX; x <= innerMaxX; x++) {
            for (int y = innerMinY; y <= innerMaxY; y++) {
                for (int z = innerMinZ; z <= innerMaxZ; z++) {
                    world.getBlockAt(x, y, z).setType(org.bukkit.Material.AIR, false);
                }
            }
        }
    }

    public void fill(World world, org.bukkit.Material material) {
        for (int x = innerMinX; x <= innerMaxX; x++) {
            for (int y = innerMinY; y <= innerMaxY; y++) {
                for (int z = innerMinZ; z <= innerMaxZ; z++) {
                    world.getBlockAt(x, y, z).setType(material, false);
                }
            }
        }
    }

    public Location getSpawnLocation(World world) {
        return new Location(world, spawnX, spawnY, spawnZ, spawnYaw, spawnPitch);
    }

    public Location getCenterAbove() {
        World w = org.bukkit.Bukkit.getWorld(worldName);
        double cx = (outerMinX + outerMaxX) / 2.0 + 0.5;
        double cz = (outerMinZ + outerMaxZ) / 2.0 + 0.5;
        double cy = outerMaxY + 3;
        return new Location(w, cx, cy, cz);
    }

    // getters for outer/inner bounds (used by admin tools / serialization)
    public int getOuterMinX() { return outerMinX; }
    public int getOuterMinY() { return outerMinY; }
    public int getOuterMinZ() { return outerMinZ; }
    public int getOuterMaxX() { return outerMaxX; }
    public int getOuterMaxY() { return outerMaxY; }
    public int getOuterMaxZ() { return outerMaxZ; }
    public int getInnerMinX() { return innerMinX; }
    public int getInnerMinY() { return innerMinY; }
    public int getInnerMinZ() { return innerMinZ; }
    public int getInnerMaxX() { return innerMaxX; }
    public int getInnerMaxY() { return innerMaxY; }
    public int getInnerMaxZ() { return innerMaxZ; }
    public double getSpawnX() { return spawnX; }
    public double getSpawnY() { return spawnY; }
    public double getSpawnZ() { return spawnZ; }
    public float getSpawnYaw() { return spawnYaw; }
    public float getSpawnPitch() { return spawnPitch; }
}

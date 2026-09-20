package Bzbxddbx.customAirdropPlugin.util;

import org.bukkit.Location;

import java.util.Objects;

public final class LocationUtil {

    private LocationUtil() {
    }

    public static boolean isSameBlock(Location first, Location second) {
        if (first == null || second == null) {
            return false;
        }
        return Objects.equals(first.getWorld(), second.getWorld())
                && first.getBlockX() == second.getBlockX()
                && first.getBlockY() == second.getBlockY()
                && first.getBlockZ() == second.getBlockZ();
    }
}
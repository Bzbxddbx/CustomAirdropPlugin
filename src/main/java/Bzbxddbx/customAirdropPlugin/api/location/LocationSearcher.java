package Bzbxddbx.customAirdropPlugin.api.location;

import Bzbxddbx.customAirdropPlugin.config.ConfigSettings;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public interface LocationSearcher {

    default CompletableFuture<Location> findSafeLocation(World world) {
        return CompletableFuture.supplyAsync(() -> {
            var spawn = world.getSpawnLocation();
            var radius = ConfigSettings.defaults().searchRadius();
            var random = ThreadLocalRandom.current();
            for (int attempt = 0; attempt < 32; attempt++) {
                int x = spawn.getBlockX() + random.nextInt(-radius, radius + 1);
                int z = spawn.getBlockZ() + random.nextInt(-radius, radius + 1);
                Block top = world.getHighestBlockAt(x, z);
                Material type = top.getType();
                if (!type.isAir() && type != Material.WATER && type != Material.LAVA) {
                    return top.getLocation();
                }
            }
            return spawn.clone();
        });
    }
}
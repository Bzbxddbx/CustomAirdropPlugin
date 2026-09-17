package Bzbxddbx.customAirdropPlugin.api.location;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.concurrent.CompletableFuture;

public interface LocationSearcher {

    CompletableFuture<Location> findSafeLocation(World world);
}
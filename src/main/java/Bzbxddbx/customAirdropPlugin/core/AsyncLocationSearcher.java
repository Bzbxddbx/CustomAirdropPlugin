package Bzbxddbx.customAirdropPlugin.core;

import Bzbxddbx.customAirdropPlugin.api.location.LocationSearcher;
import Bzbxddbx.customAirdropPlugin.config.ConfigSettings;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Поиск безопасной точки: координаты-кандидаты генерируются асинхронно (чистая
 * математика), чтение блоков и возврат результата — строго на главном потоке,
 * чтобы не нарушать потокобезопасность мира.
 */
public final class AsyncLocationSearcher implements LocationSearcher {

    private static final int ATTEMPTS = 32;

    private final JavaPlugin plugin;
    private final ConfigSettings settings;

    public AsyncLocationSearcher(JavaPlugin plugin, ConfigSettings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @Override
    public CompletableFuture<Location> findSafeLocation(World world) {
        return CompletableFuture.supplyAsync(() -> this.candidates(world, this.settings.searchRadius()))
                .thenCompose(candidates -> this.resolveOnMainThread(world, candidates));
    }

    private List<Candidate> candidates(World world, int radius) {
        var random = ThreadLocalRandom.current();
        Location spawn = world.getSpawnLocation();
        List<Candidate> candidates = new ArrayList<>(ATTEMPTS);
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            int x = spawn.getBlockX() + random.nextInt(-radius, radius + 1);
            int z = spawn.getBlockZ() + random.nextInt(-radius, radius + 1);
            candidates.add(new Candidate(x, z));
        }
        return candidates;
    }

    private CompletableFuture<Location> resolveOnMainThread(World world, List<Candidate> candidates) {
        CompletableFuture<Location> result = new CompletableFuture<>();
        this.plugin.getServer().getGlobalRegionScheduler().run(this.plugin, task -> {
            for (Candidate candidate : candidates) {
                Block top = world.getHighestBlockAt(candidate.x(), candidate.z());
                Material type = top.getType();
                if (type.isSolid() && type != Material.WATER && type != Material.LAVA) {
                    result.complete(top.getLocation().clone().add(0, 1, 0));
                    return;
                }
            }
            result.complete(world.getSpawnLocation().clone().add(0, 1, 0));
        });
        return result;
    }

    private record Candidate(int x, int z) {
    }
}
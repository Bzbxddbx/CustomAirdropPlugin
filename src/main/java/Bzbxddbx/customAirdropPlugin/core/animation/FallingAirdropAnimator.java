package Bzbxddbx.customAirdropPlugin.core.animation;

import Bzbxddbx.customAirdropPlugin.config.Messages;
import Bzbxddbx.customAirdropPlugin.core.hologram.AirdropHologram;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.FallingBlock;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Анимация падения сундука с неба: FallingBlock с blockdata CHEST, движущаяся
 * голограмма и след частиц. Приземление перехватывается через
 * {@link FallingBlockRegistry}; при таймауте или «пролёте» мимо цели происходит
 * форс-спавн. {@link #close()} отменяет незавершённую анимацию без утечек.
 */
public final class FallingAirdropAnimator implements AutoCloseable {

    @FunctionalInterface
    public interface LandCallback {
        void onLand(Location location, AirdropHologram hologram);
    }

    private final JavaPlugin plugin;
    private final GlobalRegionScheduler scheduler;
    private final FallingBlockRegistry registry;
    private final Location target;
    private final int fallDistance;
    private final long timeoutTicks;
    private final Messages messages;
    private final LandCallback callback;

    private FallingBlock fallingBlock;
    private AirdropHologram hologram;
    private ScheduledTask tickTask;
    private ScheduledTask watchdogTask;
    private UUID fallingBlockId;
    private boolean landed;

    public FallingAirdropAnimator(JavaPlugin plugin, GlobalRegionScheduler scheduler, FallingBlockRegistry registry,
                                  Location target, int fallDistance, long timeoutTicks, Messages messages,
                                  LandCallback callback) {
        this.plugin = plugin;
        this.scheduler = scheduler;
        this.registry = registry;
        this.target = target.getBlock().getLocation();
        this.fallDistance = fallDistance;
        this.timeoutTicks = timeoutTicks;
        this.messages = messages;
        this.callback = callback;
    }

    public UUID getFallingBlockId() {
        return this.fallingBlockId;
    }

    public void start() {
        World world = this.target.getWorld();
        int spawnY = FallPath.spawnY(world.getMaxHeight(), this.target.getBlockY(), this.fallDistance);
        Location start = new Location(world, this.target.getX() + 0.5, spawnY, this.target.getZ() + 0.5);
        this.fallingBlock = world.spawn(start, FallingBlock.class, entity -> {
            entity.setBlockData(Material.CHEST.createBlockData());
            entity.setDropItem(false);
            entity.setHurtEntities(false);
        });
        this.fallingBlockId = this.fallingBlock.getUniqueId();
        this.hologram = new AirdropHologram(world, start, this.messages.render("hologram.active"));
        this.registry.register(this);
        this.tickTask = this.scheduler.runAtFixedRate(this.plugin, task -> this.tick(), 1L, 2L);
        this.watchdogTask = this.scheduler.runDelayed(this.plugin, task -> this.forceLand(), this.timeoutTicks);
    }

    private void tick() {
        if (this.fallingBlock == null || !this.fallingBlock.isValid()) {
            this.forceLand();
            return;
        }
        Location current = this.fallingBlock.getLocation();
        this.hologram.moveTo(current);
        current.getWorld().spawnParticle(Particle.FLAME, current.clone().add(0, 0.5, 0),
                6, 0.25, 0.25, 0.25, 0.01);
        if (current.getBlockY() <= this.target.getBlockY()) {
            this.forceLand();
        }
    }

    void land(Location location) {
        if (this.landed) {
            return;
        }
        this.landed = true;
        this.cancelTasks();
        this.registry.unregister(this);
        this.removeFallingBlock();
        this.callback.onLand(location.getBlock().getLocation(), this.hologram);
    }

    private void forceLand() {
        if (this.landed) {
            return;
        }
        this.removeFallingBlock();
        this.land(this.target);
    }

    private void removeFallingBlock() {
        if (this.fallingBlock != null && this.fallingBlock.isValid()) {
            this.fallingBlock.remove();
        }
        this.fallingBlock = null;
    }

    private void cancelTasks() {
        if (this.tickTask != null) {
            this.tickTask.cancel();
            this.tickTask = null;
        }
        if (this.watchdogTask != null) {
            this.watchdogTask.cancel();
            this.watchdogTask = null;
        }
    }

    @Override
    public void close() {
        if (this.landed) {
            return;
        }
        this.landed = true;
        this.cancelTasks();
        this.registry.unregister(this);
        this.removeFallingBlock();
        if (this.hologram != null) {
            this.hologram.close();
        }
        this.hologram = null;
    }
}
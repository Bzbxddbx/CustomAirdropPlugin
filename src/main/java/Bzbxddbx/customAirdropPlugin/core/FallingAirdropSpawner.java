package Bzbxddbx.customAirdropPlugin.core;

import Bzbxddbx.customAirdropPlugin.api.Airdrop;
import Bzbxddbx.customAirdropPlugin.api.loot.AirdropSpawner;
import Bzbxddbx.customAirdropPlugin.api.loot.LootProvider;
import Bzbxddbx.customAirdropPlugin.config.ConfigSettings;
import Bzbxddbx.customAirdropPlugin.config.Messages;
import Bzbxddbx.customAirdropPlugin.core.animation.FallingAirdropAnimator;
import Bzbxddbx.customAirdropPlugin.core.animation.FallingBlockRegistry;
import Bzbxddbx.customAirdropPlugin.core.fx.AirdropEffects;
import Bzbxddbx.customAirdropPlugin.core.hologram.AirdropHologram;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

/**
 * Режим «падение с неба»: запускает анимацию и завершает future только после
 * приземления. {@link #dispose()} отменяет незавершённую анимацию и ломает
 * future через {@link CancellationException}.
 */
public final class FallingAirdropSpawner implements AirdropSpawner {

    private final JavaPlugin plugin;
    private final GlobalRegionScheduler scheduler;
    private final FallingBlockRegistry registry;
    private final LootProvider lootProvider;
    private final AirdropEffects effects;
    private final Messages messages;
    private final ConfigSettings settings;

    private CompletableFuture<Airdrop> pendingFuture;
    private FallingAirdropAnimator currentAnimator;

    public FallingAirdropSpawner(JavaPlugin plugin, GlobalRegionScheduler scheduler, FallingBlockRegistry registry,
                                 LootProvider lootProvider, AirdropEffects effects, Messages messages,
                                 ConfigSettings settings) {
        this.plugin = plugin;
        this.scheduler = scheduler;
        this.registry = registry;
        this.lootProvider = lootProvider;
        this.effects = effects;
        this.messages = messages;
        this.settings = settings;
    }

    @Override
    public CompletableFuture<Airdrop> spawn(Location target) {
        CompletableFuture<Airdrop> future = new CompletableFuture<>();
        this.pendingFuture = future;
        FallingAirdropAnimator animator = new FallingAirdropAnimator(
                this.plugin, this.scheduler, this.registry,
                target, this.settings.fallDistance(), this.settings.fallTimeoutTicks(), this.messages,
                (landingLocation, hologram) -> this.complete(landingLocation, hologram, future));
        this.currentAnimator = animator;
        animator.start();
        return future;
    }

    private void complete(Location location, AirdropHologram hologram, CompletableFuture<Airdrop> future) {
        if (future.isCancelled() || future.isCompletedExceptionally()) {
            hologram.close();
            return;
        }
        ActiveAirdrop airdrop = new ActiveAirdrop(location, this.lootProvider, this.effects, this.messages, hologram);
        airdrop.spawn();
        this.currentAnimator = null;
        future.complete(airdrop);
    }

    @Override
    public void dispose() {
        if (this.currentAnimator != null) {
            this.currentAnimator.close();
            this.currentAnimator = null;
        }
        if (this.pendingFuture != null && !this.pendingFuture.isDone()) {
            this.pendingFuture.completeExceptionally(new CancellationException("Airdrop spawn cancelled"));
        }
        this.pendingFuture = null;
    }
}
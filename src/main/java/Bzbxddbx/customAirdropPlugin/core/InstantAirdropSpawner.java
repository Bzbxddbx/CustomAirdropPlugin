package Bzbxddbx.customAirdropPlugin.core;

import Bzbxddbx.customAirdropPlugin.api.Airdrop;
import Bzbxddbx.customAirdropPlugin.api.loot.AirdropSpawner;
import Bzbxddbx.customAirdropPlugin.api.loot.LootProvider;
import Bzbxddbx.customAirdropPlugin.config.Messages;
import Bzbxddbx.customAirdropPlugin.core.fx.AirdropEffects;
import org.bukkit.Location;

import java.util.concurrent.CompletableFuture;

/**
 * Мгновенный режим появления: сундук и голограмма ставятся сразу в точке приземления.
 */
public final class InstantAirdropSpawner implements AirdropSpawner {

    private final LootProvider lootProvider;
    private final AirdropEffects effects;
    private final Messages messages;

    public InstantAirdropSpawner(LootProvider lootProvider, AirdropEffects effects, Messages messages) {
        this.lootProvider = lootProvider;
        this.effects = effects;
        this.messages = messages;
    }

    @Override
    public CompletableFuture<Airdrop> spawn(Location target) {
        ActiveAirdrop airdrop = new ActiveAirdrop(target, this.lootProvider, this.effects, this.messages);
        airdrop.spawn();
        return CompletableFuture.completedFuture(airdrop);
    }
}
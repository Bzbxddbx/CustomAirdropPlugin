package Bzbxddbx.customAirdropPlugin.core;

import Bzbxddbx.customAirdropPlugin.api.Airdrop;
import Bzbxddbx.customAirdropPlugin.api.AirdropState;
import Bzbxddbx.customAirdropPlugin.api.loot.LootProvider;
import Bzbxddbx.customAirdropPlugin.config.Messages;
import Bzbxddbx.customAirdropPlugin.core.fx.AirdropEffects;
import Bzbxddbx.customAirdropPlugin.core.hologram.AirdropHologram;
import Bzbxddbx.customAirdropPlugin.core.loot.SlotPlacer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Доменная модель аирдропа. Отвечает только за конечный автомат состояния
 * (WAITING → ACTIVE → OPENED / удаление) и делегирует визуализацию голограмме,
 * эффектам и раскладке лута.
 */
public final class ActiveAirdrop implements Airdrop {

    private final UUID id;
    private final Location location;
    private final LootProvider lootProvider;
    private final AirdropEffects effects;
    private final Messages messages;
    private AirdropHologram hologram;
    private AirdropState state = AirdropState.WAITING;

    public ActiveAirdrop(Location location, LootProvider lootProvider, AirdropEffects effects, Messages messages) {
        this(location, lootProvider, effects, messages, null);
    }

    /**
     * @param hologram уже существующая голограмма (например, унаследованная от
     *                 анимации падения); при {@code null} создаётся новая.
     */
    public ActiveAirdrop(Location location, LootProvider lootProvider, AirdropEffects effects, Messages messages,
                         AirdropHologram hologram) {
        this.id = UUID.randomUUID();
        this.location = location.getBlock().getLocation();
        this.lootProvider = lootProvider;
        this.effects = effects;
        this.messages = messages;
        this.hologram = hologram;
    }

    @Override
    public UUID getId() {
        return this.id;
    }

    @Override
    public Location getLocation() {
        return this.location.clone();
    }

    @Override
    public AirdropState getState() {
        return this.state;
    }

    @Override
    public void spawn() {
        this.location.getBlock().setType(Material.CHEST);
        if (this.hologram == null) {
            this.hologram = new AirdropHologram(this.location.getWorld(), this.location,
                    this.messages.render("hologram.active"));
        } else {
            this.hologram.setText(this.messages.render("hologram.active"));
        }
        this.state = AirdropState.ACTIVE;
    }

    @Override
    public void open() {
        if (this.state != AirdropState.ACTIVE) {
            return;
        }
        this.effects.playOpen(this.location);
        if (this.location.getBlock().getState() instanceof Chest chest) {
            this.fillChest(chest.getInventory());
        }
        this.hologram.setText(this.messages.render("hologram.opened"));
        this.state = AirdropState.OPENED;
    }

    private void fillChest(Inventory inventory) {
        inventory.clear();
        List<ItemStack> loot = this.lootProvider.provideLoot();
        List<Integer> slots = SlotPlacer.occupy(inventory.getSize(), 10, ThreadLocalRandom.current(), loot.size());
        for (int index = 0; index < slots.size(); index++) {
            inventory.setItem(slots.get(index), loot.get(index));
        }
    }

    /**
     * Re-создаёт голограмму, если она была удалена извне (выгрузка чанка,
     * сторонний плагин). Делает это только при загруженном чанке сундука и
     * возвращает {@code true}, если вывеска была пересоздана.
     */
    public boolean refreshHologramIfMissing() {
        if (this.hologram != null && this.hologram.isValid()) {
            return false;
        }
        World world = this.location.getWorld();
        if (!world.isChunkLoaded(this.location.getBlockX() >> 4, this.location.getBlockZ() >> 4)) {
            return false;
        }
        try {
            this.hologram = new AirdropHologram(world, this.location,
                    this.messages.render(hologramKeyFor(this.state)));
        } catch (RuntimeException e) {
            this.hologram = null;
            return false;
        }
        return true;
    }

    static String hologramKeyFor(AirdropState state) {
        return state == AirdropState.OPENED ? "hologram.opened" : "hologram.active";
    }

    @Override
    public void remove() {
        if (this.hologram != null) {
            this.hologram.close();
        }
        this.hologram = null;
        this.location.getBlock().setType(Material.AIR);
    }
}
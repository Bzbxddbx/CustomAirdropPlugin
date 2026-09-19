package Bzbxddbx.customAirdropPlugin.core;

import Bzbxddbx.customAirdropPlugin.api.Airdrop;
import Bzbxddbx.customAirdropPlugin.core.loot.LootContainer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class ActiveAirdrop implements Airdrop {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final UUID id;
    private final Location location;
    private final LootContainer container;
    private final int minSlots;
    private final int maxSlots;
    private AirdropState state;
    private TextDisplay hologram;

    public ActiveAirdrop(Location location, LootContainer container, int minSlots, int maxSlots) {
        this.id = UUID.randomUUID();
        this.location = location;
        this.container = container;
        this.minSlots = minSlots;
        this.maxSlots = maxSlots;
        this.state = AirdropState.WAITING;
    }

    @Override
    public UUID getId() {
        return this.id;
    }

    @Override
    public Location getLocation() {
        return this.location;
    }

    @Override
    public AirdropState getState() {
        return this.state;
    }

    @Override
    public void spawn() {
        this.location.getBlock().setType(Material.CHEST);
        this.state = AirdropState.ACTIVE;
        World world = this.location.getWorld();
        Location displayLocation = this.location.clone().add(0.5, 1.0, 0.5);
        Component hologramText = MINI_MESSAGE.deserialize(
                "<gold><b>[Мистический сундук]</b></gold>\n<gray>Кликни, чтобы открыть</gray>");
        this.hologram = world.spawn(displayLocation, TextDisplay.class, display -> {
            display.text(hologramText);
            display.setBillboard(Display.Billboard.CENTER);
            display.setLineWidth(400);
            display.setSeeThrough(true);
        });
    }

    @Override
    public void open() {
        if (this.state == AirdropState.OPENED) {
            return;
        }
        World world = this.location.getWorld();
        world.playSound(this.location, Sound.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 1.0f, 1.0f);
        world.spawnParticle(Particle.FLAME, this.location.clone().add(0.5, 0.5, 0.5), 40, 0.5, 0.5, 0.5, 0.05);
        if (this.location.getBlock().getState() instanceof Chest chest) {
            List<ItemStack> loot = this.container.generateRandomLoot(this.minSlots, this.maxSlots);
            List<Integer> slots = new ArrayList<>();
            for (int i = 0; i < 27; i++) {
                slots.add(i);
            }
            Collections.shuffle(slots);
            for (int i = 0; i < loot.size() && i < slots.size(); i++) {
                chest.getInventory().setItem(slots.get(i), loot.get(i));
            }
        }
        if (this.hologram != null && this.hologram.isValid()) {
            this.hologram.text(MINI_MESSAGE.deserialize("<red><b>[ОТКРЫТ]</b></red>"));
        }
        this.state = AirdropState.OPENED;
    }

    @Override
    public void remove() {
        if (this.hologram != null && this.hologram.isValid()) {
            this.hologram.remove();
        }
        this.hologram = null;
        this.location.getBlock().setType(Material.AIR);
    }
}
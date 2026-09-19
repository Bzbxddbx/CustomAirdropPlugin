package Bzbxddbx.customAirdropPlugin.core;

import Bzbxddbx.customAirdropPlugin.api.Airdrop;
import Bzbxddbx.customAirdropPlugin.api.loot.LootProvider;
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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class ActiveAirdrop implements Airdrop {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final UUID id;
    private final Location location;
    private final LootProvider lootProvider;
    private AirdropState state;
    private TextDisplay hologram;

    public ActiveAirdrop(Location location, LootProvider lootProvider) {
        this.id = UUID.randomUUID();
        this.location = location;
        this.lootProvider = lootProvider;
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
        this.hologram = world.spawn(displayLocation, TextDisplay.class, display -> {
            display.text(MINI_MESSAGE.deserialize(
                    "<gold><b>[Мистический сундук]</b></gold>\n<gray>Кликни, чтобы открыть</gray>"));
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
            chest.getInventory().clear();
            List<ItemStack> loot = this.lootProvider.provideLoot();
            ThreadLocalRandom random = ThreadLocalRandom.current();
            List<Integer> occupied = new ArrayList<>();
            for (ItemStack stack : loot) {
                int slot = this.findFreeSlot(random, occupied);
                if (slot >= 0) {
                    chest.getInventory().setItem(slot, stack);
                }
            }
        }
        if (this.hologram != null && this.hologram.isValid()) {
            this.hologram.text(MINI_MESSAGE.deserialize("<red><b>[ОТКРЫТ]</b></red>"));
        }
        this.state = AirdropState.OPENED;
    }

    private int findFreeSlot(ThreadLocalRandom random, List<Integer> occupied) {
        for (int attempt = 0; attempt < 10; attempt++) {
            int slot = random.nextInt(27);
            if (!occupied.contains(slot)) {
                occupied.add(slot);
                return slot;
            }
        }
        return -1;
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
package Bzbxddbx.customAirdropPlugin.core;

import Bzbxddbx.customAirdropPlugin.api.Airdrop;
import Bzbxddbx.customAirdropPlugin.config.ConfigSettings;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class ActiveAirdrop implements Airdrop {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private final UUID id;
    private final Location location;
    private AirdropState state;
    private TextDisplay textDisplay;

    public ActiveAirdrop(UUID id, Location location, AirdropState state) {
        this.id = id;
        this.location = location;
        this.state = state;
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

    public void setState(AirdropState state) {
        this.state = state;
    }

    public TextDisplay getTextDisplay() {
        return this.textDisplay;
    }

    public void setTextDisplay(TextDisplay textDisplay) {
        this.textDisplay = textDisplay;
    }

    @Override
    public void spawn() {
        this.location.getBlock().setType(Material.CHEST);
        World world = this.location.getWorld();
        Location displayLocation = this.location.clone().add(0.5, 1.2, 0.5);
        this.textDisplay = world.spawn(displayLocation, TextDisplay.class, display -> {
            display.text(LEGACY.deserialize("§6[Мистический сундук]§r Кликни для открытия"));
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
        Location displayLocation = this.location.clone().add(0.5, 1.0, 0.5);
        world.spawnParticle(Particle.FLAME, displayLocation, 40, 0.5, 0.5, 0.5, 0.05);
        world.playSound(this.location, Sound.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 1.0f, 1.0f);
        if (this.textDisplay != null && this.textDisplay.isValid()) {
            this.textDisplay.text(LEGACY.deserialize("§cОТКРЫТ"));
        }
        if (this.location.getBlock().getState() instanceof Chest chest) {
            List<Material> loot = ConfigSettings.defaults().loot();
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int amount = random.nextInt(3, 7);
            for (int i = 0; i < amount; i++) {
                Material material = loot.get(random.nextInt(loot.size()));
                ItemStack stack = new ItemStack(material, random.nextInt(1, 6));
                chest.getInventory().setItem(random.nextInt(27), stack);
            }
        }
        this.state = AirdropState.OPENED;
    }

    @Override
    public void remove() {
        if (this.textDisplay != null && this.textDisplay.isValid()) {
            this.textDisplay.remove();
        }
        this.textDisplay = null;
        this.location.getBlock().setType(Material.AIR);
        this.state = AirdropState.WAITING;
    }
}
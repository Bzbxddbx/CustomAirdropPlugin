package Bzbxddbx.customAirdropPlugin.core.fx;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;

/**
 * Звуковое и визуальное оформление игровых действий (открытие сундука).
 */
public final class AirdropEffects {

    public void playOpen(Location location) {
        World world = location.getWorld();
        world.playSound(location, Sound.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 1.0f, 1.0f);
        world.spawnParticle(Particle.FLAME, location.clone().add(0.5, 0.5, 0.5), 40, 0.5, 0.5, 0.5, 0.05);
    }
}
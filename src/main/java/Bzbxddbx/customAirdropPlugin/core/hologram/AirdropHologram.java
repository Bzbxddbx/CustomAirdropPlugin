package Bzbxddbx.customAirdropPlugin.core.hologram;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;

/**
 * Обёртка над нативным {@link TextDisplay}: создание голограммы над блоком,
 * смена текста, перемещение и удаление. Единственная ответственность — жизненный
 * цикл сущности-голограммы.
 */
public final class AirdropHologram implements AutoCloseable {

    private final TextDisplay display;

    public AirdropHologram(World world, Location anchor, Component text) {
        Location position = anchor.clone().add(0.5, 1.0, 0.5);
        this.display = world.spawn(position, TextDisplay.class, spawn -> {
            spawn.text(text);
            spawn.setBillboard(Display.Billboard.CENTER);
            spawn.setLineWidth(400);
            spawn.setSeeThrough(true);
        });
    }

    public boolean isValid() {
        return this.display != null && this.display.isValid();
    }

    public void setText(Component text) {
        if (this.isValid()) {
            this.display.text(text);
        }
    }

    public void moveTo(Location anchor) {
        if (this.isValid()) {
            this.display.teleport(anchor.clone().add(0.5, 1.0, 0.5));
        }
    }

    @Override
    public void close() {
        if (this.isValid()) {
            this.display.remove();
        }
    }
}
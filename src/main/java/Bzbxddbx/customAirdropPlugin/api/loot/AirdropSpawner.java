package Bzbxddbx.customAirdropPlugin.api.loot;

import Bzbxddbx.customAirdropPlugin.api.Airdrop;
import org.bukkit.Location;

import java.util.concurrent.CompletableFuture;

/**
 * Стратегия появления аирдропа в мире. Реализации могут размещать сундук
 * мгновенно или через анимацию (падение с неба). {@link #dispose()} отменяет
 * текущую незавершённую попытку спавна (используется менеджером при остановке).
 */
public interface AirdropSpawner {

    CompletableFuture<Airdrop> spawn(Location target);

    default void dispose() {
    }
}
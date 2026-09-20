package Bzbxddbx.customAirdropPlugin.listener;

import Bzbxddbx.customAirdropPlugin.api.Airdrop;
import Bzbxddbx.customAirdropPlugin.api.AirdropManager;
import Bzbxddbx.customAirdropPlugin.api.AirdropState;
import Bzbxddbx.customAirdropPlugin.util.LocationUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Optional;

public class PlayerInteractListener implements Listener {

    private final AirdropManager airdropManager;

    public PlayerInteractListener(AirdropManager airdropManager) {
        this.airdropManager = airdropManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.CHEST) {
            return;
        }
        Optional<Airdrop> maybeAirdrop = this.airdropManager.getActiveAirdrop();
        if (maybeAirdrop.isEmpty()) {
            return;
        }
        Airdrop airdrop = maybeAirdrop.get();
        if (airdrop.getState() != AirdropState.ACTIVE) {
            return;
        }
        if (LocationUtil.isSameBlock(block.getLocation(), airdrop.getLocation())) {
            event.setCancelled(true);
            airdrop.open();
        }
    }
}
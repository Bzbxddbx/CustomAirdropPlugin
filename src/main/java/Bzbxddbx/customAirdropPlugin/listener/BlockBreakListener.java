package Bzbxddbx.customAirdropPlugin.listener;

import Bzbxddbx.customAirdropPlugin.api.Airdrop;
import Bzbxddbx.customAirdropPlugin.api.AirdropManager;
import Bzbxddbx.customAirdropPlugin.config.Messages;
import Bzbxddbx.customAirdropPlugin.util.LocationUtil;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;
import java.util.Optional;

/**
 * Тотальная защита сундука: блок нельзя сломать, он вычёркивается из списка
 * взрывов TNT/криперов и не перемещается/не ломается поршнями.
 */
public final class BlockBreakListener implements Listener {

    private final AirdropManager airdropManager;
    private final Messages messages;

    public BlockBreakListener(AirdropManager airdropManager, Messages messages) {
        this.airdropManager = airdropManager;
        this.messages = messages;
    }

    private Optional<Airdrop> activeAirdrop() {
        return this.airdropManager.getActiveAirdrop();
    }

    private boolean blocksContainAirdrop(List<Block> blocks) {
        Optional<Airdrop> maybeAirdrop = this.activeAirdrop();
        if (maybeAirdrop.isEmpty()) {
            return false;
        }
        Airdrop airdrop = maybeAirdrop.get();
        return blocks.stream().anyMatch(block ->
                LocationUtil.isSameBlock(block.getLocation(), airdrop.getLocation()));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        this.activeAirdrop().ifPresent(airdrop -> {
            if (LocationUtil.isSameBlock(event.getBlock().getLocation(), airdrop.getLocation())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(this.messages.render("block.cannot-break"));
            }
        });
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        this.filterExplosion(event.blockList());
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        this.filterExplosion(event.blockList());
    }

    private void filterExplosion(List<Block> blocks) {
        this.activeAirdrop().ifPresent(airdrop ->
                blocks.removeIf(block ->
                        LocationUtil.isSameBlock(block.getLocation(), airdrop.getLocation())));
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (this.blocksContainAirdrop(event.getBlocks())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (this.blocksContainAirdrop(event.getBlocks())) {
            event.setCancelled(true);
        }
    }
}
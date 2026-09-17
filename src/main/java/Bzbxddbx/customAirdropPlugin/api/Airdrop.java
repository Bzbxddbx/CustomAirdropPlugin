package Bzbxddbx.customAirdropPlugin.api;

import Bzbxddbx.customAirdropPlugin.core.AirdropState;
import org.bukkit.Location;

import java.util.UUID;

public interface Airdrop {

    UUID getId();

    Location getLocation();

    AirdropState getState();

    void spawn();

    void open();

    void remove();
}
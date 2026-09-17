package Bzbxddbx.customAirdropPlugin.core;

import Bzbxddbx.customAirdropPlugin.api.Airdrop;
import org.bukkit.Location;

import java.util.UUID;

public final class ActiveAirdrop implements Airdrop {

    private final UUID id;
    private final Location location;
    private AirdropState state;

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

    @Override
    public void spawn() {
    }

    @Override
    public void open() {
    }

    @Override
    public void remove() {
    }
}
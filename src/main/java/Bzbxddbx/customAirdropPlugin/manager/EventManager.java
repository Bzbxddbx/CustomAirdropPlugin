package Bzbxddbx.customAirdropPlugin.manager;

import Bzbxddbx.customAirdropPlugin.api.Airdrop;
import Bzbxddbx.customAirdropPlugin.api.AirdropManager;

import java.util.Optional;

public class EventManager implements AirdropManager {

    private Airdrop activeAirdrop;

    @Override
    public void startEvent() {
    }

    @Override
    public void stopEvent() {
        this.activeAirdrop = null;
    }

    @Override
    public Optional<Airdrop> getActiveAirdrop() {
        return Optional.ofNullable(this.activeAirdrop);
    }
}
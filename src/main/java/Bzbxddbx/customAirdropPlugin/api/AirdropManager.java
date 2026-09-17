package Bzbxddbx.customAirdropPlugin.api;

import java.util.Optional;

public interface AirdropManager {

    void startEvent();

    void stopEvent();

    Optional<Airdrop> getActiveAirdrop();
}
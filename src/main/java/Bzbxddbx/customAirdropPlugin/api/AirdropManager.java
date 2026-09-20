package Bzbxddbx.customAirdropPlugin.api;

import java.util.Optional;

public interface AirdropManager {

    boolean startEvent();

    void stopEvent();

    Optional<Airdrop> getActiveAirdrop();
}
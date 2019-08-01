package co.rsk.net.statesync;


import co.rsk.net.SyncProcessor;
import org.ethereum.db.BlockStore;

public class StateSyncFactory {

    private final SyncConfiguration syncConfiguration;
    private final SyncProcessor syncProcessor;
    private final PeersInformation peersInformation;
    private final BlockStore blockStore;
    private final boolean stateSyncActive;

    public StateSyncFactory(SyncConfiguration syncConfiguration,
                   SyncProcessor syncProcessor,
                   PeersInformation peersInformation,
                   BlockStore blockStore,
                   boolean stateSyncActive) {
        this.syncConfiguration = syncConfiguration;
        this.syncProcessor = syncProcessor;
        this.peersInformation = peersInformation;
        this.blockStore = blockStore;
        this.stateSyncActive = stateSyncActive;
    }

    StateSyncState newDeciding() {
        StateSyncState constructedState = new DecidingStateSyncState(this,
                syncConfiguration, peersInformation, blockStore, stateSyncActive);
        return constructedState.onEnter();
    }

    StateSyncState newDisabled() {
        StateSyncState constructedState = new DisabledStateSyncState(this, syncConfiguration, syncProcessor, peersInformation);
        return constructedState.onEnter();
    }
}
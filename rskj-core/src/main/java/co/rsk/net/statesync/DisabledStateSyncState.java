package co.rsk.net.statesync;


import co.rsk.net.SyncProcessor;

public class DisabledStateSyncState extends BaseStateSyncState {

    private final SyncProcessor syncProcessor;

    public DisabledStateSyncState(Factory factory,
                                  SyncConfiguration syncConfiguration,
                                  SyncProcessor syncProcessor,
                                  PeersInformation peersInformation) {
        super(factory, syncConfiguration, peersInformation);
        this.syncProcessor = syncProcessor;
    }

    public StateSyncState onEnter() {
        return this;
    }
}
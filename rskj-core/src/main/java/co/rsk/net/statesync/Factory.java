package co.rsk.net.statesync;


import co.rsk.net.SyncProcessor;

public class Factory {

    private final SyncConfiguration syncConfiguration;
    private final SyncProcessor syncProcessor;

    public Factory(SyncConfiguration syncConfiguration, SyncProcessor syncProcessor) {
        this.syncConfiguration = syncConfiguration;
        this.syncProcessor = syncProcessor;
    }

    StateSyncState newDisabled() {
        StateSyncState constructedState = new DisabledStateSyncState(this, syncConfiguration, syncProcessor);
        return constructedState.onEnter();
    }
}
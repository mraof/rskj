package co.rsk.net.statesync;

import co.rsk.net.NodeID;
import co.rsk.net.Status;
import org.ethereum.db.BlockStore;

import java.time.Duration;

public class DecidingStateSyncState extends BaseStateSyncState {

    private final BlockStore blockStore;
    private final boolean stateSyncActive;

    public DecidingStateSyncState(Factory factory,
                                  SyncConfiguration syncConfiguration,
                                  PeersInformation peersInformation,
                                  BlockStore blockStore,
                                  boolean stateSyncActive) {
        super(factory, syncConfiguration, peersInformation);
        this.blockStore = blockStore;
        this.stateSyncActive = stateSyncActive;
    }

    @Override
    public StateSyncState newPeerStatus(NodeID peerId, Status status) {
        peersInformation.getOrRegisterPeer(peerId).setStatus(status);
        if (peersInformation.count() >= syncConfiguration.getExpectedPeers()) {
            return tryStartSyncing();
        }
        return this;
    }

    @Override
    public StateSyncState tick(Duration duration) {
        peersInformation.cleanExpired();
        timeElapsed = timeElapsed.plus(duration);
        if (peersInformation.count() > 0 &&
                timeElapsed.compareTo(syncConfiguration.getTimeoutWaitingPeers()) >= 0) {

            return tryStartSyncing();
        }
        return this;
    }

    @Override
    public StateSyncState onEnter() {
        if (!stateSyncActive || blockStore.getMaxNumber() > 0) {
            return factory.newDisabled();
        }
        return this;
    }

    private StateSyncState tryStartSyncing() {
        return this;
    }
}
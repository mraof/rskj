package co.rsk.net.statesync;

import co.rsk.net.NodeID;
import co.rsk.net.Status;
import org.ethereum.db.BlockStore;

import java.time.Duration;
import java.util.Optional;

public class DecidingStateSyncState extends BaseStateSyncState {

    private final BlockStore blockStore;
    private final boolean stateSyncActive;

    public DecidingStateSyncState(StateSyncFactory factory,
                                  StateSyncConfiguration syncConfiguration,
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
            return startSyncing();
        }
        return this;
    }

    @Override
    public StateSyncState tick(Duration duration) {
        peersInformation.cleanExpired();
        timeElapsed = timeElapsed.plus(duration);
        if (peersInformation.count() > 0 &&
                timeElapsed.compareTo(syncConfiguration.getTimeoutWaitingPeers()) >= 0) {

            return startSyncing();
        }
        return this;
    }

    @Override
    public StateSyncState onEnter() {
        if (!stateSyncActive) {
            return factory.newDisabled();
        }
        return this;
    }

    private StateSyncState startSyncing() {
        Optional<NodeID> optBestPeer = peersInformation.getBestPeer();
        if (optBestPeer.isPresent()) {
            Status peerStatus = peersInformation.getPeer(optBestPeer.get()).getStatus();
            if (peerStatus.getBestBlockNumber() < syncConfiguration.getbPre() + syncConfiguration.getbPos()) {
                logger.debug("The best peer doesn't have enough blocks to allow for state synchronization");
                return this;
            }

            long checkpoint = peerStatus.getBestBlockNumber() - syncConfiguration.getbPos();
            return factory.newBlocksDownload(optBestPeer.get(), checkpoint);
        }
        return this;
    }
}
package co.rsk.net.statesync;


import co.rsk.crypto.Keccak256;
import co.rsk.net.NodeID;
import co.rsk.net.SyncProcessor;
import co.rsk.trie.TrieStore;
import org.ethereum.db.BlockStore;
import org.ethereum.net.server.ChannelManager;

public class StateSyncFactory {

    private final StateSyncConfiguration syncConfiguration;
    private final SyncProcessor syncProcessor;
    private final PeersInformation peersInformation;
    private final BlockStore blockStore;
    private final boolean stateSyncActive;
    private final TrieStore trieStore;
    private ChannelManager channelManager;

    public StateSyncFactory(StateSyncConfiguration syncConfiguration,
                            SyncProcessor syncProcessor,
                            PeersInformation peersInformation,
                            BlockStore blockStore,
                            TrieStore trieStore,
                            ChannelManager channelManager,
                            boolean stateSyncActive) {
        this.syncConfiguration = syncConfiguration;
        this.syncProcessor = syncProcessor;
        this.peersInformation = peersInformation;
        this.blockStore = blockStore;
        this.trieStore = trieStore;
        this.channelManager = channelManager;
        this.stateSyncActive = stateSyncActive;
    }

    StateSyncState newDeciding() {
        StateSyncState constructedState = new DecidingStateSyncState(this,
                syncConfiguration, peersInformation, blockStore, stateSyncActive);
        return constructedState.onEnter();
    }

    StateSyncState newDisabled() {
        StateSyncState constructedState = new DisabledStateSyncState(
                this,
                syncConfiguration,
                syncProcessor,
                peersInformation);
        return constructedState.onEnter();
    }

    StateSyncState newDownloadingState(NodeID peerId, Keccak256 stateRoot) {
        StateSyncState constructedState = new StateDownloadSyncState(this,
                syncConfiguration,
                peersInformation,
                channelManager,
                trieStore,
                peerId,
                stateRoot);
        return constructedState.onEnter();
    }

    StateSyncState newBlocksDownload(NodeID peerId, long checkpoint) {
        StateSyncState constructedState = new BlocksDownloadSyncState(
                this,
                syncConfiguration,
                peersInformation,
                channelManager,
                blockStore,
                peerId,
                checkpoint);
        return constructedState.onEnter();
    }
}
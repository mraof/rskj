package co.rsk.net.sync;

import co.rsk.core.bc.ConsensusValidationMainchainView;
import co.rsk.crypto.Keccak256;
import co.rsk.net.NodeID;
import org.ethereum.core.BlockFactory;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.BlockIdentifier;

import java.util.Deque;
import java.util.List;
import java.util.Map;

public class SyncStateFactory {
    private final SyncConfiguration syncConfiguration;
    private final SyncEventsHandler syncEventsHandler;
    private final SyncInformation syncInformation;

    public SyncStateFactory(SyncConfiguration syncConfiguration,
                            SyncEventsHandler syncEventsHandler,
                            SyncInformation syncInformation) {
        this.syncConfiguration = syncConfiguration;
        this.syncEventsHandler = syncEventsHandler;
        this.syncInformation = syncInformation;
    }

    public SyncState newDecidingSyncState(PeersInformation peersInformation) {
        return new DecidingSyncState(syncConfiguration, syncEventsHandler, syncInformation, peersInformation);
    }

    public SyncState newCheckingBestHeaderSyncState(Keccak256 bestBlockHash) {
        return new CheckingBestHeaderSyncState(syncConfiguration,
                syncEventsHandler, syncInformation, bestBlockHash.getBytes());
    }

    public SyncState newFindingConnectionPointSyncState(long bestBlockNumber) {
        return new FindingConnectionPointSyncState(syncConfiguration,
                syncEventsHandler, syncInformation, bestBlockNumber);
    }

    public SyncState newDownloadingSkeletonSyncState(PeersInformation peersInformation,
                                                     long connectionPoint) {
        return new DownloadingSkeletonSyncState(syncConfiguration,
                syncEventsHandler, syncInformation, peersInformation, connectionPoint);
    }

    public SyncState newDownloadingHeadersSyncState(ConsensusValidationMainchainView miningMainchainView,
                                                    Map<NodeID, List<BlockIdentifier>> skeletons,
                                                    long connectionPoint) {
        return new DownloadingHeadersSyncState(syncConfiguration,
                syncEventsHandler, syncInformation, skeletons, connectionPoint, miningMainchainView);
    }

    public SyncState newDownloadingBodiesSyncState(BlockFactory blockFactory,
                                                   List<Deque<BlockHeader>> pendingHeaders,
                                                   Map<NodeID, List<BlockIdentifier>> skeletons) {
        return new DownloadingBodiesSyncState(syncConfiguration,
                syncEventsHandler, syncInformation, blockFactory, pendingHeaders, skeletons);
    }
}

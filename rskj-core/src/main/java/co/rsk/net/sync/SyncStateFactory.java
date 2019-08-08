package co.rsk.net.sync;

import co.rsk.core.bc.ConsensusValidationMainchainView;
import co.rsk.crypto.Keccak256;
import co.rsk.net.BlockSyncService;
import co.rsk.net.NodeID;
import co.rsk.validators.BlockCompositeRule;
import co.rsk.validators.BlockHeaderValidationRule;
import org.ethereum.core.BlockFactory;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.BlockIdentifier;
import org.ethereum.core.Blockchain;
import org.ethereum.validator.DependentBlockHeaderRule;

import java.util.Deque;
import java.util.List;
import java.util.Map;

public class SyncStateFactory {
    private final SyncConfiguration syncConfiguration;
    private final SyncEventsHandler syncEventsHandler;
    private final BlockSyncService blockSyncService;
    private final Blockchain blockchain;
    private final DependentBlockHeaderRule blockParentValidationRule;
    private final BlockHeaderValidationRule blockHeaderValidationRule;
    private final BlockCompositeRule blockValidationRule;
    private final PeersInformation peersInformation;
    private final SyncMessager syncMessager;

    public SyncStateFactory(SyncConfiguration syncConfiguration,
                            SyncEventsHandler syncEventsHandler,
                            PeersInformation peersInformation,
                            BlockSyncService blockSyncService,
                            Blockchain blockchain,
                            BlockHeaderValidationRule blockHeaderValidationRule,
                            DependentBlockHeaderRule blockParentValidationRule,
                            BlockCompositeRule blockValidationRule,
                            SyncMessager syncMessager) {
        this.syncConfiguration = syncConfiguration;
        this.syncEventsHandler = syncEventsHandler;
        this.peersInformation = peersInformation;
        this.blockSyncService = blockSyncService;
        this.blockchain = blockchain;
        this.blockHeaderValidationRule = blockHeaderValidationRule;
        this.blockParentValidationRule = blockParentValidationRule;
        this.blockValidationRule = blockValidationRule;
        this.syncMessager = syncMessager;
    }

    public SyncState newDecidingSyncState() {
        return new DecidingSyncState(syncConfiguration, syncEventsHandler, peersInformation);
    }

    public SyncState newCheckingBestHeaderSyncState(NodeID selectedPeerId, Keccak256 bestBlockHash) {
        return new CheckingBestHeaderSyncState(syncConfiguration, syncEventsHandler, blockHeaderValidationRule,
                selectedPeerId, bestBlockHash.getBytes());
    }

    public SyncState newFindingConnectionPointSyncState(NodeID selectedPeerId, long bestBlockNumber) {
        return new FindingConnectionPointSyncState(syncConfiguration,
                syncEventsHandler, blockchain, selectedPeerId, bestBlockNumber);
    }

    public SyncState newDownloadingSkeletonSyncState(NodeID selectedPeerId,
                                                     long connectionPoint) {
        return new DownloadingSkeletonSyncState(syncConfiguration,
                syncEventsHandler, peersInformation, syncMessager, selectedPeerId, connectionPoint);
    }

    public SyncState newDownloadingHeadersSyncState(ConsensusValidationMainchainView miningMainchainView,
                                                    Map<NodeID, List<BlockIdentifier>> skeletons,
                                                    NodeID selectedPeerId,
                                                    long connectionPoint) {
        return new DownloadingHeadersSyncState(syncConfiguration,
                syncEventsHandler, miningMainchainView, blockParentValidationRule,
                blockHeaderValidationRule, skeletons, selectedPeerId, connectionPoint);
    }

    public SyncState newDownloadingBodiesSyncState(BlockFactory blockFactory,
                                                   List<Deque<BlockHeader>> pendingHeaders,
                                                   Map<NodeID, List<BlockIdentifier>> skeletons) {
        return new DownloadingBodiesSyncState(syncConfiguration,
                syncEventsHandler, peersInformation, blockchain, blockFactory, blockSyncService, blockValidationRule,
                pendingHeaders, skeletons);
    }
}

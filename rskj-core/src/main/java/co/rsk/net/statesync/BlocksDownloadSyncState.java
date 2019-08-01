package co.rsk.net.statesync;

import co.rsk.core.BlockDifficulty;
import co.rsk.net.NodeID;
import org.ethereum.core.Block;
import org.ethereum.db.BlockStore;
import org.ethereum.net.server.ChannelManager;

import java.util.TreeMap;
import java.util.stream.LongStream;

public class BlocksDownloadSyncState extends BaseStateSyncState {

    private final static int BLOCKS_TO_REQUEST = 200;

    private final ChannelManager channelManager;
    private final BlockStore blockStore;
    private final NodeID peerId;
    private final long checkpoint;

    private final TreeMap<Long, Block> expectedBlocks;
    private long windowFrom;
    private BlockDifficulty cummDifficulty;
    private long lastRequestId;


    public BlocksDownloadSyncState(StateSyncFactory factory,
                                   StateSyncConfiguration syncConfiguration,
                                   PeersInformation peersInformation,
                                   ChannelManager channelManager,
                                   BlockStore blockStore,
                                   NodeID peerId,
                                   long checkpoint) {
        super(factory, syncConfiguration, peersInformation);
        this.channelManager = channelManager;
        this.blockStore = blockStore;
        this.peerId = peerId;
        this.checkpoint = checkpoint;

        this.lastRequestId = 0;

        Block bestBlock = this.blockStore.getBestBlock();
        this.windowFrom = bestBlock.getNumber();
        cummDifficulty = BlockDifficulty.ZERO.add(bestBlock.getCumulativeDifficulty());

        this.expectedBlocks = new TreeMap<>();
    }

    @Override
    public StateSyncState newBlock(Block block) {
        if (!expectedBlocks.containsKey(block.getNumber())) {
            logger.debug("Block number: {} not expected", block.getNumber());
            return this;
        }

        expectedBlocks.put(block.getNumber(), block);

        while (expectedBlocks.firstKey() == windowFrom) {
            Block blockToSave = expectedBlocks.pollFirstEntry().getValue();

            cummDifficulty = cummDifficulty.add(block.getCumulativeDifficulty());
            blockStore.saveBlock(blockToSave, cummDifficulty, true);
            windowFrom += 1;
        }
        blockStore.flush();

        if (windowFrom == checkpoint) {
            return factory.newDisabled();
        }

        if (expectedBlocks.isEmpty()) {
            return requestBlocks(windowFrom, BLOCKS_TO_REQUEST);
        }

        return this;
    }

    @Override
    public StateSyncState onEnter() {
        return requestBlocks(windowFrom, BLOCKS_TO_REQUEST);
    }

    private StateSyncState requestBlocks(long from, long count) {
        LongStream.range(from, count)
                .forEach(blockNumber -> expectedBlocks.put(blockNumber, null));
        return this;
    }
}
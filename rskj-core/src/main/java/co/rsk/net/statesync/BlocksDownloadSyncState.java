package co.rsk.net.statesync;

import co.rsk.core.BlockDifficulty;
import co.rsk.net.NodeID;
import co.rsk.net.messages.BlocksRequestMessage;
import org.ethereum.core.Block;
import org.ethereum.db.BlockStore;
import org.ethereum.net.server.ChannelManager;

import java.util.TreeMap;
import java.util.stream.LongStream;

public class BlocksDownloadSyncState extends BaseStateSyncState {

    private final static int BLOCKS_TO_REQUEST = 20;

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

        while (!expectedBlocks.isEmpty()
                && expectedBlocks.firstKey() == windowFrom
                && expectedBlocks.firstEntry().getValue() != null) {
            Block blockToSave = expectedBlocks.pollFirstEntry().getValue();

            cummDifficulty = cummDifficulty.add(block.getCumulativeDifficulty());
            blockStore.saveBlock(blockToSave, cummDifficulty, true);
            windowFrom += 1;
        }

        if (windowFrom == checkpoint) {
            return factory.newDisabled();
        }

        if (expectedBlocks.isEmpty()) {
            blockStore.flush();
            return requestBlocks(windowFrom, BLOCKS_TO_REQUEST);
        }

        return this;
    }

    @Override
    public StateSyncState onEnter() {
        return requestBlocks(windowFrom, BLOCKS_TO_REQUEST);
    }

    private StateSyncState requestBlocks(long from, int count) {
        BlocksRequestMessage blocksRequestMessage = new BlocksRequestMessage(++lastRequestId, from, count);
        if (!channelManager.sendMessageTo(peerId, blocksRequestMessage)) {
            logger.debug("Error when sending blocks request message from block {} to {} to node {}",
                    from, from + count, peerId);
            return factory.newDeciding();
        }

        LongStream.range(from, from + count)
                .forEach(blockNumber -> expectedBlocks.put(blockNumber, null));
        return this;
    }
}
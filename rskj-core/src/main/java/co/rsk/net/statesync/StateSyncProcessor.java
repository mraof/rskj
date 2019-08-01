package co.rsk.net.statesync;

import co.rsk.net.NodeID;
import co.rsk.net.Status;
import co.rsk.trie.Trie;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

public class StateSyncProcessor {

    private static final Logger logger = LoggerFactory.getLogger(StateSyncProcessor.class);

    private StateSyncState currentState;

    public StateSyncProcessor(StateSyncFactory factory) {
        currentState = factory.newDeciding();
    }

    public void tick(Duration duration) {
        currentState = currentState.tick(duration);
    }


    public void newTrieNode(NodeID peerId, long requestId, Trie trieNode) {
        logger.debug("Processing Trie Node response from node {}", peerId);
        currentState = currentState.newTrieNode(peerId, requestId, trieNode);
    }

    public void newBlock(NodeID peerId, Block block) {
        logger.debug("Processing Block response from node {}", peerId);
        currentState = currentState.newBlock(block);
    }

    public void newPeerStatus(NodeID peerId, Status status) {
        currentState = currentState.newPeerStatus(peerId, status);
    }
}
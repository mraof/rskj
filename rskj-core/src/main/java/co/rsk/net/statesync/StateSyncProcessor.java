package co.rsk.net.statesync;

import co.rsk.net.NodeID;
import co.rsk.net.Status;
import co.rsk.trie.Trie;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

public class StateSyncProcessor {

    private static final Logger logger = LoggerFactory.getLogger(StateSyncProcessor.class);

    private StateSyncState currentState;

    public StateSyncProcessor(Factory factory) {
    }

    public void tick(Duration duration) {
        currentState = currentState.tick(duration);
    }

    public void newBlockHeaders(NodeID nodeID, List<BlockHeader> blockHeaders) {
        logger.debug("Processing block headers response from node {}", nodeID);
        currentState = currentState.newBlockHeaders(blockHeaders);
    }

    public void newTrieNode(NodeID peerId, long requestId, Trie trieNode) {
        logger.debug("Processing Trie Node response from node {}", peerId);
        currentState = currentState.newTrieNode(peerId, requestId, trieNode);
    }

    public void newBlock(NodeID peerId, long requestId, List<Transaction> transactions, List<BlockHeader> uncles) {
        logger.debug("Processing Block response from node {}", peerId);
        currentState = currentState.newBody(peerId, requestId, transactions, uncles);
    }

    public void newPeerStatus(NodeID peerId, Status status) {
        currentState = currentState.newPeerStatus(peerId, status);
    }
}
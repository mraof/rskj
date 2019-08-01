package co.rsk.net.statesync;


import co.rsk.crypto.Keccak256;
import co.rsk.net.NodeID;
import co.rsk.trie.NodeReference;
import co.rsk.trie.Trie;
import co.rsk.trie.TrieStore;
import org.ethereum.net.server.ChannelManager;

import java.util.*;

public class StateDownloadSyncState extends BaseStateSyncState {

    private final ChannelManager channelManager;
    private final TrieStore trieStore;
    private final NodeID peerId;
    private final Keccak256 stateRoot;
    private Stack<Keccak256> toRetrieve;
    private Set<Keccak256> requestedNodes;
    private long lastRequestId;

    public StateDownloadSyncState(StateSyncFactory factory,
                                  StateSyncConfiguration syncConfiguration,
                                  PeersInformation peersInformation,
                                  ChannelManager channelManager,
                                  TrieStore trieStore,
                                  NodeID peerId,
                                  Keccak256 stateRoot) {
        super(factory, syncConfiguration, peersInformation);
        this.channelManager = channelManager;
        this.trieStore = trieStore;
        this.peerId = peerId;
        this.stateRoot = stateRoot;

        this.lastRequestId = 0;
        toRetrieve = new Stack<>();
        requestedNodes = new HashSet<>();
    }

    @Override
    public StateSyncState newTrieNode(NodeID peerId, long requestId, Trie trieNode) {
        logger.debug("Retrieved Node {}", trieNode.getHash().toHexString());

        Keccak256 receivedNodeHash = trieNode.getHash();
        if (!requestedNodes.contains(receivedNodeHash)) {
            logger.debug("Node {} was not expected, ignoring", receivedNodeHash);
            return this;
        }

        trieStore.saveNode(trieNode);
        requestedNodes.remove(receivedNodeHash);

        addIfNotKnown(trieNode.getLeft(), receivedNodeHash);
        addIfNotKnown(trieNode.getRight(), receivedNodeHash);

        if (toRetrieve.isEmpty()) {
            trieStore.flush();
            return factory.newDeciding();
        }

        Keccak256 newRequest = toRetrieve.pop();

        return requestNode(peerId, newRequest, newRequest.getBytes());
    }

    private void addIfNotKnown(NodeReference node, Keccak256 receivedNodeHash) {
        if (node.isEmbeddable()) {
            return;
        }

        node.getHash().ifPresent(
                child -> {
                    logger.debug("{} to retrieve, child of {}", child, receivedNodeHash);
                    toRetrieve.push(child);
                });
    }

    public StateSyncState onEnter() {
        logger.debug("Synchronizing state");
        return requestNode(peerId, stateRoot, this.stateRoot.getBytes());
    }

    private StateSyncState requestNode(NodeID peerId, Keccak256 hashToRequest, byte[] bytes) {
        return this;
    }
}
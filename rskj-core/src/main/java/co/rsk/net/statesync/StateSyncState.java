package co.rsk.net.statesync;

import co.rsk.net.NodeID;
import co.rsk.net.Status;
import co.rsk.trie.Trie;
import org.ethereum.core.Block;

import java.time.Duration;

public interface StateSyncState {

    StateSyncState newPeerStatus(NodeID peerId, Status status);

    StateSyncState tick(Duration duration);

    StateSyncState newTrieNode(NodeID peerId, long requestId, Trie trieNode);

    StateSyncState newBlock(Block block);

    StateSyncState onEnter();
}
package co.rsk.net.statesync;

import co.rsk.net.NodeID;
import co.rsk.net.Status;
import co.rsk.trie.Trie;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.Transaction;

import java.time.Duration;
import java.util.List;

public interface StateSyncState {

    StateSyncState newPeerStatus(NodeID peerId, Status status);

    StateSyncState tick(Duration duration);

    StateSyncState newTrieNode(NodeID peerId, long requestId, Trie trieNode);

    StateSyncState newBlockHeaders(List<BlockHeader> chunk);

    StateSyncState newBody(NodeID peerId,
                           long requestId,
                           List<Transaction> transactions,
                           List<BlockHeader> uncles);

    StateSyncState onEnter();
}
package co.rsk.net.statesync;

import co.rsk.core.BlockDifficulty;
import co.rsk.core.bc.BlockChainStatus;
import co.rsk.net.NodeID;
import co.rsk.net.Status;
import co.rsk.net.sync.SyncPeerStatus;
import co.rsk.scoring.PeerScoringManager;
import org.ethereum.core.Blockchain;
import org.ethereum.net.server.Channel;
import org.ethereum.net.server.ChannelManager;

import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PeersInformation {

    private static final int MAX_SIZE_FAILURE_RECORDS = 10;

    private final ChannelManager channelManager;
    private final StateSyncConfiguration syncConfiguration;
    private final PeerScoringManager peerScoringManager;
    private final Blockchain blockchain;
    private final Comparator<Map.Entry<NodeID, SyncPeerStatus>> peerComparator;
    private Map<NodeID, SyncPeerStatus> peerStatuses = new HashMap<>();
    private final Map<NodeID, Instant> failedPeers;

    public PeersInformation(ChannelManager channelManager,
                            StateSyncConfiguration syncConfiguration,
                            PeerScoringManager peerScoringManager,
                            Blockchain blockchain){
        this.channelManager = channelManager;
        this.syncConfiguration = syncConfiguration;
        this.peerScoringManager = peerScoringManager;
        this.blockchain = blockchain;
        this.peerComparator = ((Comparator<Map.Entry<NodeID, SyncPeerStatus>>) this::comparePeerFailInstant)
                .thenComparing(this::comparePeerTotalDifficulty);
        this.failedPeers = new LinkedHashMap<NodeID, Instant>(MAX_SIZE_FAILURE_RECORDS, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<NodeID, Instant> eldest) {
                return size() > MAX_SIZE_FAILURE_RECORDS;
            }
        };

    }

    public int count() {
        return peerStatuses.size();
    }

    public int countIf(Predicate<SyncPeerStatus> predicate) {
        long count = peerStatuses.values().stream()
                .filter(predicate)
                .count();
        return Math.toIntExact(count);
    }

    public SyncPeerStatus getOrRegisterPeer(NodeID nodeID) {
        SyncPeerStatus peerStatus = this.peerStatuses.get(nodeID);

        if (peerStatus != null && peerNotExpired(peerStatus)) {
            return peerStatus;
        }

        return this.registerPeer(nodeID);
    }

    public SyncPeerStatus getPeer(NodeID nodeID) {
        return this.peerStatuses.get(nodeID);
    }

    public Optional<NodeID> getBestPeer() {
        return getCandidatesStream()
                .max(this.peerComparator)
                .map(Map.Entry::getKey);
    }

    private Stream<Map.Entry<NodeID, SyncPeerStatus>> getCandidatesStream(){
        Set<NodeID> activeNodes = channelManager.getActivePeers().stream()
                .map(Channel::getNodeId).collect(Collectors.toSet());

        return peerStatuses.entrySet().stream()
                .filter(e -> peerNotExpired(e.getValue()))
                .filter(e -> activeNodes.contains(e.getKey()))
                .filter(e -> peerScoringManager.hasGoodReputation(e.getKey()))
                .filter(e -> hasLowerDifficulty(e.getKey()));
    }

    public List<NodeID> getPeerCandidates() {
        return getCandidatesStream().map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public Set<NodeID> knownNodeIds() {
        return peerStatuses.keySet();
    }

    public SyncPeerStatus registerPeer(NodeID nodeID) {
        SyncPeerStatus peerStatus = new SyncPeerStatus();
        peerStatuses.put(nodeID, peerStatus);
        return peerStatus;
    }

    public void cleanExpired() {
        peerStatuses = peerStatuses.entrySet().stream()
                .filter(e -> peerNotExpired(e.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private boolean peerNotExpired(SyncPeerStatus peer) {
        return !peer.isExpired(syncConfiguration.getExpirationTimePeerStatus());
    }

    private int comparePeerFailInstant(
            Map.Entry<NodeID, SyncPeerStatus> entry,
            Map.Entry<NodeID, SyncPeerStatus> other) {
        Instant failInstant = getFailInstant(entry.getKey());
        Instant otherFailInstant = getFailInstant(other.getKey());
        // note that this is in inverse order
        return otherFailInstant.compareTo(failInstant);
    }

    private int comparePeerScoring(
            Map.Entry<NodeID, SyncPeerStatus> entry,
            Map.Entry<NodeID, SyncPeerStatus> other) {
        int score = peerScoringManager.getPeerScoring(entry.getKey()).getScore();
        int scoreOther = peerScoringManager.getPeerScoring(other.getKey()).getScore();
        // Treats all non-negative scores the same for calculating the best peer
        if (score >= 0 && scoreOther >= 0) {
            return 0;
        }

        return Integer.compare(score, scoreOther);
    }

    private int comparePeerTotalDifficulty(
            Map.Entry<NodeID, SyncPeerStatus> entry,
            Map.Entry<NodeID, SyncPeerStatus> other) {
        BlockDifficulty ttd = entry.getValue().getStatus().getTotalDifficulty();
        BlockDifficulty otd = other.getValue().getStatus().getTotalDifficulty();

        // status messages from outdated nodes might have null difficulties
        if (ttd == null && otd == null) {
            return 0;
        }

        if (ttd == null) {
            return -1;
        }

        if (otd == null) {
            return 1;
        }

        return ttd.compareTo(otd);
    }

    private Instant getFailInstant(NodeID peerId) {
        Instant instant = failedPeers.get(peerId);
        if (instant != null){
            return instant;
        }
        return Instant.EPOCH;
    }

    private boolean hasLowerDifficulty(NodeID nodeID) {
        Status status = peerStatuses.get(nodeID).getStatus();
        if (status == null) {
            return false;
        }

        boolean hasTotalDifficulty = status.getTotalDifficulty() != null;
        BlockChainStatus nodeStatus = blockchain.getStatus();
        // this works only for testing purposes, real status without difficulty don't reach this far
        return  (hasTotalDifficulty && nodeStatus.hasLowerTotalDifficultyThan(status)) ||
                (!hasTotalDifficulty && nodeStatus.getBestBlockNumber() < status.getBestBlockNumber());
    }
}
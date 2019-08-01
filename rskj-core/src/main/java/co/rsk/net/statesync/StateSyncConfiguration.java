package co.rsk.net.statesync;

import javax.annotation.concurrent.Immutable;
import java.time.Duration;

@Immutable
public final class StateSyncConfiguration {

    private final int expectedPeers;
    private final Duration timeoutWaitingPeers;
    private final Duration timeoutWaitingRequest;
    private final int bPre;
    private final int bPos;
    private final Duration expirationTimePeerStatus;

    /**
     * @param expectedPeers The expected number of peers we would want to start finding a connection point.
     * @param timeoutWaitingPeers Timeout in minutes to start finding the connection point when we have at least one peer
     * @param timeoutWaitingRequest Timeout in seconds to wait for syncing requests
     * @param expirationTimePeerStatus Expiration time in minutes for peer status
     */
    public StateSyncConfiguration(int expectedPeers,
                                  int timeoutWaitingPeers,
                                  int timeoutWaitingRequest,
                                  int expirationTimePeerStatus,
                                  int bPre,
                                  int bPos) {
        this.expectedPeers = expectedPeers;
        this.timeoutWaitingPeers = Duration.ofSeconds(timeoutWaitingPeers);
        this.timeoutWaitingRequest = Duration.ofSeconds(timeoutWaitingRequest);
        this.expirationTimePeerStatus = Duration.ofSeconds(expirationTimePeerStatus);
        this.bPre = bPre;
        this.bPos = bPos;
    }

    public final int getExpectedPeers() {
        return expectedPeers;
    }

    public final Duration getTimeoutWaitingPeers() {
        return timeoutWaitingPeers;
    }

    public final Duration getTimeoutWaitingRequest() {
        return  timeoutWaitingRequest;
    }

    public final Duration getExpirationTimePeerStatus() {
        return expirationTimePeerStatus;
    }

    public int getbPre() {
        return bPre;
    }

    public int getbPos() {
        return bPos;
    }
}
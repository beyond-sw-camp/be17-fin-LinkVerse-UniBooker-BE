package org.example.apiqueue.domain.port.out;

import java.util.List;
import java.util.Set;

public interface QueueRepository {
    Long addToWaitQueue(Long resourceId, String token, Long userId);
    void activateNext(Long resourceId, long count);
    boolean isActive(Long resourceId, String token);
    Long rankInWait(Long resourceId, String token); // 0-based, null 가능
    Long waitSize(Long resourceId);
    Long activeSize(Long resourceId);
    void markConsumed(Long resourceId, String token);
    long expireOldActives(Long resourceId, long olderThanEpochSec);
    List<TokenView> listWait(Long resourceId, long offset, long limit);
    List<TokenView> listActive(Long resourceId, long offset, long limit);
    Set<Long> findResourcesWithWaiters();
    Set<Long> findResourcesWithActives();

    // DTO
    public static final class TokenView {
        public final String token;
        public final Long userId;
        public final String state;       // WAITING/ACTIVE/EXPIRED/CONSUMED
        public final Long position;      // 대기열 위치(1부터), ACTIVE에선 null
        public final Long joinedAt;      // epoch sec
        public final Long enteredAt;     // epoch sec (ACTIVE만)
        public TokenView(String token, Long userId, String state, Long position, Long joinedAt, Long enteredAt) {
            this.token = token; this.userId = userId; this.state = state;
            this.position = position; this.joinedAt = joinedAt; this.enteredAt = enteredAt;
        }
    }

}
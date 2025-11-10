package org.example.apiqueue.domain.port.out;

public interface QueueRepository {
    Long addToWaitQueue(Long serviceGroupId, String token, Long userId);
    void activateNext(Long serviceGroupId, long count);
    boolean isActive(Long serviceGroupId, String token);
    Long rankInWait(Long serviceGroupId, String token); // 0-based, null 가능
    Long waitSize(Long serviceGroupId);
    void markConsumed(Long serviceGroupId, String token);
}
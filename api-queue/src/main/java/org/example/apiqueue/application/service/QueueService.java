package org.example.apiqueue.application.service;

import lombok.RequiredArgsConstructor;
import org.example.apiqueue.domain.port.out.QueueRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final QueueRepository repo;

    /** 대기열 합류: 토큰 생성 후 대기열 추가 */
    public JoinResult join(Long serviceGroupId, Long userId) {
        String token = UUID.randomUUID().toString();
        Long position = repo.addToWaitQueue(serviceGroupId, token, userId);
        return new JoinResult(token, position == null ? 1 : position);
    }

    /** 앞 n명 입장 허용 */
    public void promote(Long serviceGroupId, long count) {
        repo.activateNext(serviceGroupId, count);
    }

    /** 현재 토큰이 ACTIVE인지 */
    public boolean isAllowed(Long serviceGroupId, String token) {
        return repo.isActive(serviceGroupId, token);
    }

    /** 대기 상태 조회 */
    public StatusResult status(Long serviceGroupId, String token) {
        Long r0 = repo.rankInWait(serviceGroupId, token);
        Long sz = repo.waitSize(serviceGroupId);
        long position = (r0 == null) ? 0 : r0 + 1;
        long length = (sz == null) ? 0 : sz;
        // 임시 ETA 계산(초): 초당 0.5명 처리 가정
        long etaSeconds = (position == 0) ? 0 : Math.round(position / 0.5);
        return new StatusResult(position, length, etaSeconds);
    }

    /** 소비(사용 완료) */
    public void consume(Long serviceGroupId, String token) {
        repo.markConsumed(serviceGroupId, token);
    }

    // DTOs
    public record JoinResult(String token, long position) {}
    public record StatusResult(long position, long length, long etaSeconds) {}
}
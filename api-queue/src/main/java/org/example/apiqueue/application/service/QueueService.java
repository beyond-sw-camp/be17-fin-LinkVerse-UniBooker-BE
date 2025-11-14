package org.example.apiqueue.application.service;

import lombok.RequiredArgsConstructor;
import org.example.apiqueue.domain.port.out.QueueRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final QueueRepository repo;

    /** 대기열 합류: 토큰 생성 후 대기열 추가 */
    public JoinResult join(Long resourceId, Long userId) {
        String token = UUID.randomUUID().toString();
        Long position = repo.addToWaitQueue(resourceId, token, userId);
        return new JoinResult(token, position == null ? 1 : position);
    }

    /** 앞 n명 입장 허용 */
    public void promote(Long resourceId, long count) {
        repo.activateNext(resourceId, count);
    }

    /** 현재 토큰이 ACTIVE인지 */
    public boolean isAllowed(Long resourceId, String token) {
        return repo.isActive(resourceId, token);
    }

    /** 대기 상태 조회 */
    public StatusResult status(Long resourceId, String token) {
        Long r0 = repo.rankInWait(resourceId, token);
        Long sz = repo.waitSize(resourceId);
        long position = (r0 == null) ? 0 : r0 + 1;
        long length = (sz == null) ? 0 : sz;
        // 임시 ETA 계산(초): 초당 0.5명 처리 가정
        long etaSeconds = (position == 0) ? 0 : Math.round(position / 0.5);
        return new StatusResult(position, length, etaSeconds);
    }

    /** 소비(사용 완료) */
    public void consume(Long resourceId, String token) {
        repo.markConsumed(resourceId, token);
    }

    // DTOs
    public record JoinResult(String token, long position) {}
    public record StatusResult(long position, long length, long etaSeconds) {}

    public List<QueueRepository.TokenView> listWait(Long gid, long offset, long limit) {
        return repo.listWait(gid, offset, limit);
    }
    public List<QueueRepository.TokenView> listActive(Long gid, long offset, long limit) {
        return repo.listActive(gid, offset, limit);
    }

    public long waitSize(Long resourceId) {
        return repo.waitSize(resourceId);
    }

    public long activeSize(Long resourceId) {
        return repo.activeSize(resourceId);
    }

    public long estimateEtaSeconds(Long gid, String token,
                                   long fixedDelayMs,
                                   long activeTargetSize,
                                   long maxPromotePerTick,
                                   long activeTtlSeconds) {

        // 내 순번 (1부터), 대기중이 아니면 0
        Long r0 = repo.rankInWait(gid, token);
        long myPos = (r0 == null) ? 0L : (r0 + 1);

        if (myPos <= 0) return 0L; // 이미 ACTIVE이거나 대기열에 없음

        long active = repo.activeSize(gid);
        long deficit = Math.max(0L, activeTargetSize - active);

        long D = Math.max(1L, fixedDelayMs / 1000L); // 틱 간격(초)
        long M = Math.max(1L, maxPromotePerTick);
        long T = Math.max(1L, activeTargetSize);
        long ttl = Math.max(1L, activeTtlSeconds);

        // Phase 1: 충전단계에서 내 앞 몇 명 먼저 입장 가능한가
        long phase1Serve = Math.min(deficit, myPos);
        long ticksPhase1 = (phase1Serve + M - 1) / M; // 올림

        // Phase 2: 안정상태의 평균 틱당 입장량(만료량 한도와 M 한도 중 작은 값)
        double avgPerTickSteady = Math.min(M, (double) D * ((double) T / (double) ttl));
        if (avgPerTickSteady <= 0.000001) {
            // 이론상 0이면 안전하게 아주 큰 값 리턴
            return 3600L; // 1시간 가드 (정책에 맞게 조정)
        }

        long remaining = myPos - phase1Serve;
        long ticksPhase2 = (remaining <= 0) ? 0L : (long) Math.ceil(remaining / avgPerTickSteady);

        long totalTicks = ticksPhase1 + ticksPhase2;
        return totalTicks * D; // 초 단위
    }
}
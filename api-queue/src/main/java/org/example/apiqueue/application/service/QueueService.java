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

    /** 대기열 합류 */
    public JoinResult join(Long resourceId, Long userId) {

        String existingToken = repo.getTokenForUser(resourceId, userId);

        if (existingToken != null) {

            // 토큰 상태 읽기
            String state = repo.getTokenState(existingToken);

            if ("ACTIVE".equals(state)) {
                // 이미 입장 허용된 사용자
                return new JoinResult(existingToken, -1);
            }

            if ("WAITING".equals(state)) {
                Long rank = repo.rankInWait(resourceId, existingToken);
                if (rank != null) {
                    return new JoinResult(existingToken, rank + 1);
                }
                // WAIT 상태지만 ZSET에서 누락 → 재참여 처리해야 함
            }

            // EXPIRED 혹은 WAIT/ACTIVE에서 사라진 잘못된 토큰 → 새로 join
        }

        // 여기에 오면 반드시 신규 토큰 생성
        String newToken = UUID.randomUUID().toString();
        Long pos = repo.addToWaitQueue(resourceId, newToken, userId);

        return new JoinResult(newToken, (pos == null ? 1 : pos));
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

        boolean isActive = repo.isActive(resourceId, token);
        Long r0 = repo.rankInWait(resourceId, token);
        Long waitSize = repo.waitSize(resourceId);

        // ACTIVE 상태 → position = 0
        if (isActive) {
            return new StatusResult(0, waitSize, 0);
        }

        // WAITING에도 없고 ACTIVE에도 없음 → 유효하지 않은 토큰
        if (!isActive && r0 == null) {
            return new StatusResult(-1, waitSize, 0);
        }

        // WAITING 상태 → rank 기반으로 1부터 계산
        long position = (r0 + 1);

        long etaSeconds = Math.round(position / 0.5);

        return new StatusResult(position, waitSize, etaSeconds);
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
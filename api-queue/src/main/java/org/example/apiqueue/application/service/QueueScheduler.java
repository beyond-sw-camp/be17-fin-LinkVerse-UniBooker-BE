package org.example.apiqueue.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.apiqueue.domain.port.out.QueueRepository;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "queue.scheduler", name = "enabled", havingValue = "true")
public class QueueScheduler {

    private final QueueService service;
    private final QueueRepository repo;
    private final RedissonClient redisson; // ✅ Redisson 분산락

    @Value("${queue.scheduler.resource-ids:*}")
    private String resourceIdsRaw;

    @Value("${queue.scheduler.fixed-delay-ms:10000}")
    private long fixedDelayMs;

    @Value("${queue.scheduler.active-target-size:150}")
    private long activeTargetSize;

    @Value("${queue.scheduler.max-promote-per-tick:10}")
    private long maxPromotePerTick;

    @Value("${queue.scheduler.active-ttl-seconds:120}")
    private long activeTtlSeconds;

    @Value("${queue.scheduler.lock-key:unibooker:queue:scheduler:lock}")
    private String schedulerLockKey;

    private final AtomicLong lastTick = new AtomicLong(0);

    /** 고정 rate로 정확히 fixedDelayMs마다 시작. 분산락으로 단일 인스턴스만 수행 */
    @Scheduled(fixedRateString = "${queue.scheduler.fixed-delay-ms:10000}")
    public void tick() {
        // 락 보유 시간: 주기보다 1초 짧게(겹침 방지), 최소 1초
        long leaseSeconds = Math.max(1, (fixedDelayMs / 1000) - 1);

        RLock lock = redisson.getLock(schedulerLockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(0, leaseSeconds, TimeUnit.SECONDS);
            if (!locked) {
                log.debug("[queue] another node holds scheduler lock; skip this tick");
                return;
            }

            long now = Instant.now().getEpochSecond();
            long elapsed = now - lastTick.getAndSet(now);
            log.info("[queue] tick start (+{}s since last)", elapsed);

            runTick(now);

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("[queue] scheduler interrupted", ie);
        } catch (Exception e) {
            log.error("[queue] scheduler tick failed", e);
        } finally {
            if (locked) {
                try { lock.unlock(); } catch (Exception ignore) {}
            }
        }
    }

    private void runTick(long nowEpochSec) {
        List<Long> resourceIds = resolveResourceIds();
        if (resourceIds.isEmpty()) {
            log.debug("[queue] no resources to process");
            return;
        }

        long cutoff = nowEpochSec - activeTtlSeconds;

        // 각 리소스를 병렬 처리해 전체 tick 시간을 단축
        resourceIds.parallelStream().forEach(rid -> {
            try {
                // 1) 목표 활성 인원 유지: 부족분만큼만 promote (최대 maxPromotePerTick)
                long active = service.activeSize(rid);
                long deficit = Math.max(0, activeTargetSize - active);
                long promoteCount = Math.min(deficit, maxPromotePerTick);

                if (promoteCount > 0) {
                    service.promote(rid, promoteCount);
                    log.info("[queue] promoted {} users in resource {} (active+={}/target={})",
                            promoteCount, rid, promoteCount, activeTargetSize);
                }

                // 2) 오래 머문 ACTIVE 만료 처리
                long expired = repo.expireOldActives(rid, cutoff);
                if (expired > 0) {
                    log.info("[queue] expired {} tokens in resource {}", expired, rid);
                }

            } catch (Exception e) {
                log.error("[queue] scheduler error for resource {}", rid, e);
            }
        });
    }

    /** 리소스 목록: 설정값 or 자동 탐색(*=대기/활성 보유 리소스) */
    private List<Long> resolveResourceIds() {
        if (resourceIdsRaw == null || resourceIdsRaw.isBlank() || "*".equals(resourceIdsRaw.trim())) {
            Set<Long> auto = new LinkedHashSet<>();
            try { auto.addAll(repo.findResourcesWithWaiters()); }
            catch (Exception e) { log.warn("[queue] findResourcesWithWaiters() failed", e); }
            try { auto.addAll(repo.findResourcesWithActives()); }
            catch (Exception e) { log.warn("[queue] findResourcesWithActives() failed", e); }
            return new ArrayList<>(auto);
        }
        return Arrays.stream(resourceIdsRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty() && !s.equals("*"))
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }
}

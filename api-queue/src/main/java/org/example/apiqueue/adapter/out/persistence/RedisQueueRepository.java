package org.example.apiqueue.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.example.apiqueue.domain.port.out.QueueRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisQueueRepository implements QueueRepository {

    private final StringRedisTemplate redis;

    // === TTL 설정(선택) ===
    @Value("${queue.token.ttl-seconds:7200}")       // 기본 2시간
    private long tokenTtlSeconds;
    @Value("${queue.userMapping.ttl-seconds:7200}") // 기본 2시간
    private long userMappingTtlSeconds;

    // === Key builder ( <도메인>:<리소스>:<식별자>[:<세부>] ) ===
    private String k(String... p){ return String.join(":", p); }
    private String waitKey(Long gid)   { return k("unibooker","queue", gid.toString(), "wait"); }
    private String activeKey(Long gid) { return k("unibooker","queue", gid.toString(), "active"); }
    private String seqKey(Long gid)    { return k("unibooker","queue", gid.toString(), "seq"); }
    private String userKey(Long gid, Long uid) { return k("unibooker","queue", gid.toString(), "user", uid.toString()); }
    private String tokenKey(String token)       { return k("unibooker","queue", "token", token); }

    @Override
    public Long addToWaitQueue(Long gid, String token, Long userId) {
        // 1) 중복 참여 방지: userKey SETNX
        Boolean created = redis.opsForValue().setIfAbsent(userKey(gid, userId), token, userMappingTtlSeconds, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(created)) {
            String existing = redis.opsForValue().get(userKey(gid, userId));
            Long rank = redis.opsForZSet().rank(waitKey(gid), existing);
            return (rank == null) ? 1 : rank + 1;
        }

        // 2) 순번 발급 → 대기열 삽입
        Long seq = redis.opsForValue().increment(seqKey(gid));
        redis.opsForZSet().add(waitKey(gid), token, seq);

        // 3) 토큰 상태 기록(+TTL)
        String tKey = tokenKey(token);
        redis.opsForHash().put(tKey, "state", "WAITING");
        redis.opsForHash().put(tKey, "serviceGroupId", gid.toString());
        redis.opsForHash().put(tKey, "userId", userId.toString());
        if (tokenTtlSeconds > 0) {
            redis.expire(tKey, tokenTtlSeconds, TimeUnit.SECONDS);
        }

        Long rank = redis.opsForZSet().rank(waitKey(gid), token);
        return (rank == null) ? 1 : rank + 1;
    }

    @Override
    public void activateNext(Long gid, long count) {
        Set<String> tokens = redis.opsForZSet().range(waitKey(gid), 0, count - 1);
        if (tokens == null || tokens.isEmpty()) return;

        SessionCallback<Object> cb = new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) {
                ZSetOperations<String, String> zset = operations.opsForZSet();
                HashOperations<String, Object, Object> hash = operations.opsForHash();

                double now = (double) Instant.now().getEpochSecond();
                for (String token : tokens) {
                    zset.remove(waitKey(gid), token);
                    zset.add(activeKey(gid), token, now);
                    hash.put(tokenKey(token), "state", "ACTIVE");
                }
                return null;
            }
        };

        redis.executePipelined(cb);
    }

    @Override
    public boolean isActive(Long gid, String token) {
        String state = (String) redis.opsForHash().get(tokenKey(token), "state");
        // (선택) 토큰이 다른 group의 것인지 추가 검증하려면 serviceGroupId 비교
        return "ACTIVE".equals(state);
    }

    @Override
    public Long rankInWait(Long gid, String token) {
        return redis.opsForZSet().rank(waitKey(gid), token);
    }

    @Override
    public Long waitSize(Long gid) {
        return redis.opsForZSet().size(waitKey(gid));
    }

    @Override
    public void markConsumed(Long gid, String token) {
        redis.opsForHash().put(tokenKey(token), "state", "CONSUMED");
        redis.opsForZSet().remove(activeKey(gid), token);
        // (선택) 소비 후 사용자 매핑 제거로 재참여 허용하려면 아래 라인 추가
        // var userId = (String) redis.opsForHash().get(tokenKey(token), "userId");
        // if (userId != null) redis.delete(userKey(gid, Long.valueOf(userId)));
    }
}
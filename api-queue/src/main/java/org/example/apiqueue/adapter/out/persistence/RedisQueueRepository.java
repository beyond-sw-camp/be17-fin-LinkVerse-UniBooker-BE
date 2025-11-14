package org.example.apiqueue.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.example.apiqueue.domain.port.out.QueueRepository;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisQueueRepository implements QueueRepository {

    private final StringRedisTemplate redis;
    private final RedissonClient redisson;  // Redisson TTL 전담

    // === TTL 설정 ===
    @Value("${queue.token.ttl-seconds:7200}") // 기본 2시간
    private long tokenTtlSeconds;

    @Value("${queue.userMapping.ttl-seconds:7200}")
    private long userMappingTtlSeconds;

    // === Key builder ===
    private String k(String... p){ return String.join(":", p); }
    private String waitKey(Long resourceId)   { return k("unibooker","queue", resourceId.toString(), "wait"); }
    private String activeKey(Long resourceId) { return k("unibooker","queue", resourceId.toString(), "active"); }
    private String seqKey(Long resourceId)    { return k("unibooker","queue", resourceId.toString(), "seq"); }
    private String userKey(Long resourceId, Long uid) { return k("unibooker","queue", resourceId.toString(), "user", uid.toString()); }
    private String tokenKey(String token)     { return k("unibooker","queue", "token", token); }


    @Override
    public Set<Long> findResourcesWithWaiters() {
        var pattern = "unibooker:queue:*:wait";
        var found = new java.util.LinkedHashSet<Long>();

        var options = ScanOptions.scanOptions().match(pattern).count(1000).build();
        try (Cursor<byte[]> cursor = (Cursor<byte[]>) redis.execute((RedisCallback<Object>) con -> con.scan(options))) {
            if (cursor != null) {
                while (cursor.hasNext()) {
                    String key = redis.getStringSerializer().deserialize(cursor.next());
                    String[] parts = key.split(":");
                    if (parts.length >= 4) {
                        Long rid = Long.valueOf(parts[2]);
                        Long size = redis.opsForZSet().size(key);
                        if (size != null && size > 0) found.add(rid);
                    }
                }
            }
        } catch (Exception ignore) {}
        return found;
    }

    @Override
    public Set<Long> findResourcesWithActives() {
        var pattern = "unibooker:queue:*:active";
        var found = new java.util.LinkedHashSet<Long>();

        var options = ScanOptions.scanOptions().match(pattern).count(1000).build();
        try (Cursor<byte[]> cursor = (Cursor<byte[]>) redis.execute((RedisCallback<Object>) con -> con.scan(options))) {
            if (cursor != null) {
                while (cursor.hasNext()) {
                    String key = redis.getStringSerializer().deserialize(cursor.next());
                    String[] parts = key.split(":");
                    if (parts.length >= 4) {
                        Long rid = Long.valueOf(parts[2]);
                        Long size = redis.opsForZSet().size(key);
                        if (size != null && size > 0) found.add(rid);
                    }
                }
            }
        } catch (Exception ignore) {}
        return found;
    }


    @Override
    public Long addToWaitQueue(Long resourceId, String token, Long userId) {

        // 1) 중복 참여 방지 (userKey SETNX)
        Boolean created = redis.opsForValue().setIfAbsent(
                userKey(resourceId, userId),
                token,
                userMappingTtlSeconds,
                TimeUnit.SECONDS
        );
        if (Boolean.FALSE.equals(created)) {
            String existing = redis.opsForValue().get(userKey(resourceId, userId));
            Long rank = redis.opsForZSet().rank(waitKey(resourceId), existing);
            return (rank == null) ? 1 : rank + 1;
        }

        // 2) 순번 발급 & waiting zset 삽입
        Long seq = redis.opsForValue().increment(seqKey(resourceId));
        redis.opsForZSet().add(waitKey(resourceId), token, seq);

        // 3) 토큰 정보 저장 + TTL (Redisson)
        String tKey = tokenKey(token);
        long now = Instant.now().getEpochSecond();

        redis.opsForHash().put(tKey, "state", "WAITING");
        redis.opsForHash().put(tKey, "serviceResourceId", resourceId.toString());
        redis.opsForHash().put(tKey, "userId", userId.toString());
        redis.opsForHash().put(tKey, "joinedAt", String.valueOf(now));

        // 여기만 Redisson 사용 → StackOverflowError 방지
        redisson.getBucket(tKey).expire(tokenTtlSeconds, TimeUnit.SECONDS);

        Long rank = redis.opsForZSet().rank(waitKey(resourceId), token);
        return (rank == null) ? 1 : rank + 1;
    }


    @Override
    public void activateNext(Long resourceId, long count) {
        Set<String> tokens = redis.opsForZSet().range(waitKey(resourceId), 0, count - 1);
        if (tokens == null || tokens.isEmpty()) return;

        SessionCallback<Object> cb = new SessionCallback<>() {
            @Override
            @SuppressWarnings("unchecked")
            public Object execute(RedisOperations operations) {

                RedisOperations<String, String> ops = (RedisOperations<String, String>) operations;
                ZSetOperations<String, String> zset = ops.opsForZSet();
                HashOperations<String, Object, Object> hash = ops.opsForHash();

                double now = (double) Instant.now().getEpochSecond();
                for (String token : tokens) {
                    zset.remove(waitKey(resourceId), (Object) token);
                    zset.add(activeKey(resourceId), token, now);
                    hash.put(tokenKey(token), "state", "ACTIVE");
                    hash.put(tokenKey(token), "enteredAt", String.valueOf((long) now));
                }
                return null;
            }
        };

        redis.executePipelined(cb);
    }


    @Override
    public boolean isActive(Long resourceId, String token) {
        String state = (String) redis.opsForHash().get(tokenKey(token), "state");
        return "ACTIVE".equals(state);
    }

    @Override
    public Long rankInWait(Long resourceId, String token) {
        return redis.opsForZSet().rank(waitKey(resourceId), token);
    }

    @Override
    public Long waitSize(Long resourceId) {
        return redis.opsForZSet().size(waitKey(resourceId));
    }

    @Override
    public Long activeSize(Long resourceId) {
        return redis.opsForZSet().size(activeKey(resourceId));
    }


    @Override
    public void markConsumed(Long resourceId, String token) {
        redis.opsForHash().put(tokenKey(token), "state", "CONSUMED");
        redis.opsForZSet().remove(activeKey(resourceId), token);
    }


    @Override
    public long expireOldActives(Long resourceId, long olderThanEpochSec) {
        String aKey = activeKey(resourceId);
        Set<String> tokens = redis.opsForZSet().rangeByScore(aKey, 0, olderThanEpochSec);
        if (tokens == null || tokens.isEmpty()) return 0L;

        SessionCallback<Object> cb = new SessionCallback<>() {
            @Override
            @SuppressWarnings("unchecked")
            public Object execute(RedisOperations operations) {

                RedisOperations<String, String> ops = (RedisOperations<String, String>) operations;

                ZSetOperations<String, String> zset = ops.opsForZSet();
                HashOperations<String, Object, Object> hash = ops.opsForHash();

                for (String token : tokens) {
                    zset.remove(aKey, (Object) token);
                    hash.put(tokenKey(token), "state", "EXPIRED");

                    Object uid = hash.get(tokenKey(token), "userId");
                    if (uid != null) {
                        ops.delete(userKey(resourceId, Long.valueOf(uid.toString())));
                    }
                }
                return null;
            }
        };

        redis.executePipelined(cb);
        return tokens.size();
    }


    @Override
    public List<TokenView> listWait(Long resourceId, long offset, long limit) {
        long start = Math.max(0, offset);
        long end = start + Math.max(1, limit) - 1;

        var set = redis.opsForZSet().range(waitKey(resourceId), start, end);
        if (set == null || set.isEmpty()) return List.of();

        var tokens = new java.util.ArrayList<String>(set);
        var res = new java.util.ArrayList<TokenView>(tokens.size());
        long posBase = start + 1;

        for (int i = 0; i < tokens.size(); i++) {
            String t = tokens.get(i);
            var map = redis.opsForHash().entries(tokenKey(t));

            Long uid = map.get("userId") == null ? null : Long.valueOf(map.get("userId").toString());
            String state = map.get("state") == null ? null : map.get("state").toString();
            Long joinedAt = map.get("joinedAt") == null ? null : Long.valueOf(map.get("joinedAt").toString());

            res.add(new TokenView(t, uid, state, posBase + i, joinedAt, null));
        }
        return res;
    }

    @Override
    public List<TokenView> listActive(Long resourceId, long offset, long limit) {
        long start = Math.max(0, offset);
        long end = start + Math.max(1, limit) - 1;

        var set = redis.opsForZSet().range(activeKey(resourceId), start, end);
        if (set == null || set.isEmpty()) return List.of();

        var tokens = new java.util.ArrayList<String>(set);
        var res = new java.util.ArrayList<TokenView>(tokens.size());

        for (String t : tokens) {
            var map = redis.opsForHash().entries(tokenKey(t));

            Long uid = map.get("userId") == null ? null : Long.valueOf(map.get("userId").toString());
            String state = map.get("state") == null ? null : map.get("state").toString();
            Long joinedAt = map.get("joinedAt") == null ? null : Long.valueOf(map.get("joinedAt").toString());
            Long enteredAt = map.get("enteredAt") == null ? null : Long.valueOf(map.get("enteredAt").toString());

            res.add(new TokenView(t, uid, state, null, joinedAt, enteredAt));
        }
        return res;
    }
}

package com.agenthub.infrastructure.store.reactive;

import com.agenthub.application.port.out.repositories.ReactiveKeyValueRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RedisReactiveKeyValueRepository implements ReactiveKeyValueRepository {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> set(String key, String value) {
        return redisTemplate.opsForValue().set(key, value).then();
    }

    @Override
    public Mono<Void> setex(String key, String value, long expire, TimeUnit unit) {
        return redisTemplate.opsForValue().set(key, value, Duration.of(expire, unit.toChronoUnit())).then();
    }

    @Override
    public Mono<Boolean> setnx(String key, String value) {
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    @Override
    public Mono<String> get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public Mono<String> getSet(String key, String value) {
        return redisTemplate.opsForValue().getAndSet(key, value);
    }

    @Override
    public Flux<String> mget(String... keys) {
        return redisTemplate.opsForValue().multiGet(Arrays.asList(keys))
            .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<Void> mset(Map<String, String> keyValueMap) {
        return redisTemplate.opsForValue().multiSet(keyValueMap).then();
    }

    @Override
    public Mono<Void> append(String key, String value) {
        return redisTemplate.opsForValue().append(key, value).then();
    }

    @Override
    public Mono<Long> strlen(String key) {
        return redisTemplate.opsForValue().size(key);
    }

    @Override
    public Mono<Long> incr(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    @Override
    public Mono<Long> incrBy(String key, long increment) {
        return redisTemplate.opsForValue().increment(key, increment);
    }

    @Override
    public Mono<Long> decr(String key) {
        return redisTemplate.opsForValue().increment(key, -1);
    }

    @Override
    public Mono<Long> decrBy(String key, long decrement) {
        return redisTemplate.opsForValue().increment(key, -decrement);
    }

    @Override
    public Mono<Void> hset(String key, String field, String value) {
        return redisTemplate.opsForHash().put(key, field, value).then();
    }

    @Override
    public Mono<Void> hmset(String key, Map<String, String> fieldValues) {
        return redisTemplate.opsForHash().putAll(key, fieldValues).then();
    }

    @Override
    public Mono<String> hget(String key, String field) {
        return redisTemplate.opsForHash().get(key, field).map(Object::toString);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Flux<String> hmget(String key, String... fields) {
        return redisTemplate.opsForHash().multiGet(key, Arrays.asList(fields))
            .flatMapMany(Flux::fromIterable)
            .map(v -> v != null ? v.toString() : null);
    }

    @Override
    public Mono<Map<String, String>> hgetAll(String key) {
        return redisTemplate.opsForHash().entries(key)
            .collectMap(e -> e.getKey().toString(), e -> e.getValue().toString());
    }

    @Override
    public Mono<Long> hdel(String key, String... fields) {
        return redisTemplate.opsForHash().remove(key, (Object[]) fields);
    }

    @Override
    public Mono<Boolean> hexists(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    @Override
    public Mono<Long> hlen(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    @Override
    public Flux<String> hkeys(String key) {
        return redisTemplate.opsForHash().keys(key).map(Object::toString);
    }

    @Override
    public Flux<String> hvals(String key) {
        return redisTemplate.opsForHash().values(key).map(Object::toString);
    }

    @Override
    public Mono<Boolean> hsetnx(String key, String field, String value) {
        return redisTemplate.opsForHash().putIfAbsent(key, field, value);
    }

    @Override
    public Mono<Long> hincrBy(String key, String field, long increment) {
        return redisTemplate.opsForHash().increment(key, field, increment);
    }

    @Override
    public Mono<Long> lpush(String key, String... values) {
        return redisTemplate.opsForList().leftPushAll(key, values);
    }

    @Override
    public Mono<Long> rpush(String key, String... values) {
        return redisTemplate.opsForList().rightPushAll(key, values);
    }

    @Override
    public Mono<String> lpop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    @Override
    public Mono<String> rpop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    @Override
    public Mono<Long> llen(String key) {
        return redisTemplate.opsForList().size(key);
    }

    @Override
    public Mono<String> lindex(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    @Override
    public Mono<Void> lset(String key, long index, String value) {
        return redisTemplate.opsForList().set(key, index, value).then();
    }

    @Override
    public Flux<String> lrange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    @Override
    public Mono<Void> ltrim(String key, long start, long end) {
        return redisTemplate.opsForList().trim(key, start, end).then();
    }

    @Override
    public Mono<Long> lrem(String key, long count, String value) {
        return redisTemplate.opsForList().remove(key, count, value);
    }

    @Override
    public Mono<String> rpoplpush(String source, String destination) {
        return redisTemplate.opsForList().rightPopAndLeftPush(source, destination);
    }

    @Override
    public Mono<String> blpop(long timeout, String... keys) {
        return redisTemplate.opsForList().leftPop(keys[0], Duration.ofSeconds(timeout));
    }

    @Override
    public Mono<String> brpop(long timeout, String... keys) {
        return redisTemplate.opsForList().rightPop(keys[0], Duration.ofSeconds(timeout));
    }

    @Override
    public Mono<Long> sadd(String key, String... members) {
        return redisTemplate.opsForSet().add(key, members);
    }

    @Override
    public Mono<Long> srem(String key, String... members) {
        return redisTemplate.opsForSet().remove(key, (Object[]) members);
    }

    @Override
    public Flux<String> smembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    @Override
    public Mono<Boolean> sismember(String key, String member) {
        return redisTemplate.opsForSet().isMember(key, member);
    }

    @Override
    public Mono<Long> scard(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    @Override
    public Mono<String> spop(String key) {
        return redisTemplate.opsForSet().pop(key);
    }

    @Override
    public Mono<String> srandmember(String key) {
        return redisTemplate.opsForSet().randomMember(key);
    }

    @Override
    public Flux<String> srandmember(String key, long count) {
        return redisTemplate.opsForSet().randomMembers(key, count);
    }

    @Override
    public Mono<Boolean> smove(String source, String destination, String member) {
        return redisTemplate.opsForSet().move(source, member, destination);
    }

    @Override
    public Flux<String> sinter(String... keys) {
        if (keys.length == 0) {
            return Flux.empty();
        }
        if (keys.length == 1) {
            return redisTemplate.opsForSet().members(keys[0]);
        }
        return redisTemplate.opsForSet().intersect(keys[0], Arrays.asList(Arrays.copyOfRange(keys, 1, keys.length)));
    }

    @Override
    public Flux<String> sunion(String... keys) {
        if (keys.length == 0) {
            return Flux.empty();
        }
        if (keys.length == 1) {
            return redisTemplate.opsForSet().members(keys[0]);
        }
        return redisTemplate.opsForSet().union(keys[0], Arrays.asList(Arrays.copyOfRange(keys, 1, keys.length)));
    }

    @Override
    public Flux<String> sdiff(String... keys) {
        if (keys.length == 0) {
            return Flux.empty();
        }
        if (keys.length == 1) {
            return redisTemplate.opsForSet().members(keys[0]);
        }
        return redisTemplate.opsForSet().difference(keys[0], Arrays.asList(Arrays.copyOfRange(keys, 1, keys.length)));
    }

    @Override
    public Mono<Long> zadd(String key, Map<String, Double> memberScores) {
        Set<ZSetOperations.TypedTuple<String>> tuples = memberScores.entrySet().stream()
            .map(e -> ZSetOperations.TypedTuple.of(e.getKey(), e.getValue()))
            .collect(Collectors.toSet());
        return redisTemplate.opsForZSet().addAll(key, tuples);
    }

    @Override
    public Mono<Long> zadd(String key, String member, double score) {
        return redisTemplate.opsForZSet().add(key, member, score).map(b -> b ? 1L : 0L);
    }

    @Override
    public Mono<Long> zrem(String key, String... members) {
        return redisTemplate.opsForZSet().remove(key, (Object[]) members);
    }

    @Override
    public Mono<Double> zscore(String key, String member) {
        return redisTemplate.opsForZSet().score(key, member);
    }

    @Override
    public Mono<Long> zrank(String key, String member) {
        return redisTemplate.opsForZSet().rank(key, member);
    }

    @Override
    public Mono<Long> zrevrank(String key, String member) {
        return redisTemplate.opsForZSet().reverseRank(key, member);
    }

    @Override
    public Mono<Long> zcard(String key) {
        return redisTemplate.opsForZSet().size(key);
    }

    @Override
    public Mono<Long> zcount(String key, double min, double max) {
        return redisTemplate.opsForZSet().count(key, Range.closed(min, max));
    }

    @Override
    public Flux<String> zrange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, Range.closed(start, end));
    }

    @Override
    public Mono<Map<String, Double>> zrangeWithScores(String key, long start, long end) {
        return redisTemplate.opsForZSet().rangeWithScores(key, Range.closed(start, end))
            .collectMap(ZSetOperations.TypedTuple::getValue, ZSetOperations.TypedTuple::getScore);
    }

    @Override
    public Flux<String> zrevrange(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, Range.closed(start, end));
    }

    @Override
    public Mono<Map<String, Double>> zrevrangeWithScores(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRangeWithScores(key, Range.closed(start, end))
            .collectMap(ZSetOperations.TypedTuple::getValue, ZSetOperations.TypedTuple::getScore);
    }

    @Override
    public Flux<String> zrangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(key, Range.closed(min, max));
    }

    @Override
    public Mono<Map<String, Double>> zrangeByScoreWithScores(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScoreWithScores(key, Range.closed(min, max))
            .collectMap(ZSetOperations.TypedTuple::getValue, ZSetOperations.TypedTuple::getScore);
    }

    @Override
    public Mono<Double> zincrby(String key, double increment, String member) {
        return redisTemplate.opsForZSet().incrementScore(key, member, increment);
    }

    @Override
    public Mono<Long> zremrangeByRank(String key, long start, long end) {
        return redisTemplate.opsForZSet().removeRange(key, Range.closed(start, end));
    }

    @Override
    public Mono<Long> zremrangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().removeRangeByScore(key, Range.closed(min, max));
    }

    @Override
    public Mono<Long> del(String... keys) {
        if (keys.length == 0) {
            return Mono.just(0L);
        }
        return redisTemplate.delete(keys);
    }

    @Override
    public Mono<Boolean> exists(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public Mono<Boolean> expire(String key, long expire, TimeUnit unit) {
        return redisTemplate.expire(key, Duration.of(expire, unit.toChronoUnit()));
    }

    @Override
    public Mono<Boolean> expireAt(String key, long timestamp) {
        return redisTemplate.expireAt(key, Instant.ofEpochSecond(timestamp));
    }

    @Override
    public Mono<Boolean> persist(String key) {
        return redisTemplate.persist(key);
    }

    @Override
    public Mono<Long> ttl(String key) {
        return redisTemplate.getExpire(key).map(Duration::getSeconds);
    }

    @Override
    public Mono<Long> pttl(String key) {
        return redisTemplate.getExpire(key).map(Duration::toMillis);
    }

    @Override
    public Mono<String> type(String key) {
        return redisTemplate.type(key).map(Enum::name);
    }

    @Override
    public Mono<Void> rename(String oldKey, String newKey) {
        return redisTemplate.rename(oldKey, newKey).then();
    }

    @Override
    public Mono<Boolean> renamenx(String oldKey, String newKey) {
        return redisTemplate.renameIfAbsent(oldKey, newKey);
    }

    @Override
    public Flux<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    @Override
    public Mono<String> randomKey() {
        return redisTemplate.randomKey();
    }

    @Override
    public Mono<Boolean> move(String key, int dbIndex) {
        return redisTemplate.move(key, dbIndex);
    }

    @Override
    public Mono<Boolean> setbit(String key, long offset, boolean value) {
        return redisTemplate.opsForValue().setBit(key, offset, value);
    }

    @Override
    public Mono<Boolean> getbit(String key, long offset) {
        return redisTemplate.opsForValue().getBit(key, offset);
    }

    @Override
    @SuppressWarnings("Convert2Lambda")
    public Mono<Long> bitcount(String key) {
        return Mono.fromCallable(() ->
            (Long) stringRedisTemplate.execute(
                (org.springframework.data.redis.core.RedisCallback<Long>)
                    connection -> connection.bitCount(key.getBytes())));
    }

    @Override
    @SuppressWarnings("Convert2Lambda")
    public Mono<Long> bitcount(String key, long start, long end) {
        return Mono.fromCallable(() ->
            (Long) stringRedisTemplate.execute(
                (org.springframework.data.redis.core.RedisCallback<Long>)
                    connection -> connection.bitCount(key.getBytes(), start, end)));
    }

    @Override
    public Mono<Long> pfadd(String key, String... elements) {
        return Mono.fromCallable(() -> stringRedisTemplate.opsForHyperLogLog().add(key, elements));
    }

    @Override
    public Mono<Long> pfcount(String... keys) {
        return Mono.fromCallable(() -> stringRedisTemplate.opsForHyperLogLog().size(keys));
    }

    @Override
    public Mono<Void> pfmerge(String destKey, String... sourceKeys) {
        return Mono.fromRunnable(() -> stringRedisTemplate.opsForHyperLogLog().union(destKey, sourceKeys));
    }

    @Override
    public Mono<Object> exec(Runnable transaction) {
        return Mono.fromRunnable(transaction).then(Mono.empty());
    }

    @Override
    public Mono<Object> eval(String script, List<String> keys, List<String> args) {
        return Mono.fromCallable(() -> stringRedisTemplate.execute(
            (org.springframework.data.redis.core.RedisCallback<Object>) connection -> {
                int numKeys = keys.size();
                byte[][] keysBytes = keys.stream().map(String::getBytes).toArray(byte[][]::new);
                return connection.eval(script.getBytes(),
                    org.springframework.data.redis.connection.ReturnType.VALUE, numKeys, keysBytes);
            }));
    }

    @Override
    public Mono<Object> evalsha(String sha1, List<String> keys, List<String> args) {
        return Mono.fromCallable(() -> stringRedisTemplate.execute(
            (org.springframework.data.redis.core.RedisCallback<Object>) connection -> {
                int numKeys = keys.size();
                byte[][] keysBytes = keys.stream().map(String::getBytes).toArray(byte[][]::new);
                return connection.evalSha(sha1,
                    org.springframework.data.redis.connection.ReturnType.VALUE, numKeys, keysBytes);
            }));
    }

    @Override
    public Mono<Long> publish(String channel, String message) {
        return redisTemplate.convertAndSend(channel, message);
    }

    @Override
    public Mono<Void> subscribe(String... channels) {
        return Mono.error(new UnsupportedOperationException(
            "subscribe must be managed via RedisMessageListenerContainer"));
    }

    @Override
    public Mono<Void> psubscribe(String... patterns) {
        return Mono.error(new UnsupportedOperationException(
            "psubscribe must be managed via RedisMessageListenerContainer"));
    }

    @Override
    public Mono<Void> unsubscribe(String... channels) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> punsubscribe(String... patterns) {
        return Mono.empty();
    }

    @Override
    public Mono<Long> dbsize() {
        return Mono.fromCallable(() -> stringRedisTemplate.execute(
            (org.springframework.data.redis.core.RedisCallback<Long>) connection -> connection.dbSize()));
    }

    @Override
    public Mono<Void> flushdb() {
        return Mono.fromRunnable(() -> stringRedisTemplate.execute(
            (org.springframework.data.redis.core.RedisCallback<Void>) connection -> {
                connection.flushDb();
                return null;
            }));
    }

    @Override
    public Mono<Void> flushall() {
        return Mono.fromRunnable(() -> stringRedisTemplate.execute(
            (org.springframework.data.redis.core.RedisCallback<Void>) connection -> {
                connection.flushAll();
                return null;
            }));
    }

    @Override
    public Mono<Void> select(int dbIndex) {
        return Mono.fromRunnable(() -> stringRedisTemplate.execute(
            (org.springframework.data.redis.core.RedisCallback<Void>) connection -> {
                connection.select(dbIndex);
                return null;
            }));
    }

    @Override
    public Mono<Long> delByPattern(String pattern) {
        return redisTemplate.keys(pattern)
            .collectList()
            .flatMap(keys -> keys.isEmpty() ? Mono.just(0L) : redisTemplate.delete(keys.toArray(new String[0])));
    }

    @Override
    public <T> Mono<T> get(String key, Class<T> type) {
        return redisTemplate.opsForValue().get(key)
            .flatMap(json -> Mono.fromCallable(() -> objectMapper.readValue(json, type)));
    }

    @Override
    public <T> Mono<Void> set(String key, T value) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(value))
            .flatMap(json -> redisTemplate.opsForValue().set(key, json).then());
    }

    @Override
    public <T> Mono<Void> setex(String key, T value, long expire, TimeUnit unit) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(value))
            .flatMap(json -> redisTemplate.opsForValue().set(key, json,
                Duration.of(expire, unit.toChronoUnit())).then());
    }
}

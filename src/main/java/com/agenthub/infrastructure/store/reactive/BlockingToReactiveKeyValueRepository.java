package com.agenthub.infrastructure.store.reactive;

import com.agenthub.application.port.out.repositories.KeyValueRepository;
import com.agenthub.application.port.out.repositories.ReactiveKeyValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 将阻塞式 {@link KeyValueRepository} 适配为响应式 {@link ReactiveKeyValueRepository}。
 */
@Component
@RequiredArgsConstructor
public class BlockingToReactiveKeyValueRepository implements ReactiveKeyValueRepository {

    private final KeyValueRepository delegate;

    // ==================== 字符串(String)操作 ====================

    @Override
    public Mono<Void> set(String key, String value) {
        return Mono.fromRunnable(() -> delegate.set(key, value));
    }

    @Override
    public Mono<Void> setex(String key, String value, long expire, TimeUnit unit) {
        return Mono.fromRunnable(() -> delegate.setex(key, value, expire, unit));
    }

    @Override
    public Mono<Boolean> setnx(String key, String value) {
        return Mono.fromCallable(() -> delegate.setnx(key, value));
    }

    @Override
    public Mono<String> get(String key) {
        return Mono.fromCallable(() -> delegate.get(key));
    }

    @Override
    public Mono<String> getSet(String key, String value) {
        return Mono.fromCallable(() -> delegate.getSet(key, value));
    }

    @Override
    public Flux<String> mget(String... keys) {
        return Mono.fromCallable(() -> delegate.mget(keys)).flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<Void> mset(Map<String, String> keyValueMap) {
        return Mono.fromRunnable(() -> delegate.mset(keyValueMap));
    }

    @Override
    public Mono<Void> append(String key, String value) {
        return Mono.fromRunnable(() -> delegate.append(key, value));
    }

    @Override
    public Mono<Long> strlen(String key) {
        return Mono.fromCallable(() -> delegate.strlen(key));
    }

    @Override
    public Mono<Long> incr(String key) {
        return Mono.fromCallable(() -> delegate.incr(key));
    }

    @Override
    public Mono<Long> incrBy(String key, long increment) {
        return Mono.fromCallable(() -> delegate.incrBy(key, increment));
    }

    @Override
    public Mono<Long> decr(String key) {
        return Mono.fromCallable(() -> delegate.decr(key));
    }

    @Override
    public Mono<Long> decrBy(String key, long decrement) {
        return Mono.fromCallable(() -> delegate.decrBy(key, decrement));
    }

    // ==================== 哈希(Hash)操作 ====================

    @Override
    public Mono<Void> hset(String key, String field, String value) {
        return Mono.fromRunnable(() -> delegate.hset(key, field, value));
    }

    @Override
    public Mono<Void> hmset(String key, Map<String, String> fieldValues) {
        return Mono.fromRunnable(() -> delegate.hmset(key, fieldValues));
    }

    @Override
    public Mono<String> hget(String key, String field) {
        return Mono.fromCallable(() -> delegate.hget(key, field));
    }

    @Override
    public Flux<String> hmget(String key, String... fields) {
        return Mono.fromCallable(() -> delegate.hmget(key, fields)).flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<Map<String, String>> hgetAll(String key) {
        return Mono.fromCallable(() -> delegate.hgetAll(key));
    }

    @Override
    public Mono<Long> hdel(String key, String... fields) {
        return Mono.fromCallable(() -> delegate.hdel(key, fields));
    }

    @Override
    public Mono<Boolean> hexists(String key, String field) {
        return Mono.fromCallable(() -> delegate.hexists(key, field));
    }

    @Override
    public Mono<Long> hlen(String key) {
        return Mono.fromCallable(() -> delegate.hlen(key));
    }

    @Override
    public Flux<String> hkeys(String key) {
        return Mono.fromCallable(() -> delegate.hkeys(key)).flatMapMany(Flux::fromIterable);
    }

    @Override
    public Flux<String> hvals(String key) {
        return Mono.fromCallable(() -> delegate.hvals(key)).flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<Boolean> hsetnx(String key, String field, String value) {
        return Mono.fromCallable(() -> delegate.hsetnx(key, field, value));
    }

    @Override
    public Mono<Long> hincrBy(String key, String field, long increment) {
        return Mono.fromCallable(() -> delegate.hincrBy(key, field, increment));
    }

    // ==================== 列表(List)操作 ====================

    @Override
    public Mono<Long> lpush(String key, String... values) {
        return Mono.fromCallable(() -> delegate.lpush(key, values));
    }

    @Override
    public Mono<Long> rpush(String key, String... values) {
        return Mono.fromCallable(() -> delegate.rpush(key, values));
    }

    @Override
    public Mono<String> lpop(String key) {
        return Mono.fromCallable(() -> delegate.lpop(key));
    }

    @Override
    public Mono<String> rpop(String key) {
        return Mono.fromCallable(() -> delegate.rpop(key));
    }

    @Override
    public Mono<Long> llen(String key) {
        return Mono.fromCallable(() -> delegate.llen(key));
    }

    @Override
    public Mono<String> lindex(String key, long index) {
        return Mono.fromCallable(() -> delegate.lindex(key, index));
    }

    @Override
    public Mono<Void> lset(String key, long index, String value) {
        return Mono.fromRunnable(() -> delegate.lset(key, index, value));
    }

    @Override
    public Flux<String> lrange(String key, long start, long end) {
        return Mono.fromCallable(() -> delegate.lrange(key, start, end)).flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<Void> ltrim(String key, long start, long end) {
        return Mono.fromRunnable(() -> delegate.ltrim(key, start, end));
    }

    @Override
    public Mono<Long> lrem(String key, long count, String value) {
        return Mono.fromCallable(() -> delegate.lrem(key, count, value));
    }

    @Override
    public Mono<String> rpoplpush(String source, String destination) {
        return Mono.fromCallable(() -> delegate.rpoplpush(source, destination));
    }

    @Override
    public Mono<String> blpop(long timeout, String... keys) {
        return Mono.fromCallable(() -> delegate.blpop(timeout, keys));
    }

    @Override
    public Mono<String> brpop(long timeout, String... keys) {
        return Mono.fromCallable(() -> delegate.brpop(timeout, keys));
    }

    // ==================== 集合(Set)操作 ====================

    @Override
    public Mono<Long> sadd(String key, String... members) {
        return Mono.fromCallable(() -> delegate.sadd(key, members));
    }

    @Override
    public Mono<Long> srem(String key, String... members) {
        return Mono.fromCallable(() -> delegate.srem(key, members));
    }

    @Override
    public Flux<String> smembers(String key) {
        return Mono.fromCallable(() -> delegate.smembers(key)).flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<Boolean> sismember(String key, String member) {
        return Mono.fromCallable(() -> delegate.sismember(key, member));
    }

    @Override
    public Mono<Long> scard(String key) {
        return Mono.fromCallable(() -> delegate.scard(key));
    }

    @Override
    public Mono<String> spop(String key) {
        return Mono.fromCallable(() -> delegate.spop(key));
    }

    @Override
    public Mono<String> srandmember(String key) {
        return Mono.fromCallable(() -> delegate.srandmember(key));
    }

    @Override
    public Flux<String> srandmember(String key, long count) {
        return Mono.fromCallable(() -> delegate.srandmember(key, count)).flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<Boolean> smove(String source, String destination, String member) {
        return Mono.fromCallable(() -> delegate.smove(source, destination, member));
    }

    @Override
    public Flux<String> sinter(String... keys) {
        return Mono.fromCallable(() -> delegate.sinter(keys)).flatMapMany(Flux::fromIterable);
    }

    @Override
    public Flux<String> sunion(String... keys) {
        return Mono.fromCallable(() -> delegate.sunion(keys)).flatMapMany(Flux::fromIterable);
    }

    @Override
    public Flux<String> sdiff(String... keys) {
        return Mono.fromCallable(() -> delegate.sdiff(keys)).flatMapMany(Flux::fromIterable);
    }

    // ==================== 有序集合(Sorted Set)操作 ====================

    @Override
    public Mono<Long> zadd(String key, Map<String, Double> memberScores) {
        return Mono.fromCallable(() -> delegate.zadd(key, memberScores));
    }

    @Override
    public Mono<Long> zadd(String key, String member, double score) {
        return Mono.fromCallable(() -> delegate.zadd(key, member, score));
    }

    @Override
    public Mono<Long> zrem(String key, String... members) {
        return Mono.fromCallable(() -> delegate.zrem(key, members));
    }

    @Override
    public Mono<Double> zscore(String key, String member) {
        return Mono.fromCallable(() -> delegate.zscore(key, member));
    }

    @Override
    public Mono<Long> zrank(String key, String member) {
        return Mono.fromCallable(() -> delegate.zrank(key, member));
    }

    @Override
    public Mono<Long> zrevrank(String key, String member) {
        return Mono.fromCallable(() -> delegate.zrevrank(key, member));
    }

    @Override
    public Mono<Long> zcard(String key) {
        return Mono.fromCallable(() -> delegate.zcard(key));
    }

    @Override
    public Mono<Long> zcount(String key, double min, double max) {
        return Mono.fromCallable(() -> delegate.zcount(key, min, max));
    }

    @Override
    public Flux<String> zrange(String key, long start, long end) {
        return Mono.fromCallable(() -> delegate.zrange(key, start, end)).flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<Map<String, Double>> zrangeWithScores(String key, long start, long end) {
        return Mono.fromCallable(() -> delegate.zrangeWithScores(key, start, end));
    }

    @Override
    public Flux<String> zrevrange(String key, long start, long end) {
        return Mono.fromCallable(() -> delegate.zrevrange(key, start, end)).flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<Map<String, Double>> zrevrangeWithScores(String key, long start, long end) {
        return Mono.fromCallable(() -> delegate.zrevrangeWithScores(key, start, end));
    }

    @Override
    public Flux<String> zrangeByScore(String key, double min, double max) {
        return Mono.fromCallable(() -> delegate.zrangeByScore(key, min, max)).flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<Map<String, Double>> zrangeByScoreWithScores(String key, double min, double max) {
        return Mono.fromCallable(() -> delegate.zrangeByScoreWithScores(key, min, max));
    }

    @Override
    public Mono<Double> zincrby(String key, double increment, String member) {
        return Mono.fromCallable(() -> delegate.zincrby(key, increment, member));
    }

    @Override
    public Mono<Long> zremrangeByRank(String key, long start, long end) {
        return Mono.fromCallable(() -> delegate.zremrangeByRank(key, start, end));
    }

    @Override
    public Mono<Long> zremrangeByScore(String key, double min, double max) {
        return Mono.fromCallable(() -> delegate.zremrangeByScore(key, min, max));
    }

    // ==================== 键(Key)操作 ====================

    @Override
    public Mono<Long> del(String... keys) {
        return Mono.fromCallable(() -> delegate.del(keys));
    }

    @Override
    public Mono<Boolean> exists(String key) {
        return Mono.fromCallable(() -> delegate.exists(key));
    }

    @Override
    public Mono<Boolean> expire(String key, long expire, TimeUnit unit) {
        return Mono.fromCallable(() -> delegate.expire(key, expire, unit));
    }

    @Override
    public Mono<Boolean> expireAt(String key, long timestamp) {
        return Mono.fromCallable(() -> delegate.expireAt(key, timestamp));
    }

    @Override
    public Mono<Boolean> persist(String key) {
        return Mono.fromCallable(() -> delegate.persist(key));
    }

    @Override
    public Mono<Long> ttl(String key) {
        return Mono.fromCallable(() -> delegate.ttl(key));
    }

    @Override
    public Mono<Long> pttl(String key) {
        return Mono.fromCallable(() -> delegate.pttl(key));
    }

    @Override
    public Mono<String> type(String key) {
        return Mono.fromCallable(() -> delegate.type(key));
    }

    @Override
    public Mono<Void> rename(String oldKey, String newKey) {
        return Mono.fromRunnable(() -> delegate.rename(oldKey, newKey));
    }

    @Override
    public Mono<Boolean> renamenx(String oldKey, String newKey) {
        return Mono.fromCallable(() -> delegate.renamenx(oldKey, newKey));
    }

    @Override
    public Flux<String> keys(String pattern) {
        return Mono.fromCallable(() -> delegate.keys(pattern)).flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<String> randomKey() {
        return Mono.fromCallable(() -> delegate.randomKey());
    }

    @Override
    public Mono<Boolean> move(String key, int dbIndex) {
        return Mono.fromCallable(() -> delegate.move(key, dbIndex));
    }

    // ==================== 位图(Bitmap)操作 ====================

    @Override
    public Mono<Boolean> setbit(String key, long offset, boolean value) {
        return Mono.fromCallable(() -> delegate.setbit(key, offset, value));
    }

    @Override
    public Mono<Boolean> getbit(String key, long offset) {
        return Mono.fromCallable(() -> delegate.getbit(key, offset));
    }

    @Override
    public Mono<Long> bitcount(String key) {
        return Mono.fromCallable(() -> delegate.bitcount(key));
    }

    @Override
    public Mono<Long> bitcount(String key, long start, long end) {
        return Mono.fromCallable(() -> delegate.bitcount(key, start, end));
    }

    // ==================== HyperLogLog操作 ====================

    @Override
    public Mono<Long> pfadd(String key, String... elements) {
        return Mono.fromCallable(() -> delegate.pfadd(key, elements));
    }

    @Override
    public Mono<Long> pfcount(String... keys) {
        return Mono.fromCallable(() -> delegate.pfcount(keys));
    }

    @Override
    public Mono<Void> pfmerge(String destKey, String... sourceKeys) {
        return Mono.fromRunnable(() -> delegate.pfmerge(destKey, sourceKeys));
    }

    // ==================== 事务和脚本操作 ====================

    @Override
    public Mono<Object> exec(Runnable transaction) {
        return Mono.fromCallable(() -> delegate.exec(transaction));
    }

    @Override
    public Mono<Object> eval(String script, List<String> keys, List<String> args) {
        return Mono.fromCallable(() -> delegate.eval(script, keys, args));
    }

    @Override
    public Mono<Object> evalsha(String sha1, List<String> keys, List<String> args) {
        return Mono.fromCallable(() -> delegate.evalsha(sha1, keys, args));
    }

    // ==================== 发布订阅操作 ====================

    @Override
    public Mono<Long> publish(String channel, String message) {
        return Mono.fromCallable(() -> delegate.publish(channel, message));
    }

    @Override
    public Mono<Void> subscribe(String... channels) {
        return Mono.fromRunnable(() -> delegate.subscribe(channels));
    }

    @Override
    public Mono<Void> psubscribe(String... patterns) {
        return Mono.fromRunnable(() -> delegate.psubscribe(patterns));
    }

    @Override
    public Mono<Void> unsubscribe(String... channels) {
        return Mono.fromRunnable(() -> delegate.unsubscribe(channels));
    }

    @Override
    public Mono<Void> punsubscribe(String... patterns) {
        return Mono.fromRunnable(() -> delegate.punsubscribe(patterns));
    }

    // ==================== 数据库操作 ====================

    @Override
    public Mono<Long> dbsize() {
        return Mono.fromCallable(() -> delegate.dbsize());
    }

    @Override
    public Mono<Void> flushdb() {
        return Mono.fromRunnable(delegate::flushdb);
    }

    @Override
    public Mono<Void> flushall() {
        return Mono.fromRunnable(delegate::flushall);
    }

    @Override
    public Mono<Void> select(int dbIndex) {
        return Mono.fromRunnable(() -> delegate.select(dbIndex));
    }

    // ==================== 其他实用方法 ====================

    @Override
    public Mono<Long> delByPattern(String pattern) {
        return Mono.fromCallable(() -> delegate.delByPattern(pattern));
    }

    @Override
    public <T> Mono<T> get(String key, Class<T> type) {
        return Mono.fromCallable(() -> delegate.get(key, type));
    }

    @Override
    public <T> Mono<Void> set(String key, T value) {
        return Mono.fromRunnable(() -> delegate.set(key, value));
    }

    @Override
    public <T> Mono<Void> setex(String key, T value, long expire, TimeUnit unit) {
        return Mono.fromRunnable(() -> delegate.setex(key, value, expire, unit));
    }
}

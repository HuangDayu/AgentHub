package com.agenthub.infrastructure.store.redis;

import com.agenthub.application.port.out.repositories.KeyValueRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis键值存储服务实现
 * 
 * @author huangdayu
 */
@Component
@ConditionalOnBean(RedisTemplate.class)
@ConditionalOnProperty(name = "agenthub.kv.type", havingValue = "redis")
public class RedisKeyValueRepository implements KeyValueRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    public RedisKeyValueRepository(RedisTemplate<String, Object> redisTemplate,
                                   StringRedisTemplate stringRedisTemplate) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // ==================== 字符串(String)操作 ====================

    @Override
    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void setex(String key, String value, long expire, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, Expiration.from( expire, unit));
    }

    @Override
    public boolean setnx(String key, String value) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key, value));
    }

    @Override
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    @Override
    public String getSet(String key, String value) {
        return stringRedisTemplate.opsForValue().getAndSet(key, value);
    }

    @Override
    public List<String> mget(String... keys) {
        return stringRedisTemplate.opsForValue().multiGet(Arrays.asList(keys));
    }

    @Override
    public void mset(Map<String, String> keyValueMap) {
        stringRedisTemplate.opsForValue().multiSet(keyValueMap);
    }

    @Override
    public void append(String key, String value) {
        stringRedisTemplate.opsForValue().append(key, value);
    }

    @Override
    public Long strlen(String key) {
        return stringRedisTemplate.opsForValue().size(key);
    }

    @Override
    public Long incr(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }

    @Override
    public Long incrBy(String key, long increment) {
        return stringRedisTemplate.opsForValue().increment(key, increment);
    }

    @Override
    public Long decr(String key) {
        return stringRedisTemplate.opsForValue().decrement(key);
    }

    @Override
    public Long decrBy(String key, long decrement) {
        return stringRedisTemplate.opsForValue().decrement(key, decrement);
    }

    // ==================== 哈希(Hash)操作 ====================

    @Override
    public void hset(String key, String field, String value) {
        stringRedisTemplate.opsForHash().put(key, field, value);
    }

    @Override
    public void hmset(String key, Map<String, String> fieldValues) {
        stringRedisTemplate.opsForHash().putAll(key, fieldValues);
    }

    @Override
    public String hget(String key, String field) {
        return (String) stringRedisTemplate.opsForHash().get(key, field);
    }

    @Override
    public List<String> hmget(String key, String... fields) {
        return stringRedisTemplate.<String, String>opsForHash().multiGet(key, Arrays.asList(fields));
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        return stringRedisTemplate.<String, String>opsForHash().entries(key);
    }

    @Override
    public Long hdel(String key, String... fields) {
        return stringRedisTemplate.opsForHash().delete(key, (Object[]) fields);
    }

    @Override
    public boolean hexists(String key, String field) {
        return stringRedisTemplate.opsForHash().hasKey(key, field);
    }

    @Override
    public Long hlen(String key) {
        return stringRedisTemplate.opsForHash().size(key);
    }

    @Override
    public Set<String> hkeys(String key) {
        return stringRedisTemplate.<String, String>opsForHash().keys(key);
    }

    @Override
    public List<String> hvals(String key) {
        return stringRedisTemplate.<String, String>opsForHash().values(key);
    }

    @Override
    public boolean hsetnx(String key, String field, String value) {
        return stringRedisTemplate.opsForHash().putIfAbsent(key, field, value);
    }

    @Override
    public Long hincrBy(String key, String field, long increment) {
        return stringRedisTemplate.opsForHash().increment(key, field, increment);
    }

    // ==================== 列表(List)操作 ====================

    @Override
    public Long lpush(String key, String... values) {
        return stringRedisTemplate.opsForList().leftPushAll(key, values);
    }

    @Override
    public Long rpush(String key, String... values) {
        return stringRedisTemplate.opsForList().rightPushAll(key, values);
    }

    @Override
    public String lpop(String key) {
        return stringRedisTemplate.opsForList().leftPop(key);
    }

    @Override
    public String rpop(String key) {
        return stringRedisTemplate.opsForList().rightPop(key);
    }

    @Override
    public Long llen(String key) {
        return stringRedisTemplate.opsForList().size(key);
    }

    @Override
    public String lindex(String key, long index) {
        return stringRedisTemplate.opsForList().index(key, index);
    }

    @Override
    public void lset(String key, long index, String value) {
        stringRedisTemplate.opsForList().set(key, index, value);
    }

    @Override
    public List<String> lrange(String key, long start, long end) {
        return stringRedisTemplate.opsForList().range(key, start, end);
    }

    @Override
    public void ltrim(String key, long start, long end) {
        stringRedisTemplate.opsForList().trim(key, start, end);
    }

    @Override
    public Long lrem(String key, long count, String value) {
        return stringRedisTemplate.opsForList().remove(key, count, value);
    }

    @Override
    public String rpoplpush(String source, String destination) {
        return stringRedisTemplate.opsForList().rightPopAndLeftPush(source, destination);
    }

    @Override
    public String blpop(long timeout, String... keys) {
        return stringRedisTemplate.opsForList().leftPop(keys[0], timeout, TimeUnit.SECONDS);
    }

    @Override
    public String brpop(long timeout, String... keys) {
        return stringRedisTemplate.opsForList().rightPop(keys[0], timeout, TimeUnit.SECONDS);
    }

    // ==================== 集合(Set)操作 ====================

    @Override
    public Long sadd(String key, String... members) {
        return stringRedisTemplate.opsForSet().add(key, members);
    }

    @Override
    public Long srem(String key, String... members) {
        return stringRedisTemplate.opsForSet().remove(key, (Object[]) members);
    }

    @Override
    public Set<String> smembers(String key) {
        return stringRedisTemplate.opsForSet().members(key);
    }

    @Override
    public boolean sismember(String key, String member) {
        return stringRedisTemplate.opsForSet().isMember(key, member);
    }

    @Override
    public Long scard(String key) {
        return stringRedisTemplate.opsForSet().size(key);
    }

    @Override
    public String spop(String key) {
        return stringRedisTemplate.opsForSet().pop(key);
    }

    @Override
    public String srandmember(String key) {
        return stringRedisTemplate.opsForSet().randomMember(key);
    }

    @Override
    public List<String> srandmember(String key, long count) {
        return stringRedisTemplate.opsForSet().randomMembers(key, count);
    }

    @Override
    public boolean smove(String source, String destination, String member) {
        return stringRedisTemplate.opsForSet().move(source, member, destination);
    }

    @Override
    public Set<String> sinter(String... keys) {
        return stringRedisTemplate.opsForSet().intersect(Arrays.asList(keys));
    }

    @Override
    public Set<String> sunion(String... keys) {
        return stringRedisTemplate.opsForSet().union(Arrays.asList(keys));
    }

    @Override
    public Set<String> sdiff(String... keys) {
        return stringRedisTemplate.opsForSet().difference(Arrays.asList(keys));
    }

    // ==================== 有序集合(Sorted Set)操作 ====================

    @Override
    public Long zadd(String key, Map<String, Double> memberScores) {
        Set<ZSetOperations.TypedTuple<String>> tuples = new HashSet<>();
        memberScores.forEach((member, score) -> 
            tuples.add(new DefaultTypedTuple<>(member, score)));
        return stringRedisTemplate.opsForZSet().add(key, tuples);
    }

    @Override
    public Long zadd(String key, String member, double score) {
        return stringRedisTemplate.opsForZSet().add(key, member, score) ? 1L : 0L;
    }

    @Override
    public Long zrem(String key, String... members) {
        return stringRedisTemplate.opsForZSet().remove(key, (Object[]) members);
    }

    @Override
    public Double zscore(String key, String member) {
        return stringRedisTemplate.opsForZSet().score(key, member);
    }

    @Override
    public Long zrank(String key, String member) {
        return stringRedisTemplate.opsForZSet().rank(key, member);
    }

    @Override
    public Long zrevrank(String key, String member) {
        return stringRedisTemplate.opsForZSet().reverseRank(key, member);
    }

    @Override
    public Long zcard(String key) {
        return stringRedisTemplate.opsForZSet().size(key);
    }

    @Override
    public Long zcount(String key, double min, double max) {
        return stringRedisTemplate.opsForZSet().count(key, min, max);
    }

    @Override
    public Set<String> zrange(String key, long start, long end) {
        return stringRedisTemplate.opsForZSet().range(key, start, end);
    }

    @Override
    public Map<String, Double> zrangeWithScores(String key, long start, long end) {
        Set<ZSetOperations.TypedTuple<String>> tuples = 
            stringRedisTemplate.opsForZSet().rangeWithScores(key, start, end);
        Map<String, Double> result = new LinkedHashMap<>();
        if (tuples != null) {
            tuples.forEach(tuple -> result.put(tuple.getValue(), tuple.getScore()));
        }
        return result;
    }

    @Override
    public Set<String> zrevrange(String key, long start, long end) {
        return stringRedisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    @Override
    public Map<String, Double> zrevrangeWithScores(String key, long start, long end) {
        Set<ZSetOperations.TypedTuple<String>> tuples = 
            stringRedisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
        Map<String, Double> result = new LinkedHashMap<>();
        if (tuples != null) {
            tuples.forEach(tuple -> result.put(tuple.getValue(), tuple.getScore()));
        }
        return result;
    }

    @Override
    public Set<String> zrangeByScore(String key, double min, double max) {
        return stringRedisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    @Override
    public Map<String, Double> zrangeByScoreWithScores(String key, double min, double max) {
        Set<ZSetOperations.TypedTuple<String>> tuples = 
            stringRedisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max);
        Map<String, Double> result = new LinkedHashMap<>();
        if (tuples != null) {
            tuples.forEach(tuple -> result.put(tuple.getValue(), tuple.getScore()));
        }
        return result;
    }

    @Override
    public Double zincrby(String key, double increment, String member) {
        return stringRedisTemplate.opsForZSet().incrementScore(key, member, increment);
    }

    @Override
    public Long zremrangeByRank(String key, long start, long end) {
        return stringRedisTemplate.opsForZSet().removeRange(key, start, end);
    }

    @Override
    public Long zremrangeByScore(String key, double min, double max) {
        return stringRedisTemplate.opsForZSet().removeRangeByScore(key, min, max);
    }

    // ==================== 键(Key)操作 ====================

    @Override
    public Long del(String... keys) {
        return stringRedisTemplate.delete(Arrays.asList(keys));
    }

    @Override
    public boolean exists(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    @Override
    public boolean expire(String key, long expire, TimeUnit unit) {
        return Boolean.TRUE.equals(stringRedisTemplate.expire(key, expire, unit));
    }

    @Override
    public boolean expireAt(String key, long timestamp) {
        return Boolean.TRUE.equals(stringRedisTemplate.expireAt(key, new Date(timestamp)));
    }

    @Override
    public boolean persist(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.persist(key));
    }

    @Override
    public Long ttl(String key) {
        return stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    @Override
    public Long pttl(String key) {
        return stringRedisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
    }

    @Override
    public String type(String key) {
        DataType dataType = stringRedisTemplate.type(key);
        return dataType != null ? dataType.name() : "none";
    }

    @Override
    public void rename(String oldKey, String newKey) {
        stringRedisTemplate.rename(oldKey, newKey);
    }

    @Override
    public boolean renamenx(String oldKey, String newKey) {
        return Boolean.TRUE.equals(stringRedisTemplate.renameIfAbsent(oldKey, newKey));
    }

    @Override
    public Set<String> keys(String pattern) {
        return stringRedisTemplate.keys(pattern);
    }

    @Override
    public String randomKey() {
        return stringRedisTemplate.randomKey();
    }

    @Override
    public boolean move(String key, int dbIndex) {
        return Boolean.TRUE.equals(stringRedisTemplate.move(key, dbIndex));
    }

    // ==================== 位图(Bitmap)操作 ====================

    @Override
    public boolean setbit(String key, long offset, boolean value) {
        return stringRedisTemplate.opsForValue().setBit(key, offset, value);
    }

    @Override
    public boolean getbit(String key, long offset) {
        return stringRedisTemplate.opsForValue().getBit(key, offset);
    }

    @Override
    public Long bitcount(String key) {
        return (Long) stringRedisTemplate.execute(
            (RedisCallback<Long>) connection -> connection.bitCount(key.getBytes())
        );
    }

    @Override
    public Long bitcount(String key, long start, long end) {
        return (Long) stringRedisTemplate.execute(
            (RedisCallback<Long>) connection -> connection.bitCount(key.getBytes(), start, end)
        );
    }

    // ==================== HyperLogLog操作 ====================

    @Override
    public Long pfadd(String key, String... elements) {
        return stringRedisTemplate.opsForHyperLogLog().add(key, elements);
    }

    @Override
    public Long pfcount(String... keys) {
        return stringRedisTemplate.opsForHyperLogLog().size(keys);
    }

    @Override
    public void pfmerge(String destKey, String... sourceKeys) {
        stringRedisTemplate.opsForHyperLogLog().union(destKey, sourceKeys);
    }

    // ==================== 事务和脚本操作 ====================

    @Override
    public Object exec(Runnable transaction) {
        return stringRedisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.multi();
            transaction.run();
            return connection.exec();
        });
    }

    @Override
    public Object eval(String script, List<String> keys, List<String> args) {
        return stringRedisTemplate.execute(
            (RedisCallback<Object>) connection -> {
                byte[] scriptBytes = script.getBytes();
                int numKeys = keys.size();
                byte[][] keysBytes = keys.stream().map(String::getBytes).toArray(byte[][]::new);
                return connection.eval(scriptBytes, ReturnType.VALUE, numKeys, keysBytes);
            }
        );
    }

    @Override
    public Object evalsha(String sha1, List<String> keys, List<String> args) {
        return stringRedisTemplate.execute(
            (RedisCallback<Object>) connection -> {
                int numKeys = keys.size();
                byte[][] keysBytes = keys.stream().map(String::getBytes).toArray(byte[][]::new);
                return connection.evalSha(sha1, ReturnType.VALUE, numKeys, keysBytes);
            }
        );
    }

    // ==================== 发布订阅操作 ====================

    @Override
    public Long publish(String channel, String message) {
        return stringRedisTemplate.convertAndSend(channel, message);
    }

    @Override
    public void subscribe(String... channels) {
        // 需要单独的连接来处理订阅
        throw new UnsupportedOperationException("请使用MessageListener进行订阅");
    }

    @Override
    public void psubscribe(String... patterns) {
        // 需要单独的连接来处理模式订阅
        throw new UnsupportedOperationException("请使用MessageListener进行模式订阅");
    }

    @Override
    public void unsubscribe(String... channels) {
        // 需要单独的连接来处理取消订阅
        throw new UnsupportedOperationException("请使用MessageListener取消订阅");
    }

    @Override
    public void punsubscribe(String... patterns) {
        // 需要单独的连接来处理取消模式订阅
        throw new UnsupportedOperationException("请使用MessageListener取消模式订阅");
    }

    // ==================== 数据库操作 ====================

    @Override
    public Long dbsize() {
        return stringRedisTemplate.execute((RedisCallback<Long>) connection -> connection.dbSize());
    }

    @Override
    public void flushdb() {
        stringRedisTemplate.execute((RedisCallback<Void>) connection -> {
            connection.flushDb();
            return null;
        });
    }

    @Override
    public void flushall() {
        stringRedisTemplate.execute((RedisCallback<Void>) connection -> {
            connection.flushAll();
            return null;
        });
    }

    @Override
    public void select(int dbIndex) {
        stringRedisTemplate.execute((RedisCallback<Void>) connection -> {
            connection.select(dbIndex);
            return null;
        });
    }

    // ==================== 其他实用方法 ====================

    @Override
    public Long delByPattern(String pattern) {
        Set<String> keys = keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            return stringRedisTemplate.delete(keys);
        }
        return 0L;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? (T) value : null;
    }

    @Override
    public <T> void set(String key, T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public <T> void setex(String key, T value, long expire, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, Expiration.from(expire, unit));
    }
}

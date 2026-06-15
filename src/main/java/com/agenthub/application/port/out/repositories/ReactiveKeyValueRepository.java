package com.agenthub.application.port.out.repositories;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 响应式键值存储服务端口接口，与 KeyValueRepository 一一对应。
 */
public interface ReactiveKeyValueRepository {

    // ==================== 字符串(String)操作 ====================

    /**
     * 设置键值对。
     */
    Mono<Void> set(String key, String value);

    /**
     * 设置键值对并设置过期时间。
     */
    Mono<Void> setex(String key, String value, long expire, TimeUnit unit);

    /**
     * 仅当 key 不存在时设置值。
     */
    Mono<Boolean> setnx(String key, String value);

    /**
     * 获取值。
     */
    Mono<String> get(String key);

    /**
     * 获取并设置新值。
     */
    Mono<String> getSet(String key, String value);

    /**
     * 批量获取。
     */
    Flux<String> mget(String... keys);

    /**
     * 批量设置。
     */
    Mono<Void> mset(Map<String, String> keyValueMap);

    /**
     * 追加值。
     */
    Mono<Void> append(String key, String value);

    /**
     * 获取字符串长度。
     */
    Mono<Long> strlen(String key);

    /**
     * 递增。
     */
    Mono<Long> incr(String key);

    /**
     * 递增指定值。
     */
    Mono<Long> incrBy(String key, long increment);

    /**
     * 递减。
     */
    Mono<Long> decr(String key);

    /**
     * 递减指定值。
     */
    Mono<Long> decrBy(String key, long decrement);

    // ==================== 哈希(Hash)操作 ====================

    /**
     * 设置哈希字段值。
     */
    Mono<Void> hset(String key, String field, String value);

    /**
     * 批量设置哈希字段。
     */
    Mono<Void> hmset(String key, Map<String, String> fieldValues);

    /**
     * 获取哈希字段值。
     */
    Mono<String> hget(String key, String field);

    /**
     * 批量获取哈希字段值。
     */
    Flux<String> hmget(String key, String... fields);

    /**
     * 获取所有哈希字段和值。
     */
    Mono<Map<String, String>> hgetAll(String key);

    /**
     * 删除哈希字段。
     */
    Mono<Long> hdel(String key, String... fields);

    /**
     * 判断哈希字段是否存在。
     */
    Mono<Boolean> hexists(String key, String field);

    /**
     * 获取哈希字段数量。
     */
    Mono<Long> hlen(String key);

    /**
     * 获取所有哈希字段名。
     */
    Flux<String> hkeys(String key);

    /**
     * 获取所有哈希字段值。
     */
    Flux<String> hvals(String key);

    /**
     * 仅当字段不存在时设置值。
     */
    Mono<Boolean> hsetnx(String key, String field, String value);

    /**
     * 哈希字段值递增。
     */
    Mono<Long> hincrBy(String key, String field, long increment);

    // ==================== 列表(List)操作 ====================

    /**
     * 左侧推入。
     */
    Mono<Long> lpush(String key, String... values);

    /**
     * 右侧推入。
     */
    Mono<Long> rpush(String key, String... values);

    /**
     * 左侧弹出。
     */
    Mono<String> lpop(String key);

    /**
     * 右侧弹出。
     */
    Mono<String> rpop(String key);

    /**
     * 获取列表长度。
     */
    Mono<Long> llen(String key);

    /**
     * 获取指定索引的元素。
     */
    Mono<String> lindex(String key, long index);

    /**
     * 设置指定索引的元素。
     */
    Mono<Void> lset(String key, long index, String value);

    /**
     * 获取列表范围内的元素。
     */
    Flux<String> lrange(String key, long start, long end);

    /**
     * 裁剪列表，只保留指定范围内的元素。
     */
    Mono<Void> ltrim(String key, long start, long end);

    /**
     * 移除列表中指定值的元素。
     */
    Mono<Long> lrem(String key, long count, String value);

    /**
     * 将源列表右侧弹出并推入目标列表左侧。
     */
    Mono<String> rpoplpush(String source, String destination);

    /**
     * 阻塞式左侧弹出。
     */
    Mono<String> blpop(long timeout, String... keys);

    /**
     * 阻塞式右侧弹出。
     */
    Mono<String> brpop(long timeout, String... keys);

    // ==================== 集合(Set)操作 ====================

    /**
     * 添加集合元素。
     */
    Mono<Long> sadd(String key, String... members);

    /**
     * 移除集合元素。
     */
    Mono<Long> srem(String key, String... members);

    /**
     * 获取集合所有元素。
     */
    Flux<String> smembers(String key);

    /**
     * 判断元素是否在集合中。
     */
    Mono<Boolean> sismember(String key, String member);

    /**
     * 获取集合元素数量。
     */
    Mono<Long> scard(String key);

    /**
     * 随机弹出一个元素。
     */
    Mono<String> spop(String key);

    /**
     * 随机获取一个元素。
     */
    Mono<String> srandmember(String key);

    /**
     * 随机获取多个元素。
     */
    Flux<String> srandmember(String key, long count);

    /**
     * 将元素从源集合移动到目标集合。
     */
    Mono<Boolean> smove(String source, String destination, String member);

    /**
     * 集合交集。
     */
    Flux<String> sinter(String... keys);

    /**
     * 集合并集。
     */
    Flux<String> sunion(String... keys);

    /**
     * 集合差集。
     */
    Flux<String> sdiff(String... keys);

    // ==================== 有序集合(Sorted Set)操作 ====================

    /**
     * 添加有序集合元素。
     */
    Mono<Long> zadd(String key, Map<String, Double> memberScores);

    /**
     * 添加单个有序集合元素。
     */
    Mono<Long> zadd(String key, String member, double score);

    /**
     * 移除有序集合元素。
     */
    Mono<Long> zrem(String key, String... members);

    /**
     * 获取元素分数。
     */
    Mono<Double> zscore(String key, String member);

    /**
     * 获取元素排名(从小到大)。
     */
    Mono<Long> zrank(String key, String member);

    /**
     * 获取元素排名(从大到小)。
     */
    Mono<Long> zrevrank(String key, String member);

    /**
     * 获取有序集合元素数量。
     */
    Mono<Long> zcard(String key);

    /**
     * 统计指定分数范围内的元素数量。
     */
    Mono<Long> zcount(String key, double min, double max);

    /**
     * 获取指定排名范围内的元素(从小到大)。
     */
    Flux<String> zrange(String key, long start, long end);

    /**
     * 获取指定排名范围内的元素和分数(从小到大)。
     */
    Mono<Map<String, Double>> zrangeWithScores(String key, long start, long end);

    /**
     * 获取指定排名范围内的元素(从大到小)。
     */
    Flux<String> zrevrange(String key, long start, long end);

    /**
     * 获取指定排名范围内的元素和分数(从大到小)。
     */
    Mono<Map<String, Double>> zrevrangeWithScores(String key, long start, long end);

    /**
     * 获取指定分数范围内的元素。
     */
    Flux<String> zrangeByScore(String key, double min, double max);

    /**
     * 获取指定分数范围内的元素和分数。
     */
    Mono<Map<String, Double>> zrangeByScoreWithScores(String key, double min, double max);

    /**
     * 递增元素分数。
     */
    Mono<Double> zincrby(String key, double increment, String member);

    /**
     * 移除指定排名范围内的元素。
     */
    Mono<Long> zremrangeByRank(String key, long start, long end);

    /**
     * 移除指定分数范围内的元素。
     */
    Mono<Long> zremrangeByScore(String key, double min, double max);

    // ==================== 键(Key)操作 ====================

    /**
     * 删除键。
     */
    Mono<Long> del(String... keys);

    /**
     * 判断键是否存在。
     */
    Mono<Boolean> exists(String key);

    /**
     * 设置键过期时间。
     */
    Mono<Boolean> expire(String key, long expire, TimeUnit unit);

    /**
     * 设置键在指定时间戳过期。
     */
    Mono<Boolean> expireAt(String key, long timestamp);

    /**
     * 移除键的过期时间。
     */
    Mono<Boolean> persist(String key);

    /**
     * 获取键的剩余生存时间(秒)。
     */
    Mono<Long> ttl(String key);

    /**
     * 获取键的剩余生存时间(毫秒)。
     */
    Mono<Long> pttl(String key);

    /**
     * 获取键的类型。
     */
    Mono<String> type(String key);

    /**
     * 重命名键。
     */
    Mono<Void> rename(String oldKey, String newKey);

    /**
     * 仅当新键不存在时重命名。
     */
    Mono<Boolean> renamenx(String oldKey, String newKey);

    /**
     * 查找匹配模式的所有键。
     */
    Flux<String> keys(String pattern);

    /**
     * 随机返回一个键。
     */
    Mono<String> randomKey();

    /**
     * 将键移动到指定数据库。
     */
    Mono<Boolean> move(String key, int dbIndex);

    // ==================== 位图(Bitmap)操作 ====================

    /**
     * 设置位的值。
     */
    Mono<Boolean> setbit(String key, long offset, boolean value);

    /**
     * 获取位的值。
     */
    Mono<Boolean> getbit(String key, long offset);

    /**
     * 统计值为1的位数。
     */
    Mono<Long> bitcount(String key);

    /**
     * 统计指定范围内值为1的位数。
     */
    Mono<Long> bitcount(String key, long start, long end);

    // ==================== HyperLogLog操作 ====================

    /**
     * 添加元素到HyperLogLog。
     */
    Mono<Long> pfadd(String key, String... elements);

    /**
     * 获取HyperLogLog的基数估算值。
     */
    Mono<Long> pfcount(String... keys);

    /**
     * 合并多个HyperLogLog。
     */
    Mono<Void> pfmerge(String destKey, String... sourceKeys);

    // ==================== 事务和脚本操作 ====================

    /**
     * 执行事务块。
     */
    Mono<Object> exec(Runnable transaction);

    /**
     * 执行 Lua 脚本。
     */
    Mono<Object> eval(String script, List<String> keys, List<String> args);

    /**
     * 执行 Lua 脚本(使用SHA1校验和)。
     */
    Mono<Object> evalsha(String sha1, List<String> keys, List<String> args);

    // ==================== 发布订阅操作 ====================

    /**
     * 发布消息。
     */
    Mono<Long> publish(String channel, String message);

    /**
     * 订阅频道。
     */
    Mono<Void> subscribe(String... channels);

    /**
     * 模式订阅。
     */
    Mono<Void> psubscribe(String... patterns);

    /**
     * 取消订阅。
     */
    Mono<Void> unsubscribe(String... channels);

    /**
     * 取消模式订阅。
     */
    Mono<Void> punsubscribe(String... patterns);

    // ==================== 数据库操作 ====================

    /**
     * 获取当前数据库大小(键的数量)。
     */
    Mono<Long> dbsize();

    /**
     * 清空当前数据库。
     */
    Mono<Void> flushdb();

    /**
     * 清空所有数据库。
     */
    Mono<Void> flushall();

    /**
     * 选择数据库。
     */
    Mono<Void> select(int dbIndex);

    // ==================== 其他实用方法 ====================

    /**
     * 批量删除匹配模式的所有键。
     */
    Mono<Long> delByPattern(String pattern);

    /**
     * 获取键的值并转换为指定类型。
     */
    <T> Mono<T> get(String key, Class<T> type);

    /**
     * 设置键值对(对象序列化)。
     */
    <T> Mono<Void> set(String key, T value);

    /**
     * 设置键值对并设置过期时间(对象序列化)。
     */
    <T> Mono<Void> setex(String key, T value, long expire, TimeUnit unit);
}

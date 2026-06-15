package com.agenthub.application.port.out.repositories;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 键值存储服务端口接口，支持类似Redis的所有核心功能
 * 
 * @author huangdayu
 */
public interface KeyValueRepository {

    // ==================== 字符串(String)操作 ====================

    /**
     * 设置键值对
     */
    void set(String key, String value);

    /**
     * 设置键值对并设置过期时间
     */
    void setex(String key, String value, long expire, TimeUnit unit);

    /**
     * 仅当key不存在时设置值
     * @return 设置成功返回true，key已存在返回false
     */
    boolean setnx(String key, String value);

    /**
     * 获取值
     */
    String get(String key);

    /**
     * 获取并设置新值
     */
    String getSet(String key, String value);

    /**
     * 批量获取
     */
    List<String> mget(String... keys);

    /**
     * 批量设置
     */
    void mset(Map<String, String> keyValueMap);

    /**
     * 追加值 
     */
    void append(String key, String value);

    /**
     * 获取字符串长度
     */
    Long strlen(String key);

    /**
     * 递增
     */
    Long incr(String key);

    /**
     * 递增指定值
     */
    Long incrBy(String key, long increment);

    /**
     * 递减
     */
    Long decr(String key);

    /**
     * 递减指定值
     */
    Long decrBy(String key, long decrement);

    // ==================== 哈希(Hash)操作 ====================

    /**
     * 设置哈希字段值
     */
    void hset(String key, String field, String value);

    /**
     * 批量设置哈希字段
     */
    void hmset(String key, Map<String, String> fieldValues);

    /**
     * 获取哈希字段值
     */
    String hget(String key, String field);

    /**
     * 批量获取哈希字段值
     */
    List<String> hmget(String key, String... fields);

    /**
     * 获取所有哈希字段和值
     */
    Map<String, String> hgetAll(String key);

    /**
     * 删除哈希字段
     */
    Long hdel(String key, String... fields);

    /**
     * 判断哈希字段是否存在
     */
    boolean hexists(String key, String field);

    /**
     * 获取哈希字段数量
     */
    Long hlen(String key);

    /**
     * 获取所有哈希字段名
     */
    Set<String> hkeys(String key);

    /**
     * 获取所有哈希字段值
     */
    List<String> hvals(String key);

    /**
     * 仅当字段不存在时设置值
     */
    boolean hsetnx(String key, String field, String value);

    /**
     * 哈希字段值递增
     */
    Long hincrBy(String key, String field, long increment);

    // ==================== 列表(List)操作 ====================

    /**
     * 左侧推入
     */
    Long lpush(String key, String... values);

    /**
     * 右侧推入
     */
    Long rpush(String key, String... values);

    /**
     * 左侧弹出
     */
    String lpop(String key);

    /**
     * 右侧弹出
     */
    String rpop(String key);

    /**
     * 获取列表长度
     */
    Long llen(String key);

    /**
     * 获取指定索引的元素
     */
    String lindex(String key, long index);

    /**
     * 设置指定索引的元素
     */
    void lset(String key, long index, String value);

    /**
     * 获取列表范围内的元素
     */
    List<String> lrange(String key, long start, long end);

    /**
     * 裁剪列表，只保留指定范围内的元素
     */
    void ltrim(String key, long start, long end);

    /**
     * 移除列表中指定值的元素
     */
    Long lrem(String key, long count, String value);

    /**
     * 将源列表右侧弹出并推入目标列表左侧
     */
    String rpoplpush(String source, String destination);

    /**
     * 阻塞式左侧弹出
     */
    String blpop(long timeout, String... keys);

    /**
     * 阻塞式右侧弹出
     */
    String brpop(long timeout, String... keys);

    // ==================== 集合(Set)操作 ====================

    /**
     * 添加集合元素
     */
    Long sadd(String key, String... members);

    /**
     * 移除集合元素
     */
    Long srem(String key, String... members);

    /**
     * 获取集合所有元素
     */
    Set<String> smembers(String key);

    /**
     * 判断元素是否在集合中
     */
    boolean sismember(String key, String member);

    /**
     * 获取集合元素数量
     */
    Long scard(String key);

    /**
     * 随机弹出一个元素
     */
    String spop(String key);

    /**
     * 随机获取一个元素
     */
    String srandmember(String key);

    /**
     * 随机获取多个元素
     */
    List<String> srandmember(String key, long count);

    /**
     * 将元素从源集合移动到目标集合
     */
    boolean smove(String source, String destination, String member);

    /**
     * 集合交集
     */
    Set<String> sinter(String... keys);

    /**
     * 集合并集
     */
    Set<String> sunion(String... keys);

    /**
     * 集合差集
     */
    Set<String> sdiff(String... keys);

    // ==================== 有序集合(Sorted Set)操作 ====================

    /**
     * 添加有序集合元素
     */
    Long zadd(String key, Map<String, Double> memberScores);

    /**
     * 添加单个有序集合元素
     */
    Long zadd(String key, String member, double score);

    /**
     * 移除有序集合元素
     */
    Long zrem(String key, String... members);

    /**
     * 获取元素分数
     */
    Double zscore(String key, String member);

    /**
     * 获取元素排名(从小到大)
     */
    Long zrank(String key, String member);

    /**
     * 获取元素排名(从大到小)
     */
    Long zrevrank(String key, String member);

    /**
     * 获取有序集合元素数量
     */
    Long zcard(String key);

    /**
     * 统计指定分数范围内的元素数量
     */
    Long zcount(String key, double min, double max);

    /**
     * 获取指定排名范围内的元素(从小到大)
     */
    Set<String> zrange(String key, long start, long end);

    /**
     * 获取指定排名范围内的元素和分数(从小到大)
     */
    Map<String, Double> zrangeWithScores(String key, long start, long end);

    /**
     * 获取指定排名范围内的元素(从大到小)
     */
    Set<String> zrevrange(String key, long start, long end);

    /**
     * 获取指定排名范围内的元素和分数(从大到小)
     */
    Map<String, Double> zrevrangeWithScores(String key, long start, long end);

    /**
     * 获取指定分数范围内的元素
     */
    Set<String> zrangeByScore(String key, double min, double max);

    /**
     * 获取指定分数范围内的元素和分数
     */
    Map<String, Double> zrangeByScoreWithScores(String key, double min, double max);

    /**
     * 递增元素分数
     */
    Double zincrby(String key, double increment, String member);

    /**
     * 移除指定排名范围内的元素
     */
    Long zremrangeByRank(String key, long start, long end);

    /**
     * 移除指定分数范围内的元素
     */
    Long zremrangeByScore(String key, double min, double max);

    // ==================== 键(Key)操作 ====================

    /**
     * 删除键
     */
    Long del(String... keys);

    /**
     * 判断键是否存在
     */
    boolean exists(String key);

    /**
     * 设置键过期时间
     */
    boolean expire(String key, long expire, TimeUnit unit);

    /**
     * 设置键在指定时间戳过期
     */
    boolean expireAt(String key, long timestamp);

    /**
     * 移除键的过期时间
     */
    boolean persist(String key);

    /**
     * 获取键的剩余生存时间(秒)
     */
    Long ttl(String key);

    /**
     * 获取键的剩余生存时间(毫秒)
     */
    Long pttl(String key);

    /**
     * 获取键的类型
     */
    String type(String key);

    /**
     * 重命名键
     */
    void rename(String oldKey, String newKey);

    /**
     * 仅当新键不存在时重命名
     */
    boolean renamenx(String oldKey, String newKey);

    /**
     * 查找匹配模式的所有键
     */
    Set<String> keys(String pattern);

    /**
     * 随机返回一个键
     */
    String randomKey();

    /**
     * 将键移动到指定数据库
     */
    boolean move(String key, int dbIndex);

    // ==================== 位图(Bitmap)操作 ====================

    /**
     * 设置位的值
     */
    boolean setbit(String key, long offset, boolean value);

    /**
     * 获取位的值
     */
    boolean getbit(String key, long offset);

    /**
     * 统计值为1的位数
     */
    Long bitcount(String key);

    /**
     * 统计指定范围内值为1的位数
     */
    Long bitcount(String key, long start, long end);

    // ==================== HyperLogLog操作 ====================

    /**
     * 添加元素到HyperLogLog
     */
    Long pfadd(String key, String... elements);

    /**
     * 获取HyperLogLog的基数估算值
     */
    Long pfcount(String... keys);

    /**
     * 合并多个HyperLogLog
     */
    void pfmerge(String destKey, String... sourceKeys);

    // ==================== 事务和脚本操作 ====================

    /**
     * 执行事务块
     */
    Object exec(Runnable transaction);

    /**
     * 执行Lua脚本
     */
    Object eval(String script, List<String> keys, List<String> args);

    /**
     * 执行Lua脚本(使用SHA1校验和)
     */
    Object evalsha(String sha1, List<String> keys, List<String> args);

    // ==================== 发布订阅操作 ====================

    /**
     * 发布消息
     */
    Long publish(String channel, String message);

    /**
     * 订阅频道
     */
    void subscribe(String... channels);

    /**
     * 模式订阅
     */
    void psubscribe(String... patterns);

    /**
     * 取消订阅
     */
    void unsubscribe(String... channels);

    /**
     * 取消模式订阅
     */
    void punsubscribe(String... patterns);

    // ==================== 数据库操作 ====================

    /**
     * 获取当前数据库大小(键的数量)
     */
    Long dbsize();

    /**
     * 清空当前数据库
     */
    void flushdb();

    /**
     * 清空所有数据库
     */
    void flushall();

    /**
     * 选择数据库
     */
    void select(int dbIndex);

    // ==================== 其他实用方法 ====================

    /**
     * 批量删除匹配模式的所有键
     */
    Long delByPattern(String pattern);

    /**
     * 获取键的值并转换为指定类型
     */
    <T> T get(String key, Class<T> type);

    /**
     * 设置键值对(对象序列化)
     */
    <T> void set(String key, T value);

    /**
     * 设置键值对并设置过期时间(对象序列化)
     */
    <T> void setex(String key, T value, long expire, TimeUnit unit);
}

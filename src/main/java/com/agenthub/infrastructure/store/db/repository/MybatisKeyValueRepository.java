package com.agenthub.infrastructure.store.db.repository;

import com.agenthub.application.port.out.repositories.KeyValueRepository;
import com.agenthub.infrastructure.store.db.entity.*;
import com.agenthub.infrastructure.store.db.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 基于MyBatis-Plus的键值存储服务实现
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "agenthub.kv.type", havingValue = "database")
public class MybatisKeyValueRepository implements KeyValueRepository {

    private final KvStoreMapper storeMapper;
    private final KvHashMapper hashMapper;
    private final KvListMapper listMapper;
    private final KvSetMapper setMapper;
    private final KvZsetMapper zsetMapper;

    // ==================== 字符串操作 ====================

    @Override
    public void set(String key, String value) {
        KvStore store = createKvStore(key, value, "string");
        storeMapper.insertOrUpdate(store);
    }

    @Override
    public void setex(String key, String value, long expire, TimeUnit unit) {
        long expireTime = System.currentTimeMillis() + unit.toMillis(expire);
        KvStore store = createKvStore(key, value, "string");
        store.setExpireTime(expireTime);
        storeMapper.insertOrUpdate(store);
    }

    @Override
    public boolean setnx(String key, String value) {
        if (exists(key)) return false;
        set(key, value);
        return true;
    }

    @Override
    public String get(String key) {
        cleanExpiredKeys();
        QueryWrapper<KvStore> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key).eq("kv_type", "string");
        KvStore store = storeMapper.selectOne(wrapper);
        return store != null ? store.getKvValue() : null;
    }

    @Override
    public String getSet(String key, String value) {
        String oldValue = get(key);
        set(key, value);
        return oldValue;
    }

    @Override
    public List<String> mget(String... keys) {
        return Arrays.stream(keys).map(this::get).collect(Collectors.toList());
    }

    @Override
    public void mset(Map<String, String> keyValueMap) {
        keyValueMap.forEach(this::set);
    }

    @Override
    public void append(String key, String value) {
        String old = get(key);
        set(key, old != null ? old + value : value);
    }

    @Override
    public Long strlen(String key) {
        String value = get(key);
        return value != null ? (long) value.length() : 0L;
    }

    @Override
    public Long incr(String key) {
        return incrBy(key, 1);
    }

    @Override
    public Long incrBy(String key, long increment) {
        String value = get(key);
        long newValue = (value != null ? Long.parseLong(value) : 0) + increment;
        set(key, String.valueOf(newValue));
        return newValue;
    }

    @Override
    public Long decr(String key) {
        return decrBy(key, 1);
    }

    @Override
    public Long decrBy(String key, long decrement) {
        return incrBy(key, -decrement);
    }

    // ==================== 哈希操作 ====================

    @Override
    public void hset(String key, String field, String value) {
        KvHash hash = new KvHash();
        hash.setKvKey(key);
        hash.setField(field);
        hash.setKvValue(value);
        hashMapper.insertOrUpdate(hash);
    }

    @Override
    public void hmset(String key, Map<String, String> fieldValues) {
        fieldValues.forEach((field, value) -> hset(key, field, value));
    }

    @Override
    public String hget(String key, String field) {
        cleanExpiredKeys();
        QueryWrapper<KvHash> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key).eq("field", field);
        KvHash hash = hashMapper.selectOne(wrapper);
        return hash != null ? hash.getKvValue() : null;
    }

    @Override
    public List<String> hmget(String key, String... fields) {
        return Arrays.stream(fields).map(f -> hget(key, f)).collect(Collectors.toList());
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        cleanExpiredKeys();
        QueryWrapper<KvHash> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key);
        return hashMapper.selectList(wrapper).stream()
                .collect(Collectors.toMap(KvHash::getField, KvHash::getKvValue));
    }

    @Override
    public Long hdel(String key, String... fields) {
        QueryWrapper<KvHash> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key).in("field", Arrays.asList(fields));
        return (long) hashMapper.delete(wrapper);
    }

    @Override
    public boolean hexists(String key, String field) {
        return hget(key, field) != null;
    }

    @Override
    public Long hlen(String key) {
        QueryWrapper<KvHash> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key);
        return hashMapper.selectCount(wrapper);
    }

    @Override
    public Set<String> hkeys(String key) {
        QueryWrapper<KvHash> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key);
        return hashMapper.selectList(wrapper).stream()
                .map(KvHash::getField).collect(Collectors.toSet());
    }

    @Override
    public List<String> hvals(String key) {
        QueryWrapper<KvHash> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key);
        return hashMapper.selectList(wrapper).stream()
                .map(KvHash::getKvValue).collect(Collectors.toList());
    }

    @Override
    public boolean hsetnx(String key, String field, String value) {
        if (hexists(key, field)) return false;
        hset(key, field, value);
        return true;
    }

    @Override
    public Long hincrBy(String key, String field, long increment) {
        String value = hget(key, field);
        long newValue = (value != null ? Long.parseLong(value) : 0) + increment;
        hset(key, field, String.valueOf(newValue));
        return newValue;
    }

    // ==================== 列表操作 ====================

    @Override
    public Long lpush(String key, String... values) {
        Long minIndex = getMinIndex(key);
        for (int i = 0; i < values.length; i++) {
            insertListItem(key, minIndex - i, values[i]);
        }
        return llen(key);
    }

    @Override
    public Long rpush(String key, String... values) {
        Long maxIndex = getMaxIndex(key);
        for (int i = 0; i < values.length; i++) {
            insertListItem(key, maxIndex + 1 + i, values[i]);
        }
        return llen(key);
    }

    @Override
    public String lpop(String key) {
        QueryWrapper<KvList> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key).orderByAsc("list_index").last("LIMIT 1");
        KvList item = listMapper.selectOne(wrapper);
        if (item == null) return null;
        listMapper.deleteById(item);
        return item.getKvValue();
    }

    @Override
    public String rpop(String key) {
        QueryWrapper<KvList> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key).orderByDesc("list_index").last("LIMIT 1");
        KvList item = listMapper.selectOne(wrapper);
        if (item == null) return null;
        listMapper.deleteById(item);
        return item.getKvValue();
    }

    @Override
    public Long llen(String key) {
        QueryWrapper<KvList> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key);
        return listMapper.selectCount(wrapper);
    }

    @Override
    public String lindex(String key, long index) {
        List<String> list = lrange(key, index, index);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public void lset(String key, long index, String value) {
        QueryWrapper<KvList> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key).orderByAsc("list_index").last("LIMIT 1 OFFSET " + index);
        KvList item = listMapper.selectOne(wrapper);
        if (item != null) {
            item.setKvValue(value);
            listMapper.updateById(item);
        }
    }

    @Override
    public List<String> lrange(String key, long start, long end) {
        QueryWrapper<KvList> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key).orderByAsc("list_index")
               .last("LIMIT " + (end - start + 1) + " OFFSET " + start);
        return listMapper.selectList(wrapper).stream()
                .map(KvList::getKvValue).collect(Collectors.toList());
    }

    @Override
    public void ltrim(String key, long start, long end) {
        List<String> list = lrange(key, start, end);
        QueryWrapper<KvList> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key);
        listMapper.delete(wrapper);
        for (int i = 0; i < list.size(); i++) {
            insertListItem(key, (long) i, list.get(i));
        }
    }

    @Override
    public Long lrem(String key, long count, String value) {
        QueryWrapper<KvList> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key).eq("kv_value", value);
        if (count > 0) wrapper.last("LIMIT " + count);
        return (long) listMapper.delete(wrapper);
    }

    @Override
    public String rpoplpush(String source, String destination) {
        String value = rpop(source);
        if (value != null) lpush(destination, value);
        return value;
    }

    @Override
    public String blpop(long timeout, String... keys) {
        return blockPop(timeout, keys, true);
    }

    @Override
    public String brpop(long timeout, String... keys) {
        return blockPop(timeout, keys, false);
    }

    // ==================== 集合操作 ====================

    @Override
    public Long sadd(String key, String... members) {
        long added = 0;
        for (String member : members) {
            KvSet set = new KvSet();
            set.setKvKey(key);
            set.setMember(member);
            added += setMapper.insertOrUpdate(set) ? 1 : 0;
        }
        return added;
    }

    @Override
    public Long srem(String key, String... members) {
        QueryWrapper<KvSet> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key).in("member", Arrays.asList(members));
        return (long) setMapper.delete(wrapper);
    }

    @Override
    public Set<String> smembers(String key) {
        QueryWrapper<KvSet> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key);
        return setMapper.selectList(wrapper).stream()
                .map(KvSet::getMember).collect(Collectors.toSet());
    }

    @Override
    public boolean sismember(String key, String member) {
        QueryWrapper<KvSet> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key).eq("member", member);
        return setMapper.selectCount(wrapper) > 0;
    }

    @Override
    public Long scard(String key) {
        QueryWrapper<KvSet> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key);
        return setMapper.selectCount(wrapper);
    }

    @Override
    public String spop(String key) {
        QueryWrapper<KvSet> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key).last("LIMIT 1");
        KvSet set = setMapper.selectOne(wrapper);
        if (set == null) return null;
        setMapper.deleteById(set);
        return set.getMember();
    }

    @Override
    public String srandmember(String key) {
        QueryWrapper<KvSet> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key).last("ORDER BY RAND() LIMIT 1");
        KvSet set = setMapper.selectOne(wrapper);
        return set != null ? set.getMember() : null;
    }

    @Override
    public List<String> srandmember(String key, long count) {
        QueryWrapper<KvSet> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key).last("ORDER BY RAND() LIMIT " + count);
        return setMapper.selectList(wrapper).stream()
                .map(KvSet::getMember).collect(Collectors.toList());
    }

    @Override
    public boolean smove(String source, String destination, String member) {
        if (srem(source, member) > 0) {
            sadd(destination, member);
            return true;
        }
        return false;
    }

    @Override
    public Set<String> sinter(String... keys) {
        Set<String> result = smembers(keys[0]);
        for (int i = 1; i < keys.length; i++) {
            result.retainAll(smembers(keys[i]));
        }
        return result;
    }

    @Override
    public Set<String> sunion(String... keys) {
        Set<String> result = new HashSet<>();
        for (String key : keys) result.addAll(smembers(key));
        return result;
    }

    @Override
    public Set<String> sdiff(String... keys) {
        Set<String> result = smembers(keys[0]);
        for (int i = 1; i < keys.length; i++) {
            result.removeAll(smembers(keys[i]));
        }
        return result;
    }

    // ==================== 有序集合操作 ====================

    @Override
    public Long zadd(String key, Map<String, Double> memberScores) {
        long added = 0;
        for (Map.Entry<String, Double> entry : memberScores.entrySet()) {
            added += zaddSingle(key, entry.getKey(), entry.getValue());
        }
        return added;
    }

    @Override
    public Long zadd(String key, String member, double score) {
        return zaddSingle(key, member, score);
    }

    @Override
    public Long zrem(String key, String... members) {
        QueryWrapper<KvZset> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key).in("member", Arrays.asList(members));
        return (long) zsetMapper.delete(wrapper);
    }

    @Override
    public Double zscore(String key, String member) {
        QueryWrapper<KvZset> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key).eq("member", member);
        KvZset zset = zsetMapper.selectOne(wrapper);
        return zset != null ? zset.getScore() : null;
    }

    @Override
    public Long zrank(String key, String member) {
        Double score = zscore(key, member);
        if (score == null) return null;
        QueryWrapper<KvZset> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key).lt("score", score);
        return zsetMapper.selectCount(wrapper);
    }

    @Override
    public Long zrevrank(String key, String member) {
        Long rank = zrank(key, member);
        Long card = zcard(key);
        return rank != null && card != null ? card - rank - 1 : null;
    }

    @Override
    public Long zcard(String key) {
        QueryWrapper<KvZset> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key);
        return zsetMapper.selectCount(wrapper);
    }

    @Override
    public Long zcount(String key, double min, double max) {
        QueryWrapper<KvZset> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key).between("score", min, max);
        return zsetMapper.selectCount(wrapper);
    }

    @Override
    public Set<String> zrange(String key, long start, long end) {
        QueryWrapper<KvZset> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key).orderByAsc("score")
               .last("LIMIT " + (end - start + 1) + " OFFSET " + start);
        return zsetMapper.selectList(wrapper).stream()
                .map(KvZset::getMember).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Map<String, Double> zrangeWithScores(String key, long start, long end) {
        QueryWrapper<KvZset> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key).orderByAsc("score")
               .last("LIMIT " + (end - start + 1) + " OFFSET " + start);
        return zsetMapper.selectList(wrapper).stream()
                .collect(Collectors.toMap(KvZset::getMember, KvZset::getScore,
                        (a, b) -> a, LinkedHashMap::new));
    }

    @Override
    public Set<String> zrevrange(String key, long start, long end) {
        QueryWrapper<KvZset> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key).orderByDesc("score")
               .last("LIMIT " + (end - start + 1) + " OFFSET " + start);
        return zsetMapper.selectList(wrapper).stream()
                .map(KvZset::getMember).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Map<String, Double> zrevrangeWithScores(String key, long start, long end) {
        QueryWrapper<KvZset> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key).orderByDesc("score")
               .last("LIMIT " + (end - start + 1) + " OFFSET " + start);
        return zsetMapper.selectList(wrapper).stream()
                .collect(Collectors.toMap(KvZset::getMember, KvZset::getScore,
                        (a, b) -> a, LinkedHashMap::new));
    }

    @Override
    public Set<String> zrangeByScore(String key, double min, double max) {
        QueryWrapper<KvZset> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key).between("score", min, max).orderByAsc("score");
        return zsetMapper.selectList(wrapper).stream()
                .map(KvZset::getMember).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Map<String, Double> zrangeByScoreWithScores(String key, double min, double max) {
        QueryWrapper<KvZset> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key).between("score", min, max).orderByAsc("score");
        return zsetMapper.selectList(wrapper).stream()
                .collect(Collectors.toMap(KvZset::getMember, KvZset::getScore,
                        (a, b) -> a, LinkedHashMap::new));
    }

    @Override
    public Double zincrby(String key, double increment, String member) {
        Double score = zscore(key, member);
        double newScore = (score != null ? score : 0) + increment;
        zadd(key, member, newScore);
        return newScore;
    }

    @Override
    public Long zremrangeByRank(String key, long start, long end) {
        Set<String> members = zrange(key, start, end);
        return zrem(key, members.toArray(new String[0]));
    }

    @Override
    public Long zremrangeByScore(String key, double min, double max) {
        QueryWrapper<KvZset> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key).between("score", min, max);
        return (long) zsetMapper.delete(wrapper);
    }

    // ==================== 键操作 ====================

    @Override
    public Long del(String... keys) {
        long count = 0;
        for (String key : keys) {
            count += deleteByKey(storeMapper, key);
            count += deleteByKey(hashMapper, key);
            count += deleteByKey(listMapper, key);
            count += deleteByKey(setMapper, key);
            count += deleteByKey(zsetMapper, key);
        }
        return count;
    }

    @Override
    public boolean exists(String key) {
        cleanExpiredKeys();
        QueryWrapper<KvStore> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key);
        return storeMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean expire(String key, long expire, TimeUnit unit) {
        long expireTime = System.currentTimeMillis() + unit.toMillis(expire);
        UpdateWrapper<KvStore> wrapper = new UpdateWrapper<>();
        wrapper.eq("kv_key", key).set("expire_time", expireTime);
        return storeMapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean expireAt(String key, long timestamp) {
        UpdateWrapper<KvStore> wrapper = new UpdateWrapper<>();
        wrapper.eq("kv_key", key).set("expire_time", timestamp);
        return storeMapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean persist(String key) {
        UpdateWrapper<KvStore> wrapper = new UpdateWrapper<>();
        wrapper.eq("kv_key", key).set("expire_time", null);
        return storeMapper.update(null, wrapper) > 0;
    }

    @Override
    public Long ttl(String key) {
        Long pttl = pttl(key);
        return pttl != null && pttl > 0 ? pttl / 1000 : pttl;
    }

    @Override
    public Long pttl(String key) {
        QueryWrapper<KvStore> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key).select("expire_time");
        KvStore store = storeMapper.selectOne(wrapper);
        if (store == null || store.getExpireTime() == null) return -1L;
        long remaining = store.getExpireTime() - System.currentTimeMillis();
        return remaining > 0 ? remaining : -2L;
    }

    @Override
    public String type(String key) {
        QueryWrapper<KvStore> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key).select("kv_type");
        KvStore store = storeMapper.selectOne(wrapper);
        return store != null ? store.getKvType() : "none";
    }

    @Override
    public void rename(String oldKey, String newKey) {
        updateKey(storeMapper, oldKey, newKey);
        updateKey(hashMapper, oldKey, newKey);
        updateKey(listMapper, oldKey, newKey);
        updateKey(setMapper, oldKey, newKey);
        updateKey(zsetMapper, oldKey, newKey);
    }

    @Override
    public boolean renamenx(String oldKey, String newKey) {
        if (exists(newKey)) return false;
        rename(oldKey, newKey);
        return true;
    }

    @Override
    public Set<String> keys(String pattern) {
        String sqlPattern = pattern.replace("*", "%").replace("?", "_");
        QueryWrapper<KvStore> wrapper = new QueryWrapper<>();
        wrapper.likeRight("kv_key", sqlPattern.replace("%", ""));
        return storeMapper.selectList(wrapper).stream()
                .map(KvStore::getKvKey).collect(Collectors.toSet());
    }

    @Override
    public String randomKey() {
        QueryWrapper<KvStore> wrapper = new QueryWrapper<>();
        wrapper.last("ORDER BY RAND() LIMIT 1");
        KvStore store = storeMapper.selectOne(wrapper);
        return store != null ? store.getKvKey() : null;
    }

    @Override
    public boolean move(String key, int dbIndex) {
        throw new UnsupportedOperationException("DB实现不支持多数据库");
    }

    // ==================== 其他操作 ====================

    @Override
    public Long dbsize() {
        return storeMapper.selectCount(null);
    }

    @Override
    public void flushdb() {
        storeMapper.delete(null);
        hashMapper.delete(null);
        listMapper.delete(null);
        setMapper.delete(null);
        zsetMapper.delete(null);
    }

    @Override
    public void flushall() {
        flushdb();
    }

    @Override
    public void select(int dbIndex) {
        throw new UnsupportedOperationException("DB实现不支持多数据库");
    }

    @Override
    public Long delByPattern(String pattern) {
        Set<String> keys = keys(pattern);
        return keys.isEmpty() ? 0L : del(keys.toArray(new String[0]));
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        String value = get(key);
        return value != null ? convertValue(value, type) : null;
    }

    @Override
    public <T> void set(String key, T value) {
        set(key, String.valueOf(value));
    }

    @Override
    public <T> void setex(String key, T value, long expire, TimeUnit unit) {
        setex(key, String.valueOf(value), expire, unit);
    }

    // ==================== 不支持的操作 ====================

    @Override
    public boolean setbit(String key, long offset, boolean value) {
        throw new UnsupportedOperationException("DB实现不支持位图操作");
    }

    @Override
    public boolean getbit(String key, long offset) {
        throw new UnsupportedOperationException("DB实现不支持位图操作");
    }

    @Override
    public Long bitcount(String key) {
        throw new UnsupportedOperationException("DB实现不支持位图操作");
    }

    @Override
    public Long bitcount(String key, long start, long end) {
        throw new UnsupportedOperationException("DB实现不支持位图操作");
    }

    @Override
    public Long pfadd(String key, String... elements) {
        throw new UnsupportedOperationException("DB实现不支持HyperLogLog操作");
    }

    @Override
    public Long pfcount(String... keys) {
        throw new UnsupportedOperationException("DB实现不支持HyperLogLog操作");
    }

    @Override
    public void pfmerge(String destKey, String... sourceKeys) {
        throw new UnsupportedOperationException("DB实现不支持HyperLogLog操作");
    }

    @Override
    public Object exec(Runnable transaction) {
        transaction.run();
        return null;
    }

    @Override
    public Object eval(String script, List<String> keys, List<String> args) {
        throw new UnsupportedOperationException("DB实现不支持Lua脚本");
    }

    @Override
    public Object evalsha(String sha1, List<String> keys, List<String> args) {
        throw new UnsupportedOperationException("DB实现不支持Lua脚本");
    }

    @Override
    public Long publish(String channel, String message) {
        throw new UnsupportedOperationException("DB实现不支持发布订阅");
    }

    @Override
    public void subscribe(String... channels) {
        throw new UnsupportedOperationException("DB实现不支持发布订阅");
    }

    @Override
    public void psubscribe(String... patterns) {
        throw new UnsupportedOperationException("DB实现不支持发布订阅");
    }

    @Override
    public void unsubscribe(String... channels) {
        throw new UnsupportedOperationException("DB实现不支持发布订阅");
    }

    @Override
    public void punsubscribe(String... patterns) {
        throw new UnsupportedOperationException("DB实现不支持发布订阅");
    }

    // ==================== 辅助方法 ====================

    private KvStore createKvStore(String key, String value, String type) {
        KvStore store = new KvStore();
        store.setKvKey(key);
        store.setKvValue(value);
        store.setKvType(type);
        return store;
    }

    private void cleanExpiredKeys() {
        QueryWrapper<KvStore> wrapper = new QueryWrapper<>();
        wrapper.isNotNull("expire_time").lt("expire_time", System.currentTimeMillis());
        storeMapper.selectList(wrapper).forEach(storeMapper::deleteById);
    }

    private void insertListItem(String key, Long index, String value) {
        KvList item = new KvList();
        item.setKvKey(key);
        item.setListIndex(index);
        item.setKvValue(value);
        listMapper.insert(item);
    }

    private Long getMinIndex(String key) {
        QueryWrapper<KvList> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key).orderByAsc("list_index").last("LIMIT 1");
        KvList item = listMapper.selectOne(wrapper);
        return item != null ? item.getListIndex() - 1 : 0L;
    }

    private Long getMaxIndex(String key) {
        QueryWrapper<KvList> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key).orderByDesc("list_index").last("LIMIT 1");
        KvList item = listMapper.selectOne(wrapper);
        return item != null ? item.getListIndex() : -1L;
    }

    private Long zaddSingle(String key, String member, Double score) {
        KvZset zset = new KvZset();
        zset.setKvKey(key);
        zset.setMember(member);
        zset.setScore(score);
        return zsetMapper.insertOrUpdate(zset) ? 1L : 0L;
    }

    private String blockPop(long timeout, String[] keys, boolean left) {
        long endTime = System.currentTimeMillis() + timeout * 1000;
        while (System.currentTimeMillis() < endTime) {
            for (String key : keys) {
                String value = left ? lpop(key) : rpop(key);
                if (value != null) return value;
            }
            sleep(100);
        }
        return null;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private <T> long deleteByKey(com.baomidou.mybatisplus.core.mapper.BaseMapper<T> mapper, String key) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.eq("kv_key", key);
        return mapper.delete(wrapper);
    }

    private <T> void updateKey(com.baomidou.mybatisplus.core.mapper.BaseMapper<T> mapper, String oldKey, String newKey) {
        UpdateWrapper<T> wrapper = new UpdateWrapper<>();
        wrapper.eq("kv_key", oldKey).set("kv_key", newKey);
        mapper.update(null, wrapper);
    }

    @SuppressWarnings("unchecked")
    private <T> T convertValue(String value, Class<T> type) {
        if (type == String.class) return (T) value;
        if (type == Integer.class) return (T) Integer.valueOf(value);
        if (type == Long.class) return (T) Long.valueOf(value);
        if (type == Double.class) return (T) Double.valueOf(value);
        if (type == Boolean.class) return (T) Boolean.valueOf(value);
        return (T) value;
    }
}

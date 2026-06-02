package com.agenthub.infrastructure.store.cache;

import com.agenthub.application.port.out.repositories.KeyValueRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 基于Map的内存键值存储服务实现
 * 
 * @author huangdayu
 */
@Component
@ConditionalOnProperty(name = "agenthub.kv.type", havingValue = "map", matchIfMissing = true)
public class MapKeyValueRepository implements KeyValueRepository {

    private final Map<String, Object> store = new ConcurrentHashMap<>();
    private final Map<String, Long> expireMap = new ConcurrentHashMap<>();

    // ==================== 字符串(String)操作 ====================

    @Override
    public void set(String key, String value) {
        store.put(key, value);
    }

    @Override
    public void setex(String key, String value, long expire, TimeUnit unit) {
        store.put(key, value);
        expireMap.put(key, System.currentTimeMillis() + unit.toMillis(expire));
    }

    @Override
    public boolean setnx(String key, String value) {
        if (isExpired(key)) {
            store.remove(key);
            expireMap.remove(key);
        }
        if (!store.containsKey(key)) {
            store.put(key, value);
            return true;
        }
        return false;
    }

    @Override
    public String get(String key) {
        if (isExpired(key)) {
            store.remove(key);
            expireMap.remove(key);
            return null;
        }
        Object value = store.get(key);
        return value != null ? value.toString() : null;
    }

    @Override
    public String getSet(String key, String value) {
        String oldValue = get(key);
        set(key, value);
        return oldValue;
    }

    @Override
    public List<String> mget(String... keys) {
        return Arrays.stream(keys)
                .map(this::get)
                .collect(Collectors.toList());
    }

    @Override
    public void mset(Map<String, String> keyValueMap) {
        keyValueMap.forEach(this::set);
    }

    @Override
    public void append(String key, String value) {
        String existing = get(key);
        set(key, existing != null ? existing + value : value);
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

    // ==================== 哈希(Hash)操作 ====================

    @Override
    @SuppressWarnings("unchecked")
    public void hset(String key, String field, String value) {
        Map<String, String> hash = (Map<String, String>) store.computeIfAbsent(key, k -> new ConcurrentHashMap<>());
        hash.put(field, value);
    }

    @Override
    public void hmset(String key, Map<String, String> fieldValues) {
        fieldValues.forEach((field, value) -> hset(key, field, value));
    }

    @Override
    @SuppressWarnings("unchecked")
    public String hget(String key, String field) {
        if (isExpired(key)) {
            store.remove(key);
            expireMap.remove(key);
            return null;
        }
        Map<String, String> hash = (Map<String, String>) store.get(key);
        return hash != null ? hash.get(field) : null;
    }

    @Override
    public List<String> hmget(String key, String... fields) {
        return Arrays.stream(fields)
                .map(field -> hget(key, field))
                .collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String> hgetAll(String key) {
        if (isExpired(key)) {
            store.remove(key);
            expireMap.remove(key);
            return new HashMap<>();
        }
        Map<String, String> hash = (Map<String, String>) store.get(key);
        return hash != null ? new HashMap<>(hash) : new HashMap<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long hdel(String key, String... fields) {
        Map<String, String> hash = (Map<String, String>) store.get(key);
        if (hash == null) return 0L;
        long count = 0;
        for (String field : fields) {
            if (hash.remove(field) != null) count++;
        }
        return count;
    }

    @Override
    public boolean hexists(String key, String field) {
        return hget(key, field) != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long hlen(String key) {
        Map<String, String> hash = (Map<String, String>) store.get(key);
        return hash != null ? (long) hash.size() : 0L;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> hkeys(String key) {
        Map<String, String> hash = (Map<String, String>) store.get(key);
        return hash != null ? new HashSet<>(hash.keySet()) : new HashSet<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> hvals(String key) {
        Map<String, String> hash = (Map<String, String>) store.get(key);
        return hash != null ? new ArrayList<>(hash.values()) : new ArrayList<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean hsetnx(String key, String field, String value) {
        Map<String, String> hash = (Map<String, String>) store.computeIfAbsent(key, k -> new ConcurrentHashMap<>());
        if (!hash.containsKey(field)) {
            hash.put(field, value);
            return true;
        }
        return false;
    }

    @Override
    public Long hincrBy(String key, String field, long increment) {
        String value = hget(key, field);
        long newValue = (value != null ? Long.parseLong(value) : 0) + increment;
        hset(key, field, String.valueOf(newValue));
        return newValue;
    }

    // ==================== 列表(List)操作 ====================

    @Override
    @SuppressWarnings("unchecked")
    public Long lpush(String key, String... values) {
        LinkedList<String> list = (LinkedList<String>) store.computeIfAbsent(key, k -> new LinkedList<>());
        for (int i = values.length - 1; i >= 0; i--) {
            list.addFirst(values[i]);
        }
        return (long) list.size();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long rpush(String key, String... values) {
        LinkedList<String> list = (LinkedList<String>) store.computeIfAbsent(key, k -> new LinkedList<>());
        Collections.addAll(list, values);
        return (long) list.size();
    }

    @Override
    @SuppressWarnings("unchecked")
    public String lpop(String key) {
        LinkedList<String> list = (LinkedList<String>) store.get(key);
        return list != null && !list.isEmpty() ? list.removeFirst() : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String rpop(String key) {
        LinkedList<String> list = (LinkedList<String>) store.get(key);
        return list != null && !list.isEmpty() ? list.removeLast() : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long llen(String key) {
        LinkedList<String> list = (LinkedList<String>) store.get(key);
        return list != null ? (long) list.size() : 0L;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String lindex(String key, long index) {
        LinkedList<String> list = (LinkedList<String>) store.get(key);
        if (list == null || index < 0 || index >= list.size()) return null;
        return list.get((int) index);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void lset(String key, long index, String value) {
        LinkedList<String> list = (LinkedList<String>) store.get(key);
        if (list != null && index >= 0 && index < list.size()) {
            list.set((int) index, value);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> lrange(String key, long start, long end) {
        LinkedList<String> list = (LinkedList<String>) store.get(key);
        if (list == null) return new ArrayList<>();
        int size = list.size();
        int fromIndex = (int) (start < 0 ? Math.max(0, size + start) : start);
        int toIndex = (int) (end < 0 ? size + end + 1 : Math.min(size, end + 1));
        if (fromIndex >= toIndex || fromIndex >= size) return new ArrayList<>();
        return new ArrayList<>(list.subList(fromIndex, toIndex));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void ltrim(String key, long start, long end) {
        LinkedList<String> list = (LinkedList<String>) store.get(key);
        if (list != null) {
            int size = list.size();
            int fromIndex = (int) (start < 0 ? Math.max(0, size + start) : start);
            int toIndex = (int) (end < 0 ? size + end + 1 : Math.min(size, end + 1));
            if (fromIndex < toIndex && fromIndex < size) {
                List<String> subList = new ArrayList<>(list.subList(fromIndex, toIndex));
                list.clear();
                list.addAll(subList);
            } else {
                list.clear();
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long lrem(String key, long count, String value) {
        LinkedList<String> list = (LinkedList<String>) store.get(key);
        if (list == null) return 0L;
        long removed = 0;
        if (count > 0) {
            Iterator<String> it = list.iterator();
            while (it.hasNext() && removed < count) {
                if (value.equals(it.next())) {
                    it.remove();
                    removed++;
                }
            }
        } else if (count < 0) {
            Iterator<String> it = list.descendingIterator();
            while (it.hasNext() && removed < -count) {
                if (value.equals(it.next())) {
                    it.remove();
                    removed++;
                }
            }
        } else {
            removed = list.stream().filter(value::equals).count();
            list.removeIf(value::equals);
        }
        return removed;
    }

    @Override
    public String rpoplpush(String source, String destination) {
        String value = rpop(source);
        if (value != null) {
            lpush(destination, value);
        }
        return value;
    }

    @Override
    public String blpop(long timeout, String... keys) {
        long endTime = System.currentTimeMillis() + timeout * 1000;
        while (System.currentTimeMillis() < endTime) {
            for (String key : keys) {
                String value = lpop(key);
                if (value != null) return value;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return null;
    }

    @Override
    public String brpop(long timeout, String... keys) {
        long endTime = System.currentTimeMillis() + timeout * 1000;
        while (System.currentTimeMillis() < endTime) {
            for (String key : keys) {
                String value = rpop(key);
                if (value != null) return value;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return null;
    }

    // ==================== 集合(Set)操作 ====================

    @Override
    @SuppressWarnings("unchecked")
    public Long sadd(String key, String... members) {
        Set<String> set = (Set<String>) store.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet());
        long added = 0;
        for (String member : members) {
            if (set.add(member)) added++;
        }
        return added;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long srem(String key, String... members) {
        Set<String> set = (Set<String>) store.get(key);
        if (set == null) return 0L;
        long removed = 0;
        for (String member : members) {
            if (set.remove(member)) removed++;
        }
        return removed;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> smembers(String key) {
        Set<String> set = (Set<String>) store.get(key);
        return set != null ? new HashSet<>(set) : new HashSet<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean sismember(String key, String member) {
        Set<String> set = (Set<String>) store.get(key);
        return set != null && set.contains(member);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long scard(String key) {
        Set<String> set = (Set<String>) store.get(key);
        return set != null ? (long) set.size() : 0L;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String spop(String key) {
        Set<String> set = (Set<String>) store.get(key);
        if (set == null || set.isEmpty()) return null;
        Iterator<String> it = set.iterator();
        String value = it.next();
        it.remove();
        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String srandmember(String key) {
        Set<String> set = (Set<String>) store.get(key);
        if (set == null || set.isEmpty()) return null;
        return set.stream().skip(new Random().nextInt(set.size())).findFirst().orElse(null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> srandmember(String key, long count) {
        Set<String> set = (Set<String>) store.get(key);
        if (set == null) return new ArrayList<>();
        List<String> list = new ArrayList<>(set);
        Collections.shuffle(list);
        return list.subList(0, (int) Math.min(count, list.size()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean smove(String source, String destination, String member) {
        Set<String> srcSet = (Set<String>) store.get(source);
        if (srcSet == null || !srcSet.remove(member)) return false;
        Set<String> destSet = (Set<String>) store.computeIfAbsent(destination, k -> ConcurrentHashMap.newKeySet());
        destSet.add(member);
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> sinter(String... keys) {
        if (keys.length == 0) return new HashSet<>();
        Set<String> result = new HashSet<>();
        Set<String> first = (Set<String>) store.get(keys[0]);
        if (first != null) result.addAll(first);
        for (int i = 1; i < keys.length && !result.isEmpty(); i++) {
            Set<String> set = (Set<String>) store.get(keys[i]);
            if (set != null) {
                result.retainAll(set);
            } else {
                result.clear();
            }
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> sunion(String... keys) {
        Set<String> result = new HashSet<>();
        for (String key : keys) {
            Set<String> set = (Set<String>) store.get(key);
            if (set != null) result.addAll(set);
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> sdiff(String... keys) {
        if (keys.length == 0) return new HashSet<>();
        Set<String> result = new HashSet<>();
        Set<String> first = (Set<String>) store.get(keys[0]);
        if (first != null) result.addAll(first);
        for (int i = 1; i < keys.length; i++) {
            Set<String> set = (Set<String>) store.get(keys[i]);
            if (set != null) result.removeAll(set);
        }
        return result;
    }

    // ==================== 有序集合(Sorted Set)操作 ====================

    @Override
    @SuppressWarnings("unchecked")
    public Long zadd(String key, Map<String, Double> memberScores) {
        TreeMap<Double, Set<String>> sortedSet = 
            (TreeMap<Double, Set<String>>) store.computeIfAbsent(key, k -> new TreeMap<>());
        long added = 0;
        for (Map.Entry<String, Double> entry : memberScores.entrySet()) {
            Double score = entry.getValue();
            String member = entry.getKey();
            sortedSet.computeIfAbsent(score, s -> ConcurrentHashMap.newKeySet()).add(member);
            added++;
        }
        return added;
    }

    @Override
    public Long zadd(String key, String member, double score) {
        Map<String, Double> map = new HashMap<>();
        map.put(member, score);
        return zadd(key, map);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long zrem(String key, String... members) {
        TreeMap<Double, Set<String>> sortedSet = (TreeMap<Double, Set<String>>) store.get(key);
        if (sortedSet == null) return 0L;
        long removed = 0;
        Set<String> memberSet = new HashSet<>(Arrays.asList(members));
        Iterator<Map.Entry<Double, Set<String>>> it = sortedSet.entrySet().iterator();
        while (it.hasNext()) {
            Set<String> set = it.next().getValue();
            Iterator<String> memberIt = set.iterator();
            while (memberIt.hasNext()) {
                if (memberSet.contains(memberIt.next())) {
                    memberIt.remove();
                    removed++;
                }
            }
            if (set.isEmpty()) it.remove();
        }
        return removed;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Double zscore(String key, String member) {
        TreeMap<Double, Set<String>> sortedSet = (TreeMap<Double, Set<String>>) store.get(key);
        if (sortedSet == null) return null;
        for (Map.Entry<Double, Set<String>> entry : sortedSet.entrySet()) {
            if (entry.getValue().contains(member)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long zrank(String key, String member) {
        TreeMap<Double, Set<String>> sortedSet = (TreeMap<Double, Set<String>>) store.get(key);
        if (sortedSet == null) return null;
        long rank = 0;
        for (Map.Entry<Double, Set<String>> entry : sortedSet.entrySet()) {
            if (entry.getValue().contains(member)) {
                return rank;
            }
            rank += entry.getValue().size();
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long zrevrank(String key, String member) {
        TreeMap<Double, Set<String>> sortedSet = (TreeMap<Double, Set<String>>) store.get(key);
        if (sortedSet == null) return null;
        long rank = 0;
        for (Map.Entry<Double, Set<String>> entry : sortedSet.descendingMap().entrySet()) {
            if (entry.getValue().contains(member)) {
                return rank;
            }
            rank += entry.getValue().size();
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long zcard(String key) {
        TreeMap<Double, Set<String>> sortedSet = (TreeMap<Double, Set<String>>) store.get(key);
        if (sortedSet == null) return 0L;
        return sortedSet.values().stream().mapToLong(Set::size).sum();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long zcount(String key, double min, double max) {
        TreeMap<Double, Set<String>> sortedSet = (TreeMap<Double, Set<String>>) store.get(key);
        if (sortedSet == null) return 0L;
        return sortedSet.subMap(min, true, max, true).values().stream()
                .mapToLong(Set::size).sum();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> zrange(String key, long start, long end) {
        TreeMap<Double, Set<String>> sortedSet = (TreeMap<Double, Set<String>>) store.get(key);
        if (sortedSet == null) return new LinkedHashSet<>();
        List<String> all = sortedSet.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toList());
        int size = all.size();
        int fromIndex = (int) (start < 0 ? Math.max(0, size + start) : start);
        int toIndex = (int) (end < 0 ? size + end + 1 : Math.min(size, end + 1));
        if (fromIndex >= toIndex || fromIndex >= size) return new LinkedHashSet<>();
        return new LinkedHashSet<>(all.subList(fromIndex, toIndex));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Double> zrangeWithScores(String key, long start, long end) {
        TreeMap<Double, Set<String>> sortedSet = (TreeMap<Double, Set<String>>) store.get(key);
        if (sortedSet == null) return new LinkedHashMap<>();
        List<Map.Entry<String, Double>> all = new ArrayList<>();
        for (Map.Entry<Double, Set<String>> entry : sortedSet.entrySet()) {
            for (String member : entry.getValue()) {
                all.add(new AbstractMap.SimpleEntry<>(member, entry.getKey()));
            }
        }
        int size = all.size();
        int fromIndex = (int) (start < 0 ? Math.max(0, size + start) : start);
        int toIndex = (int) (end < 0 ? size + end + 1 : Math.min(size, end + 1));
        Map<String, Double> result = new LinkedHashMap<>();
        if (fromIndex < toIndex && fromIndex < size) {
            all.subList(fromIndex, toIndex).forEach(e -> result.put(e.getKey(), e.getValue()));
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> zrevrange(String key, long start, long end) {
        TreeMap<Double, Set<String>> sortedSet = (TreeMap<Double, Set<String>>) store.get(key);
        if (sortedSet == null) return new LinkedHashSet<>();
        List<String> all = sortedSet.descendingMap().values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toList());
        int size = all.size();
        int fromIndex = (int) (start < 0 ? Math.max(0, size + start) : start);
        int toIndex = (int) (end < 0 ? size + end + 1 : Math.min(size, end + 1));
        if (fromIndex >= toIndex || fromIndex >= size) return new LinkedHashSet<>();
        return new LinkedHashSet<>(all.subList(fromIndex, toIndex));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Double> zrevrangeWithScores(String key, long start, long end) {
        TreeMap<Double, Set<String>> sortedSet = (TreeMap<Double, Set<String>>) store.get(key);
        if (sortedSet == null) return new LinkedHashMap<>();
        List<Map.Entry<String, Double>> all = new ArrayList<>();
        for (Map.Entry<Double, Set<String>> entry : sortedSet.descendingMap().entrySet()) {
            for (String member : entry.getValue()) {
                all.add(new AbstractMap.SimpleEntry<>(member, entry.getKey()));
            }
        }
        int size = all.size();
        int fromIndex = (int) (start < 0 ? Math.max(0, size + start) : start);
        int toIndex = (int) (end < 0 ? size + end + 1 : Math.min(size, end + 1));
        Map<String, Double> result = new LinkedHashMap<>();
        if (fromIndex < toIndex && fromIndex < size) {
            all.subList(fromIndex, toIndex).forEach(e -> result.put(e.getKey(), e.getValue()));
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> zrangeByScore(String key, double min, double max) {
        TreeMap<Double, Set<String>> sortedSet = (TreeMap<Double, Set<String>>) store.get(key);
        if (sortedSet == null) return new LinkedHashSet<>();
        return sortedSet.subMap(min, true, max, true).values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Double> zrangeByScoreWithScores(String key, double min, double max) {
        TreeMap<Double, Set<String>> sortedSet = (TreeMap<Double, Set<String>>) store.get(key);
        if (sortedSet == null) return new LinkedHashMap<>();
        Map<String, Double> result = new LinkedHashMap<>();
        sortedSet.subMap(min, true, max, true).forEach((score, members) -> {
            for (String member : members) {
                result.put(member, score);
            }
        });
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Double zincrby(String key, double increment, String member) {
        TreeMap<Double, Set<String>> sortedSet = 
            (TreeMap<Double, Set<String>>) store.computeIfAbsent(key, k -> new TreeMap<>());
        Double oldScore = zscore(key, member);
        double newScore = (oldScore != null ? oldScore : 0) + increment;
        if (oldScore != null) {
            sortedSet.get(oldScore).remove(member);
            if (sortedSet.get(oldScore).isEmpty()) {
                sortedSet.remove(oldScore);
            }
        }
        sortedSet.computeIfAbsent(newScore, s -> ConcurrentHashMap.newKeySet()).add(member);
        return newScore;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long zremrangeByRank(String key, long start, long end) {
        Set<String> members = zrange(key, start, end);
        if (members.isEmpty()) return 0L;
        return zrem(key, members.toArray(new String[0]));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long zremrangeByScore(String key, double min, double max) {
        Set<String> members = zrangeByScore(key, min, max);
        if (members.isEmpty()) return 0L;
        return zrem(key, members.toArray(new String[0]));
    }

    // ==================== 键(Key)操作 ====================

    @Override
    public Long del(String... keys) {
        long count = 0;
        for (String key : keys) {
            if (store.remove(key) != null) {
                expireMap.remove(key);
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean exists(String key) {
        if (isExpired(key)) {
            store.remove(key);
            expireMap.remove(key);
            return false;
        }
        return store.containsKey(key);
    }

    @Override
    public boolean expire(String key, long expire, TimeUnit unit) {
        if (!exists(key)) return false;
        expireMap.put(key, System.currentTimeMillis() + unit.toMillis(expire));
        return true;
    }

    @Override
    public boolean expireAt(String key, long timestamp) {
        if (!exists(key)) return false;
        expireMap.put(key, timestamp);
        return true;
    }

    @Override
    public boolean persist(String key) {
        return expireMap.remove(key) != null;
    }

    @Override
    public Long ttl(String key) {
        Long pttl = pttl(key);
        return pttl != null && pttl > 0 ? pttl / 1000 : pttl;
    }

    @Override
    public Long pttl(String key) {
        Long expireTime = expireMap.get(key);
        if (expireTime == null) return -1L;
        long remaining = expireTime - System.currentTimeMillis();
        return remaining > 0 ? remaining : -2L;
    }

    @Override
    public String type(String key) {
        if (!exists(key)) return "none";
        Object value = store.get(key);
        if (value instanceof String) return "string";
        if (value instanceof Map) return "hash";
        if (value instanceof LinkedList) return "list";
        if (value instanceof Set) return "set";
        if (value instanceof TreeMap) return "zset";
        return "string";
    }

    @Override
    public void rename(String oldKey, String newKey) {
        Object value = store.remove(oldKey);
        Long expire = expireMap.remove(oldKey);
        if (value != null) {
            store.put(newKey, value);
            if (expire != null) expireMap.put(newKey, expire);
        }
    }

    @Override
    public boolean renamenx(String oldKey, String newKey) {
        if (exists(newKey)) return false;
        rename(oldKey, newKey);
        return true;
    }

    @Override
    public Set<String> keys(String pattern) {
        String regex = pattern.replace("*", ".*").replace("?", ".");
        return store.keySet().stream()
                .filter(key -> key.matches(regex))
                .filter(key -> !isExpired(key))
                .collect(Collectors.toSet());
    }

    @Override
    public String randomKey() {
        List<String> validKeys = store.keySet().stream()
                .filter(key -> !isExpired(key))
                .collect(Collectors.toList());
        if (validKeys.isEmpty()) return null;
        return validKeys.get(new Random().nextInt(validKeys.size()));
    }

    @Override
    public boolean move(String key, int dbIndex) {
        // Map实现不支持多数据库
        throw new UnsupportedOperationException("Map实现不支持多数据库");
    }

    // ==================== 位图(Bitmap)操作 ====================

    @Override
    @SuppressWarnings("unchecked")
    public boolean setbit(String key, long offset, boolean value) {
        Map<Long, Boolean> bitmap = (Map<Long, Boolean>) store.computeIfAbsent(key, k -> new ConcurrentHashMap<>());
        return bitmap.put(offset, value) != null && bitmap.get(offset);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean getbit(String key, long offset) {
        Map<Long, Boolean> bitmap = (Map<Long, Boolean>) store.get(key);
        return bitmap != null && bitmap.getOrDefault(offset, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long bitcount(String key) {
        Map<Long, Boolean> bitmap = (Map<Long, Boolean>) store.get(key);
        if (bitmap == null) return 0L;
        return bitmap.values().stream().filter(b -> b).count();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long bitcount(String key, long start, long end) {
        Map<Long, Boolean> bitmap = (Map<Long, Boolean>) store.get(key);
        if (bitmap == null) return 0L;
        return bitmap.entrySet().stream()
                .filter(e -> e.getKey() >= start && e.getKey() <= end)
                .filter(e -> e.getValue())
                .count();
    }

    // ==================== HyperLogLog操作 ====================

    @Override
    @SuppressWarnings("unchecked")
    public Long pfadd(String key, String... elements) {
        Set<String> hll = (Set<String>) store.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet());
        long added = 0;
        for (String element : elements) {
            if (hll.add(element)) added++;
        }
        return added;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long pfcount(String... keys) {
        Set<String> union = new HashSet<>();
        for (String key : keys) {
            Set<String> hll = (Set<String>) store.get(key);
            if (hll != null) union.addAll(hll);
        }
        return (long) union.size();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void pfmerge(String destKey, String... sourceKeys) {
        Set<String> dest = (Set<String>) store.computeIfAbsent(destKey, k -> ConcurrentHashMap.newKeySet());
        for (String sourceKey : sourceKeys) {
            Set<String> source = (Set<String>) store.get(sourceKey);
            if (source != null) dest.addAll(source);
        }
    }

    // ==================== 事务和脚本操作 ====================

    @Override
    public Object exec(Runnable transaction) {
        transaction.run();
        return null;
    }

    @Override
    public Object eval(String script, List<String> keys, List<String> args) {
        throw new UnsupportedOperationException("Map实现不支持Lua脚本");
    }

    @Override
    public Object evalsha(String sha1, List<String> keys, List<String> args) {
        throw new UnsupportedOperationException("Map实现不支持Lua脚本");
    }

    // ==================== 发布订阅操作 ====================

    @Override
    public Long publish(String channel, String message) {
        throw new UnsupportedOperationException("Map实现不支持发布订阅");
    }

    @Override
    public void subscribe(String... channels) {
        throw new UnsupportedOperationException("Map实现不支持发布订阅");
    }

    @Override
    public void psubscribe(String... patterns) {
        throw new UnsupportedOperationException("Map实现不支持发布订阅");
    }

    @Override
    public void unsubscribe(String... channels) {
        throw new UnsupportedOperationException("Map实现不支持发布订阅");
    }

    @Override
    public void punsubscribe(String... patterns) {
        throw new UnsupportedOperationException("Map实现不支持发布订阅");
    }

    // ==================== 数据库操作 ====================

    @Override
    public Long dbsize() {
        cleanExpiredKeys();
        return (long) store.size();
    }

    @Override
    public void flushdb() {
        store.clear();
        expireMap.clear();
    }

    @Override
    public void flushall() {
        flushdb();
    }

    @Override
    public void select(int dbIndex) {
        throw new UnsupportedOperationException("Map实现不支持多数据库");
    }

    // ==================== 其他实用方法 ====================

    @Override
    public Long delByPattern(String pattern) {
        Set<String> keys = keys(pattern);
        if (keys.isEmpty()) return 0L;
        return del(keys.toArray(new String[0]));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = store.get(key);
        return value != null ? (T) value : null;
    }

    @Override
    public <T> void set(String key, T value) {
        store.put(key, value);
    }

    @Override
    public <T> void setex(String key, T value, long expire, TimeUnit unit) {
        store.put(key, value);
        expireMap.put(key, System.currentTimeMillis() + unit.toMillis(expire));
    }

    // ==================== 辅助方法 ====================

    private boolean isExpired(String key) {
        Long expireTime = expireMap.get(key);
        return expireTime != null && System.currentTimeMillis() > expireTime;
    }

    private void cleanExpiredKeys() {
        long now = System.currentTimeMillis();
        expireMap.entrySet().removeIf(entry -> {
            if (now > entry.getValue()) {
                store.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }
}

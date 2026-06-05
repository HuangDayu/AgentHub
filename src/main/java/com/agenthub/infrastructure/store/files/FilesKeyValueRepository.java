package com.agenthub.infrastructure.store.files;

import com.agenthub.application.port.out.repositories.KeyValueRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 基于文件的键值存储服务实现
 * 
 * @author huangdayu
 */
@Component
@ConditionalOnProperty(name = "agenthub.kv.type", havingValue = "files")
public class FilesKeyValueRepository implements KeyValueRepository {

    private final Path baseDir;
    private final ObjectMapper objectMapper;
    private final Map<String, Long> expireMap = new ConcurrentHashMap<>();

    public FilesKeyValueRepository() {
        this.baseDir = Paths.get(System.getProperty("java.io.tmpdir"), "kv-store");
        this.objectMapper = new ObjectMapper();
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create base directory", e);
        }
    }

    public FilesKeyValueRepository(String basePath) {
        this.baseDir = Paths.get(basePath);
        this.objectMapper = new ObjectMapper();
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create base directory", e);
        }
    }

    // ==================== 字符串(String)操作 ====================

    @Override
    public void set(String key, String value) {
        try {
            Path filePath = getFilePath(key, "string");
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, value.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to set value", e);
        }
    }

    @Override
    public void setex(String key, String value, long expire, TimeUnit unit) {
        set(key, value);
        expireMap.put(key, System.currentTimeMillis() + unit.toMillis(expire));
    }

    @Override
    public boolean setnx(String key, String value) {
        if (isExpired(key)) {
            delete(key);
        }
        Path filePath = getFilePath(key, "string");
        if (Files.exists(filePath)) {
            return false;
        }
        set(key, value);
        return true;
    }

    @Override
    public String get(String key) {
        if (isExpired(key)) {
            delete(key);
            return null;
        }
        return readStringValue(key);
    }

    private String readStringValue(String key) {
        try {
            Path filePath = getFilePath(key, "string");
            return Files.exists(filePath) ? new String(Files.readAllBytes(filePath)) : null;
        } catch (IOException e) {
            return null;
        }
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
        try {
            Path filePath = getFilePath(key, "string");
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, value.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException("Failed to append value", e);
        }
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
        try {
            Path filePath = getFilePath(key, "hash");
            Files.createDirectories(filePath.getParent());
            Map<String, String> hash = Files.exists(filePath) 
                ? objectMapper.readValue(filePath.toFile(), Map.class)
                : new HashMap<>();
            hash.put(field, value);
            objectMapper.writeValue(filePath.toFile(), hash);
        } catch (IOException e) {
            throw new RuntimeException("Failed to hset", e);
        }
    }

    @Override
    public void hmset(String key, Map<String, String> fieldValues) {
        fieldValues.forEach((field, value) -> hset(key, field, value));
    }

    @Override
    @SuppressWarnings("unchecked")
    public String hget(String key, String field) {
        if (isExpired(key)) {
            delete(key);
            return null;
        }
        try {
            return readHashField(key, field);
        } catch (IOException e) {
            return null;
        }
    }

    private String readHashField(String key, String field) throws IOException {
        Path filePath = getFilePath(key, "hash");
        if (!Files.exists(filePath)) return null;
        Map<String, String> hash = objectMapper.readValue(filePath.toFile(), Map.class);
        return hash.get(field);
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
            delete(key);
            return new HashMap<>();
        }
        return readHashAll(key);
    }

    private Map<String, String> readHashAll(String key) {
        try {
            Path filePath = getFilePath(key, "hash");
            return Files.exists(filePath)
                    ? objectMapper.readValue(filePath.toFile(), Map.class)
                    : new HashMap<>();
        } catch (IOException e) {
            return new HashMap<>();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long hdel(String key, String... fields) {
        try {
            Path filePath = getFilePath(key, "hash");
            if (!Files.exists(filePath)) return 0L;
            Map<String, String> hash = objectMapper.readValue(filePath.toFile(), Map.class);
            long count = removeHashFields(hash, fields);
            objectMapper.writeValue(filePath.toFile(), hash);
            return count;
        } catch (IOException e) {
            return 0L;
        }
    }

    @Override
    public boolean hexists(String key, String field) {
        return hget(key, field) != null;
    }

    private long removeHashFields(Map<String, String> hash, String[] fields) {
        long count = 0;
        for (String field : fields) {
            if (hash.remove(field) != null) count++;
        }
        return count;
    }

    @Override
    public Long hlen(String key) {
        Map<String, String> hash = hgetAll(key);
        return (long) hash.size();
    }

    @Override
    public Set<String> hkeys(String key) {
        Map<String, String> hash = hgetAll(key);
        return hash.keySet();
    }

    @Override
    public List<String> hvals(String key) {
        Map<String, String> hash = hgetAll(key);
        return new ArrayList<>(hash.values());
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean hsetnx(String key, String field, String value) {
        try {
            Path filePath = getFilePath(key, "hash");
            Files.createDirectories(filePath.getParent());
            return putIfAbsent(filePath, field, value);
        } catch (IOException e) {
            return false;
        }
    }

    private boolean putIfAbsent(Path filePath, String field, String value) throws IOException {
        Map<String, String> hash = loadOrCreateHash(filePath);
        if (hash.containsKey(field)) return false;
        hash.put(field, value);
        objectMapper.writeValue(filePath.toFile(), hash);
        return true;
    }

    private Map<String, String> loadOrCreateHash(Path filePath) throws IOException {
        return Files.exists(filePath)
                ? objectMapper.readValue(filePath.toFile(), Map.class)
                : new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    private Set<String> loadOrCreateSet(Path filePath) throws IOException {
        return Files.exists(filePath)
                ? new HashSet<>(objectMapper.readValue(filePath.toFile(), List.class))
                : new HashSet<>();
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
        try {
            Path filePath = getFilePath(key, "list");
            Files.createDirectories(filePath.getParent());
            List<String> list = loadOrCreateList(filePath);
            for (int i = values.length - 1; i >= 0; i--) {
                list.add(0, values[i]);
            }
            objectMapper.writeValue(filePath.toFile(), list);
            return (long) list.size();
        } catch (IOException e) {
            throw new RuntimeException("Failed to lpush", e);
        }
    }

    private List<String> loadOrCreateList(Path filePath) throws IOException {
        return Files.exists(filePath)
                ? objectMapper.readValue(filePath.toFile(), List.class)
                : new ArrayList<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long rpush(String key, String... values) {
        try {
            Path filePath = getFilePath(key, "list");
            Files.createDirectories(filePath.getParent());
            List<String> list = Files.exists(filePath) 
                ? objectMapper.readValue(filePath.toFile(), List.class)
                : new ArrayList<>();
            Collections.addAll(list, values);
            objectMapper.writeValue(filePath.toFile(), list);
            return (long) list.size();
        } catch (IOException e) {
            throw new RuntimeException("Failed to rpush", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public String lpop(String key) {
        try {
            Path filePath = getFilePath(key, "list");
            if (!Files.exists(filePath)) return null;
            List<String> list = objectMapper.readValue(filePath.toFile(), List.class);
            if (list.isEmpty()) return null;
            String value = list.remove(0);
            objectMapper.writeValue(filePath.toFile(), list);
            return value;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public String rpop(String key) {
        try {
            Path filePath = getFilePath(key, "list");
            if (!Files.exists(filePath)) return null;
            List<String> list = objectMapper.readValue(filePath.toFile(), List.class);
            if (list.isEmpty()) return null;
            String value = list.remove(list.size() - 1);
            objectMapper.writeValue(filePath.toFile(), list);
            return value;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long llen(String key) {
        try {
            Path filePath = getFilePath(key, "list");
            if (!Files.exists(filePath)) return 0L;
            List<String> list = objectMapper.readValue(filePath.toFile(), List.class);
            return (long) list.size();
        } catch (IOException e) {
            return 0L;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public String lindex(String key, long index) {
        try {
            Path filePath = getFilePath(key, "list");
            if (!Files.exists(filePath)) return null;
            List<String> list = objectMapper.readValue(filePath.toFile(), List.class);
            if (index < 0 || index >= list.size()) return null;
            return list.get((int) index);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void lset(String key, long index, String value) {
        try {
            Path filePath = getFilePath(key, "list");
            if (!Files.exists(filePath)) return;
            List<String> list = objectMapper.readValue(filePath.toFile(), List.class);
            if (index >= 0 && index < list.size()) {
                list.set((int) index, value);
                objectMapper.writeValue(filePath.toFile(), list);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to lset", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> lrange(String key, long start, long end) {
        try {
            Path filePath = getFilePath(key, "list");
            if (!Files.exists(filePath)) return new ArrayList<>();
            List<String> list = objectMapper.readValue(filePath.toFile(), List.class);
            int size = list.size();
            int fromIndex = (int) (start < 0 ? Math.max(0, size + start) : start);
            int toIndex = (int) (end < 0 ? size + end + 1 : Math.min(size, end + 1));
            if (fromIndex >= toIndex || fromIndex >= size) return new ArrayList<>();
            return new ArrayList<>(list.subList(fromIndex, toIndex));
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void ltrim(String key, long start, long end) {
        try {
            Path filePath = getFilePath(key, "list");
            if (!Files.exists(filePath)) return;
            List<String> list = objectMapper.readValue(filePath.toFile(), List.class);
            int[] bounds = computeListBounds(list.size(), start, end);
            List<String> trimmed = (bounds[0] < bounds[1] && bounds[0] < list.size())
                    ? new ArrayList<>(list.subList(bounds[0], bounds[1]))
                    : new ArrayList<>();
            objectMapper.writeValue(filePath.toFile(), trimmed);
        } catch (IOException e) {
            throw new RuntimeException("Failed to ltrim", e);
        }
    }

    private int[] computeListBounds(int size, long start, long end) {
        int fromIndex = (int) (start < 0 ? Math.max(0, size + start) : start);
        int toIndex = (int) (end < 0 ? size + end + 1 : Math.min(size, end + 1));
        return new int[]{fromIndex, toIndex};
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long lrem(String key, long count, String value) {
        try {
            Path filePath = getFilePath(key, "list");
            if (!Files.exists(filePath)) return 0L;
            List<String> list = objectMapper.readValue(filePath.toFile(), List.class);
            long removed = removeMatchingEntries(list, count, value);
            objectMapper.writeValue(filePath.toFile(), list);
            return removed;
        } catch (IOException e) {
            return 0L;
        }
    }

    /**
     * 从头开始删除最多 {@code count} 个匹配项。
     */
    private long removeFromHead(List<String> list, long count, String value) {
        Iterator<String> it = list.iterator();
        long removed = 0;
        while (it.hasNext() && removed < count) {
            if (value.equals(it.next())) {
                it.remove();
                removed++;
            }
        }
        return removed;
    }

    /**
     * 从尾开始删除最多 {@code |count|} 个匹配项。
     */
    private long removeFromTail(List<String> list, long count, String value) {
        long removed = 0;
        for (int i = list.size() - 1; i >= 0 && removed < -count; i--) {
            if (value.equals(list.get(i))) {
                list.remove(i);
                removed++;
            }
        }
        return removed;
    }

    /**
     * 删除所有匹配项，返回删除数量。
     */
    private long removeAllMatches(List<String> list, String value) {
        long removed = list.stream().filter(value::equals).count();
        list.removeIf(value::equals);
        return removed;
    }

    private long removeMatchingEntries(List<String> list, long count, String value) {
        if (count > 0) {
            return removeFromHead(list, count, value);
        }
        if (count < 0) {
            return removeFromTail(list, count, value);
        }
        return removeAllMatches(list, value);
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
        return pollWithTimeout(timeout, keys, this::lpop);
    }

    @Override
    public String brpop(long timeout, String... keys) {
        return pollWithTimeout(timeout, keys, this::rpop);
    }

    private String pollWithTimeout(long timeout, String[] keys, java.util.function.Function<String, String> popFn) {
        long endTime = System.currentTimeMillis() + timeout * 1000;
        while (System.currentTimeMillis() < endTime) {
            String value = tryPollKeys(keys, popFn);
            if (value != null) return value;
            if (!sleepQuietly(100)) return null;
        }
        return null;
    }

    private String tryPollKeys(String[] keys, java.util.function.Function<String, String> popFn) {
        for (String key : keys) {
            String value = popFn.apply(key);
            if (value != null) return value;
        }
        return null;
    }

    private boolean sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    // ==================== 集合(Set)操作 ====================

    @Override
    @SuppressWarnings("unchecked")
    public Long sadd(String key, String... members) {
        try {
            Path filePath = getFilePath(key, "set");
            Files.createDirectories(filePath.getParent());
            Set<String> set = loadOrCreateSet(filePath);
            long added = countNewMembers(set, members);
            objectMapper.writeValue(filePath.toFile(), new ArrayList<>(set));
            return added;
        } catch (IOException e) {
            throw new RuntimeException("Failed to sadd", e);
        }
    }

    private long countNewMembers(Set<String> set, String[] members) {
        long added = 0;
        for (String member : members) {
            if (set.add(member)) added++;
        }
        return added;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long srem(String key, String... members) {
        try {
            Path filePath = getFilePath(key, "set");
            if (!Files.exists(filePath)) return 0L;
            Set<String> set = new HashSet<>(objectMapper.readValue(filePath.toFile(), List.class));
            long removed = countRemovedMembers(set, members);
            objectMapper.writeValue(filePath.toFile(), new ArrayList<>(set));
            return removed;
        } catch (IOException e) {
            return 0L;
        }
    }

    private long countRemovedMembers(Set<String> set, String[] members) {
        long removed = 0;
        for (String member : members) {
            if (set.remove(member)) removed++;
        }
        return removed;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> smembers(String key) {
        try {
            Path filePath = getFilePath(key, "set");
            if (!Files.exists(filePath)) return new HashSet<>();
            return new HashSet<>(objectMapper.readValue(filePath.toFile(), List.class));
        } catch (IOException e) {
            return new HashSet<>();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean sismember(String key, String member) {
        try {
            Path filePath = getFilePath(key, "set");
            if (!Files.exists(filePath)) return false;
            Set<String> set = new HashSet<>(objectMapper.readValue(filePath.toFile(), List.class));
            return set.contains(member);
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long scard(String key) {
        try {
            Path filePath = getFilePath(key, "set");
            if (!Files.exists(filePath)) return 0L;
            Set<String> set = new HashSet<>(objectMapper.readValue(filePath.toFile(), List.class));
            return (long) set.size();
        } catch (IOException e) {
            return 0L;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public String spop(String key) {
        try {
            Path filePath = getFilePath(key, "set");
            if (!Files.exists(filePath)) return null;
            Set<String> set = new HashSet<>(objectMapper.readValue(filePath.toFile(), List.class));
            return popAndPersist(set, filePath);
        } catch (IOException e) {
            return null;
        }
    }

    private String popAndPersist(Set<String> set, Path filePath) throws IOException {
        if (set.isEmpty()) return null;
        Iterator<String> it = set.iterator();
        String value = it.next();
        it.remove();
        objectMapper.writeValue(filePath.toFile(), new ArrayList<>(set));
        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String srandmember(String key) {
        try {
            Path filePath = getFilePath(key, "set");
            if (!Files.exists(filePath)) return null;
            Set<String> set = new HashSet<>(objectMapper.readValue(filePath.toFile(), List.class));
            if (set.isEmpty()) return null;
            return set.stream().skip(new Random().nextInt(set.size())).findFirst().orElse(null);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> srandmember(String key, long count) {
        try {
            Path filePath = getFilePath(key, "set");
            if (!Files.exists(filePath)) return new ArrayList<>();
            Set<String> set = new HashSet<>(objectMapper.readValue(filePath.toFile(), List.class));
            List<String> list = new ArrayList<>(set);
            Collections.shuffle(list);
            return list.subList(0, (int) Math.min(count, list.size()));
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean smove(String source, String destination, String member) {
        try {
            if (!removeFromSourceSet(source, member)) return false;
            addToDestinationSet(destination, member);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean removeFromSourceSet(String source, String member) throws IOException {
        Path sourcePath = getFilePath(source, "set");
        if (!Files.exists(sourcePath)) return false;
        Set<String> srcSet = new HashSet<>(objectMapper.readValue(sourcePath.toFile(), List.class));
        if (!srcSet.remove(member)) return false;
        objectMapper.writeValue(sourcePath.toFile(), new ArrayList<>(srcSet));
        return true;
    }

    @SuppressWarnings("unchecked")
    private void addToDestinationSet(String destination, String member) throws IOException {
        Path destPath = getFilePath(destination, "set");
        Files.createDirectories(destPath.getParent());
        Set<String> destSet = Files.exists(destPath)
                ? new HashSet<>(objectMapper.readValue(destPath.toFile(), List.class))
                : new HashSet<>();
        destSet.add(member);
        objectMapper.writeValue(destPath.toFile(), new ArrayList<>(destSet));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> sinter(String... keys) {
        if (keys.length == 0) return new HashSet<>();
        Set<String> result = null;
        for (String key : keys) {
            Set<String> set = readSetOrNull(key);
            if (set == null) return new HashSet<>();
            result = combineIntersection(result, set);
        }
        return result != null ? result : new HashSet<>();
    }

    private Set<String> readSetOrNull(String key) {
        Path filePath = getFilePath(key, "set");
        if (!Files.exists(filePath)) return null;
        try {
            return new HashSet<>(objectMapper.readValue(filePath.toFile(), List.class));
        } catch (IOException e) {
            return null;
        }
    }

    private Set<String> combineIntersection(Set<String> current, Set<String> next) {
        if (current == null) return new HashSet<>(next);
        current.retainAll(next);
        return current;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> sunion(String... keys) {
        Set<String> result = new HashSet<>();
        for (String key : keys) {
            Path filePath = getFilePath(key, "set");
            if (Files.exists(filePath)) {
                try {
                    Set<String> set = new HashSet<>(objectMapper.readValue(filePath.toFile(), List.class));
                    result.addAll(set);
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> sdiff(String... keys) {
        if (keys.length == 0) return new HashSet<>();
        Set<String> result = null;
        for (int i = 0; i < keys.length; i++) {
            Set<String> set = readSetIfExists(keys[i]);
            if (set == null) continue;
            result = combineDifference(result, set);
        }
        return result != null ? result : new HashSet<>();
    }

    private Set<String> readSetIfExists(String key) {
        Path filePath = getFilePath(key, "set");
        if (!Files.exists(filePath)) return null;
        try {
            return new HashSet<>(objectMapper.readValue(filePath.toFile(), List.class));
        } catch (IOException e) {
            return null;
        }
    }

    private Set<String> combineDifference(Set<String> current, Set<String> next) {
        if (current == null) return new HashSet<>(next);
        current.removeAll(next);
        return current;
    }

    // ==================== 有序集合(Sorted Set)操作 ====================

    @Override
    @SuppressWarnings("unchecked")
    public Long zadd(String key, Map<String, Double> memberScores) {
        try {
            Path filePath = getFilePath(key, "zset");
            Files.createDirectories(filePath.getParent());
            Map<String, Double> zset = Files.exists(filePath) 
                ? objectMapper.readValue(filePath.toFile(), Map.class)
                : new LinkedHashMap<>();
            zset.putAll(memberScores);
            objectMapper.writeValue(filePath.toFile(), zset);
            return (long) memberScores.size();
        } catch (IOException e) {
            throw new RuntimeException("Failed to zadd", e);
        }
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
        try {
            Path filePath = getFilePath(key, "zset");
            if (!Files.exists(filePath)) return 0L;
            Map<String, Double> zset = objectMapper.readValue(filePath.toFile(), Map.class);
            long removed = removeZsetMembers(zset, members);
            objectMapper.writeValue(filePath.toFile(), zset);
            return removed;
        } catch (IOException e) {
            return 0L;
        }
    }

    private long removeZsetMembers(Map<String, Double> zset, String[] members) {
        long removed = 0;
        for (String member : members) {
            if (zset.remove(member) != null) removed++;
        }
        return removed;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Double zscore(String key, String member) {
        try {
            Path filePath = getFilePath(key, "zset");
            if (!Files.exists(filePath)) return null;
            Map<String, Double> zset = objectMapper.readValue(filePath.toFile(), Map.class);
            return zset.get(member);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long zrank(String key, String member) {
        try {
            Map<String, Double> zset = readZsetIfExists(key);
            if (zset == null || !zset.containsKey(member)) return null;
            return countScoresLowerThan(zset, zset.get(member));
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long zrevrank(String key, String member) {
        try {
            Map<String, Double> zset = readZsetIfExists(key);
            if (zset == null || !zset.containsKey(member)) return null;
            return countScoresGreaterThan(zset, zset.get(member));
        } catch (IOException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Double> readZsetIfExists(String key) throws IOException {
        Path filePath = getFilePath(key, "zset");
        if (!Files.exists(filePath)) return null;
        return objectMapper.readValue(filePath.toFile(), Map.class);
    }

    private long countScoresLowerThan(Map<String, Double> zset, double target) {
        return zset.values().stream().filter(s -> s < target).count();
    }

    private long countScoresGreaterThan(Map<String, Double> zset, double target) {
        return zset.values().stream().filter(s -> s > target).count();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long zcard(String key) {
        try {
            Path filePath = getFilePath(key, "zset");
            if (!Files.exists(filePath)) return 0L;
            Map<String, Double> zset = objectMapper.readValue(filePath.toFile(), Map.class);
            return (long) zset.size();
        } catch (IOException e) {
            return 0L;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long zcount(String key, double min, double max) {
        try {
            Path filePath = getFilePath(key, "zset");
            if (!Files.exists(filePath)) return 0L;
            Map<String, Double> zset = objectMapper.readValue(filePath.toFile(), Map.class);
            return zset.values().stream()
                    .filter(score -> score >= min && score <= max)
                    .count();
        } catch (IOException e) {
            return 0L;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> zrange(String key, long start, long end) {
        try {
            Path filePath = getFilePath(key, "zset");
            if (!Files.exists(filePath)) return new LinkedHashSet<>();
            Map<String, Double> zset = objectMapper.readValue(filePath.toFile(), Map.class);
            return sortedZrangeByValue(zset, start, end);
        } catch (IOException e) {
            return new LinkedHashSet<>();
        }
    }

    private Set<String> sortedZrangeByValue(Map<String, Double> zset, long start, long end) {
        List<String> sorted = sortZsetKeysByValue(zset);
        return sliceSortedKeysAsSet(sorted, start, end);
    }

    private List<String> sortZsetKeysByValue(Map<String, Double> zset) {
        return zset.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private Set<String> sliceSortedKeysAsSet(List<String> sorted, long start, long end) {
        int[] bounds = computeListBounds(sorted.size(), start, end);
        if (bounds[0] >= bounds[1] || bounds[0] >= sorted.size()) return new LinkedHashSet<>();
        return new LinkedHashSet<>(sorted.subList(bounds[0], bounds[1]));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Double> zrangeWithScores(String key, long start, long end) {
        try {
            Path filePath = getFilePath(key, "zset");
            if (!Files.exists(filePath)) return new LinkedHashMap<>();
            Map<String, Double> zset = objectMapper.readValue(filePath.toFile(), Map.class);
            return sortedZrangeWithScores(zset, start, end);
        } catch (IOException e) {
            return new LinkedHashMap<>();
        }
    }

    private Map<String, Double> sortedZrangeWithScores(Map<String, Double> zset, long start, long end) {
        List<Map.Entry<String, Double>> sorted = zset.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toList());
        return sliceSortedEntriesAsMap(sorted, start, end);
    }

    private Map<String, Double> sliceSortedEntriesAsMap(List<Map.Entry<String, Double>> sorted, long start, long end) {
        int[] bounds = computeListBounds(sorted.size(), start, end);
        Map<String, Double> result = new LinkedHashMap<>();
        if (bounds[0] < bounds[1] && bounds[0] < sorted.size()) {
            sorted.subList(bounds[0], bounds[1]).forEach(e -> result.put(e.getKey(), e.getValue()));
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> zrevrange(String key, long start, long end) {
        try {
            Path filePath = getFilePath(key, "zset");
            if (!Files.exists(filePath)) return new LinkedHashSet<>();
            Map<String, Double> zset = objectMapper.readValue(filePath.toFile(), Map.class);
            return sortedZrevrangeByValue(zset, start, end);
        } catch (IOException e) {
            return new LinkedHashSet<>();
        }
    }

    private Set<String> sortedZrevrangeByValue(Map<String, Double> zset, long start, long end) {
        List<String> sorted = sortZsetKeysByValueReversed(zset);
        return sliceSortedKeysAsSet(sorted, start, end);
    }

    private List<String> sortZsetKeysByValueReversed(Map<String, Double> zset) {
        return zset.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Double> zrevrangeWithScores(String key, long start, long end) {
        try {
            Path filePath = getFilePath(key, "zset");
            if (!Files.exists(filePath)) return new LinkedHashMap<>();
            Map<String, Double> zset = objectMapper.readValue(filePath.toFile(), Map.class);
            return sortedZrevrangeWithScores(zset, start, end);
        } catch (IOException e) {
            return new LinkedHashMap<>();
        }
    }

    private Map<String, Double> sortedZrevrangeWithScores(Map<String, Double> zset, long start, long end) {
        List<Map.Entry<String, Double>> sorted = zset.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toList());
        return sliceSortedEntriesAsMap(sorted, start, end);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> zrangeByScore(String key, double min, double max) {
        try {
            Path filePath = getFilePath(key, "zset");
            if (!Files.exists(filePath)) return new LinkedHashSet<>();
            Map<String, Double> zset = objectMapper.readValue(filePath.toFile(), Map.class);
            return zset.entrySet().stream()
                    .filter(e -> e.getValue() >= min && e.getValue() <= max)
                    .sorted(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        } catch (IOException e) {
            return new LinkedHashSet<>();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Double> zrangeByScoreWithScores(String key, double min, double max) {
        try {
            Path filePath = getFilePath(key, "zset");
            if (!Files.exists(filePath)) return new LinkedHashMap<>();
            Map<String, Double> zset = objectMapper.readValue(filePath.toFile(), Map.class);
            return zset.entrySet().stream()
                    .filter(e -> e.getValue() >= min && e.getValue() <= max)
                    .sorted(Map.Entry.comparingByValue())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
        } catch (IOException e) {
            return new LinkedHashMap<>();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Double zincrby(String key, double increment, String member) {
        try {
            Path filePath = getFilePath(key, "zset");
            Files.createDirectories(filePath.getParent());
            Map<String, Double> zset = loadOrCreateZset(filePath);
            double newScore = zset.getOrDefault(member, 0.0) + increment;
            zset.put(member, newScore);
            objectMapper.writeValue(filePath.toFile(), zset);
            return newScore;
        } catch (IOException e) {
            throw new RuntimeException("Failed to zincrby", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Double> loadOrCreateZset(Path filePath) throws IOException {
        return Files.exists(filePath)
                ? objectMapper.readValue(filePath.toFile(), Map.class)
                : new LinkedHashMap<>();
    }

    @Override
    public Long zremrangeByRank(String key, long start, long end) {
        Set<String> members = zrange(key, start, end);
        if (members.isEmpty()) return 0L;
        return zrem(key, members.toArray(new String[0]));
    }

    @Override
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
            if (delete(key)) count++;
        }
        return count;
    }

    @Override
    public boolean exists(String key) {
        if (isExpired(key)) {
            delete(key);
            return false;
        }
        try {
            return Files.walk(baseDir)
                    .anyMatch(path -> path.getFileName().toString().startsWith(encodeKey(key) + "."));
        } catch (IOException e) {
            return false;
        }
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
        try {
            String prefix = encodeKey(key) + ".";
            Optional<Path> path = Files.walk(baseDir)
                    .filter(p -> p.getFileName().toString().startsWith(prefix))
                    .findFirst();
            return path.map(this::mapFileNameToType).orElse("string");
        } catch (IOException e) {
            return "string";
        }
    }

    private String mapFileNameToType(Path path) {
        String filename = path.getFileName().toString();
        if (filename.endsWith(".string")) return "string";
        if (filename.endsWith(".hash")) return "hash";
        if (filename.endsWith(".list")) return "list";
        if (filename.endsWith(".set")) return "set";
        if (filename.endsWith(".zset")) return "zset";
        return "string";
    }

    @Override
    public void rename(String oldKey, String newKey) {
        try {
            String oldPrefix = encodeKey(oldKey);
            String newPrefix = encodeKey(newKey);
            Files.walk(baseDir)
                    .filter(path -> path.getFileName().toString().startsWith(oldPrefix + "."))
                    .forEach(oldPath -> renameMatchingFile(oldPath, oldPrefix, newPrefix));
            migrateExpireEntry(oldKey, newKey);
        } catch (IOException e) {
            throw new RuntimeException("Failed to rename", e);
        }
    }

    private void renameMatchingFile(Path oldPath, String oldPrefix, String newPrefix) {
        try {
            String filename = oldPath.getFileName().toString();
            String newFilename = filename.replace(oldPrefix, newPrefix);
            Path newPath = oldPath.getParent().resolve(newFilename);
            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to rename", e);
        }
    }

    private void migrateExpireEntry(String oldKey, String newKey) {
        Long expire = expireMap.remove(oldKey);
        if (expire != null) expireMap.put(newKey, expire);
    }

    @Override
    public boolean renamenx(String oldKey, String newKey) {
        if (exists(newKey)) return false;
        rename(oldKey, newKey);
        return true;
    }

    @Override
    public Set<String> keys(String pattern) {
        try {
            String regex = pattern.replace("*", ".*").replace("?", ".");
            return Files.walk(baseDir)
                    .filter(Files::isRegularFile)
                    .map(this::extractKeyFromFilename)
                    .filter(Objects::nonNull)
                    .filter(key -> key.matches(regex))
                    .filter(key -> !isExpired(key))
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            return new HashSet<>();
        }
    }

    private String extractKeyFromFilename(Path path) {
        String filename = path.getFileName().toString();
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? decodeKey(filename.substring(0, dotIndex)) : null;
    }

    @Override
    public String randomKey() {
        try {
            List<Path> files = Files.walk(baseDir)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
            if (files.isEmpty()) return null;
            Path randomFile = files.get(new Random().nextInt(files.size()));
            String filename = randomFile.getFileName().toString();
            int dotIndex = filename.lastIndexOf('.');
            return dotIndex > 0 ? decodeKey(filename.substring(0, dotIndex)) : null;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public boolean move(String key, int dbIndex) {
        throw new UnsupportedOperationException("Files实现不支持多数据库");
    }

    // ==================== 位图(Bitmap)操作 ====================

    @Override
    public boolean setbit(String key, long offset, boolean value) {
        throw new UnsupportedOperationException("Files实现不支持位图操作");
    }

    @Override
    public boolean getbit(String key, long offset) {
        throw new UnsupportedOperationException("Files实现不支持位图操作");
    }

    @Override
    public Long bitcount(String key) {
        throw new UnsupportedOperationException("Files实现不支持位图操作");
    }

    @Override
    public Long bitcount(String key, long start, long end) {
        throw new UnsupportedOperationException("Files实现不支持位图操作");
    }

    // ==================== HyperLogLog操作 ====================

    @Override
    public Long pfadd(String key, String... elements) {
        throw new UnsupportedOperationException("Files实现不支持HyperLogLog操作");
    }

    @Override
    public Long pfcount(String... keys) {
        throw new UnsupportedOperationException("Files实现不支持HyperLogLog操作");
    }

    @Override
    public void pfmerge(String destKey, String... sourceKeys) {
        throw new UnsupportedOperationException("Files实现不支持HyperLogLog操作");
    }

    // ==================== 事务和脚本操作 ====================

    @Override
    public Object exec(Runnable transaction) {
        transaction.run();
        return null;
    }

    @Override
    public Object eval(String script, List<String> keys, List<String> args) {
        throw new UnsupportedOperationException("Files实现不支持Lua脚本");
    }

    @Override
    public Object evalsha(String sha1, List<String> keys, List<String> args) {
        throw new UnsupportedOperationException("Files实现不支持Lua脚本");
    }

    // ==================== 发布订阅操作 ====================

    @Override
    public Long publish(String channel, String message) {
        throw new UnsupportedOperationException("Files实现不支持发布订阅");
    }

    @Override
    public void subscribe(String... channels) {
        throw new UnsupportedOperationException("Files实现不支持发布订阅");
    }

    @Override
    public void psubscribe(String... patterns) {
        throw new UnsupportedOperationException("Files实现不支持发布订阅");
    }

    @Override
    public void unsubscribe(String... channels) {
        throw new UnsupportedOperationException("Files实现不支持发布订阅");
    }

    @Override
    public void punsubscribe(String... patterns) {
        throw new UnsupportedOperationException("Files实现不支持发布订阅");
    }

    // ==================== 数据库操作 ====================

    @Override
    public Long dbsize() {
        try {
            return Files.walk(baseDir)
                    .filter(Files::isRegularFile)
                    .count();
        } catch (IOException e) {
            return 0L;
        }
    }

    @Override
    public void flushdb() {
        try {
            deleteAllFiles();
            Files.createDirectories(baseDir);
            expireMap.clear();
        } catch (IOException e) {
            throw new RuntimeException("Failed to flushdb", e);
        }
    }

    private void deleteAllFiles() throws IOException {
        Files.walk(baseDir)
                .sorted(Comparator.reverseOrder())
                .forEach(this::deleteQuietly);
    }

    private void deleteQuietly(Path path) {
        try {
            Files.delete(path);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void flushall() {
        flushdb();
    }

    @Override
    public void select(int dbIndex) {
        throw new UnsupportedOperationException("Files实现不支持多数据库");
    }

    // ==================== 其他实用方法 ====================

    @Override
    public Long delByPattern(String pattern) {
        Set<String> keys = keys(pattern);
        if (keys.isEmpty()) return 0L;
        return del(keys.toArray(new String[0]));
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

    // ==================== 辅助方法 ====================

    private Path getFilePath(String key, String type) {
        return baseDir.resolve(encodeKey(key) + "." + type);
    }

    private boolean delete(String key) {
        try {
            boolean deleted = false;
            for (Path path : Files.walk(baseDir)
                    .filter(p -> p.getFileName().toString().startsWith(encodeKey(key) + "."))
                    .collect(Collectors.toList())) {
                Files.deleteIfExists(path);
                deleted = true;
            }
            expireMap.remove(key);
            return deleted;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean isExpired(String key) {
        Long expireTime = expireMap.get(key);
        return expireTime != null && System.currentTimeMillis() > expireTime;
    }

    private String encodeKey(String key) {
        return key.replace("/", "_SLASH_")
                .replace("\\", "_BACKSLASH_")
                .replace(":", "_COLON_")
                .replace("*", "_STAR_")
                .replace("?", "_QUESTION_")
                .replace("\"", "_QUOTE_")
                .replace("<", "_LT_")
                .replace(">", "_GT_")
                .replace("|", "_PIPE_");
    }

    private String decodeKey(String encodedKey) {
        return encodedKey.replace("_SLASH_", "/")
                .replace("_BACKSLASH_", "\\")
                .replace("_COLON_", ":")
                .replace("_STAR_", "*")
                .replace("_QUESTION_", "?")
                .replace("_QUOTE_", "\"")
                .replace("_LT_", "<")
                .replace("_GT_", ">")
                .replace("_PIPE_", "|");
    }

    @SuppressWarnings("unchecked")
    private <T> T convertValue(String value, Class<T> type) {
        if (type == String.class) return (T) value;
        if (type == Integer.class || type == int.class) return (T) Integer.valueOf(value);
        if (type == Long.class || type == long.class) return (T) Long.valueOf(value);
        if (type == Double.class || type == double.class) return (T) Double.valueOf(value);
        if (type == Boolean.class || type == boolean.class) return (T) Boolean.valueOf(value);
        return (T) value;
    }
}

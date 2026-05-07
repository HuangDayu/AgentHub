package com.agenthub.infrastructure.tools.system_tools;

import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import org.springframework.ai.tool.annotation.Tool;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@AgentTools(name = "MemoryTools", description = "记忆存储工具，提供记忆的存储、搜索、更新、删除、导出等记忆管理功能")
public class MemoryTools {

    private final Map<String, MemoryEntry> memoryStore = new ConcurrentHashMap<>();

    @Tool(name = "memory_search", description = "Search memories by query")
    public String memorySearch(String query) {
        return memoryStore.values().stream()
            .filter(m -> m.content.toLowerCase().contains(query.toLowerCase()))
            .map(m -> m.id + ": " + m.content)
            .collect(Collectors.joining("\n"));
    }

    @Tool(name = "memory_get", description = "Get memory by ID")
    public String memoryGet(String memoryId) {
        MemoryEntry entry = memoryStore.get(memoryId);
        return entry != null ? entry.content : "Memory not found";
    }

    @Tool(name = "memory_store", description = "Store new memory")
    public String memoryStore(String content) {
        String id = UUID.randomUUID().toString();
        memoryStore.put(id, new MemoryEntry(id, content, System.currentTimeMillis()));
        return "Memory stored: " + id;
    }

    @Tool(name = "memory_store_with_tags", description = "Store memory with tags")
    public String memoryStoreWithTags(String content, String tags) {
        String id = UUID.randomUUID().toString();
        MemoryEntry entry = new MemoryEntry(id, content, System.currentTimeMillis());
        entry.tags = Arrays.asList(tags.split(","));
        memoryStore.put(id, entry);
        return "Memory stored: " + id;
    }

    @Tool(name = "memory_update", description = "Update existing memory")
    public String memoryUpdate(String memoryId, String newContent) {
        MemoryEntry entry = memoryStore.get(memoryId);
        if (entry == null) return "Memory not found";
        entry.content = newContent;
        entry.timestamp = System.currentTimeMillis();
        return "Memory updated: " + memoryId;
    }

    @Tool(name = "memory_delete", description = "Delete memory by ID")
    public String memoryDelete(String memoryId) {
        MemoryEntry removed = memoryStore.remove(memoryId);
        return removed != null ? "Memory deleted: " + memoryId : "Memory not found";
    }

    @Tool(name = "memory_list", description = "List all memories")
    public String memoryList() {
        return memoryStore.values().stream()
            .sorted(Comparator.comparingLong(m -> -m.timestamp))
            .map(m -> m.id + ": " + m.content.substring(0, Math.min(50, m.content.length())) + "...")
            .collect(Collectors.joining("\n"));
    }

    @Tool(name = "memory_list_by_tag", description = "List memories by tag")
    public String memoryListByTag(String tag) {
        return memoryStore.values().stream()
            .filter(m -> m.tags != null && m.tags.contains(tag))
            .map(m -> m.id + ": " + m.content)
            .collect(Collectors.joining("\n"));
    }

    @Tool(name = "memory_count", description = "Count total memories")
    public int memoryCount() {
        return memoryStore.size();
    }

    @Tool(name = "memory_clear", description = "Clear all memories")
    public String memoryClear() {
        int count = memoryStore.size();
        memoryStore.clear();
        return "Cleared " + count + " memories";
    }

    @Tool(name = "memory_export", description = "Export memories to JSON")
    public String memoryExport() {
        StringBuilder sb = new StringBuilder("[");
        memoryStore.values().forEach(m -> 
            sb.append("{\"id\":\"").append(m.id)
              .append("\",\"content\":\"").append(m.content)
              .append("\",\"timestamp\":").append(m.timestamp).append("},"));
        if (!memoryStore.isEmpty()) sb.setLength(sb.length() - 1);
        return sb.append("]").toString();
    }

    @Tool(name = "memory_recent", description = "Get recent memories")
    public String memoryRecent(int count) {
        return memoryStore.values().stream()
            .sorted(Comparator.comparingLong(m -> -m.timestamp))
            .limit(count)
            .map(m -> m.id + ": " + m.content)
            .collect(Collectors.joining("\n"));
    }

    @Tool(name = "memory_search_regex", description = "Search memories with regex")
    public String memorySearchRegex(String pattern) {
        return memoryStore.values().stream()
            .filter(m -> m.content.matches(pattern))
            .map(m -> m.id + ": " + m.content)
            .collect(Collectors.joining("\n"));
    }

    private static class MemoryEntry {
        String id, content;
        long timestamp;
        List<String> tags;
        MemoryEntry(String id, String content, long timestamp) {
            this.id = id; this.content = content; this.timestamp = timestamp;
        }
    }
}

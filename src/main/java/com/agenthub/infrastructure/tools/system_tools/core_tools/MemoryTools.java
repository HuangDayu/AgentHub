package com.agenthub.infrastructure.tools.system_tools.core_tools;

import com.agenthub.application.port.out.repositories.MemoryRepository;
import com.agenthub.domain.enums.MemoryType;
import com.agenthub.domain.model.Memory;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;
import java.util.stream.Collectors;

import static com.agenthub.infrastructure.tools.system_tools.SystemToolsUtils.getAgentContext;

/**
 * 记忆工具，提供记忆的获取、搜索、保存和删除功能。
 */
@RequiredArgsConstructor
@AgentTools(name = "MemoryTools", description = "记忆工具，提供记忆的获取、搜索、保存和删除功能")
public class MemoryTools {

    private final MemoryRepository memoryRepository;

    @Tool(description = "根据记忆ID获取记忆内容")
    public Memory memoryGet(@ToolParam(description = "记忆ID") String memoryId) {
        return memoryRepository.findById(memoryId).orElse(null);
    }

    @Tool(description = "搜索记忆，根据关键词在记忆内容和名称中匹配")
    public List<Memory> memorySearch(
            @ToolParam(description = "搜索关键词（可选，为空则返回所有记忆）") String keyword,
            ToolContext toolContext) {
        ReActAgentContext context = getAgentContext(toolContext);
        List<Memory> allMemories = memoryRepository.findByAgentId(context.getAgent().getId());
        if (keyword == null || keyword.isBlank()) {
            return allMemories;
        }
        return allMemories.stream()
                .filter(m -> matchesKeyword(m, keyword))
                .collect(Collectors.toList());
    }

    @Tool(description = "保存一条新记忆")
    public Memory memorySave(
            ToolContext toolContext,
            @ToolParam(description = "记忆内容") String content,
            @ToolParam(description = "记忆名称/标题") String name,
            @ToolParam(description = "记忆类型：SHORT_TERM / LONG_TERM / EPISODIC / SEMANTIC") MemoryType memoryType) {
        ReActAgentContext context = getAgentContext(toolContext);
        Memory memory = new Memory();
        memory.setAgentId(context.getAgent().getId());
        memory.setContent(content);
        memory.setName(name);
        memory.setMemoryType(memoryType);
        return memoryRepository.save(memory);
    }

    @Tool(description = "删除指定记忆")
    public String memoryDelete(@ToolParam(description = "记忆ID") String memoryId) {
        memoryRepository.deleteById(memoryId);
        return "记忆已删除: " + memoryId;
    }

    @Tool(description = "获取当前Agent的所有记忆列表")
    public List<Memory> memoryList(ToolContext toolContext) {
        ReActAgentContext context = getAgentContext(toolContext);
        return memoryRepository.findByAgentId(context.getAgent().getId());
    }

    private boolean matchesKeyword(Memory memory, String keyword) {
        String lower = keyword.toLowerCase();
        if (memory.getContent() != null && memory.getContent().toLowerCase().contains(lower)) {
            return true;
        }
        if (memory.getName() != null && memory.getName().toLowerCase().contains(lower)) {
            return true;
        }
        return false;
    }
}

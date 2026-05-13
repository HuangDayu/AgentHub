package com.agenthub.infrastructure.tools.system_tools.base_tools;

import com.agenthub.application.port.out.repositories.MemoryRepository;
import com.agenthub.domain.model.Memory;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;

/**
 * @author huangdayu
 */
@RequiredArgsConstructor
@AgentTools(name = "MemoryTools", description = "记忆工具，提供记忆的获取和搜索功能")
public class MemoryTools {

    private final MemoryRepository memoryRepository;

    @Tool(description = "根据记忆ID获取记忆内容")
    public Memory memoryGet(@ToolParam String memoryId) {
        return memoryRepository.findById(memoryId).orElse(null);
    }

    @Tool(description = "搜索记忆，根据智能体ID查找相关记忆")
    public List<Memory> memorySearch(@ToolParam String agentId) {
        return memoryRepository.findByAgentId(agentId);
    }

    @Tool(description = "保存记忆")
    public Memory memorySave(@ToolParam String agentId, @ToolParam String content, @ToolParam String memoryType) {
        Memory memory = new Memory();
        memory.setAgentId(agentId);
        memory.setContent(content);
        memory.setMemoryType(memoryType);
        return memoryRepository.save(memory);
    }

    @Tool(description = "删除记忆")
    public void memoryDelete(@ToolParam String memoryId) {
        memoryRepository.deleteById(memoryId);
    }
}

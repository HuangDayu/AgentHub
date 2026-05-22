package com.agenthub.infrastructure.agents.aliyun.memory;

import io.agentscope.harness.agent.memory.compaction.CompactionConfig;
import io.agentscope.harness.agent.memory.compaction.ToolResultEvictionConfig;
import org.springframework.stereotype.Component;

/**
 * AgentScope 记忆配置工厂。
 */
@Component
public class MemoryConfigFactory {

    public CompactionConfig createDefaultCompactionConfig() {
        return CompactionConfig.builder()
                .triggerMessages(30)
                .keepMessages(10)
                .flushBeforeCompact(true)
                .build();
    }

    public CompactionConfig createCompactionConfig(int triggerMessages, int keepMessages, boolean flushBeforeCompact) {
        return CompactionConfig.builder()
                .triggerMessages(triggerMessages)
                .keepMessages(keepMessages)
                .flushBeforeCompact(flushBeforeCompact)
                .build();
    }

    public ToolResultEvictionConfig createDefaultToolResultEvictionConfig() {
        return ToolResultEvictionConfig.defaults();
    }

    public ToolResultEvictionConfig createToolResultEvictionConfig(int maxResultChars, int previewChars) {
        return ToolResultEvictionConfig.builder()
                .maxResultChars(maxResultChars)
                .previewChars(previewChars)
                .build();
    }
}

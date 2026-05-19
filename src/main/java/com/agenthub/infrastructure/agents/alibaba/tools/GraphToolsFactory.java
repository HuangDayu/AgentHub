package com.agenthub.infrastructure.agents.alibaba.tools;

import com.agenthub.domain.model.agent.ReActAgentWorkspace;
import com.alibaba.cloud.ai.graph.agent.tools.GlobSearchTool;
import com.alibaba.cloud.ai.graph.agent.tools.GrepSearchTool;
import com.alibaba.cloud.ai.graph.agent.tools.WriteTodosTool;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author huangdayu
 */
@Component
public class GraphToolsFactory {

    public List<ToolCallback> getToolCallbacks(ReActAgentWorkspace workspace) {
        return List.of(
                WriteTodosTool.builder().build(),
                GrepSearchTool.builder(workspace.getRootPath().toString()).build(),
                GlobSearchTool.builder(workspace.getRootPath().toString()).build()
        );
    }

}

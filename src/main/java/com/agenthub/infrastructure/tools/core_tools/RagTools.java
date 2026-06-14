package com.agenthub.infrastructure.tools.core_tools;

import com.agenthub.application.command.RagCommand;
import com.agenthub.application.port.out.rag.RetrievalAugmentedGenerationPort;
import com.agenthub.application.port.out.repositories.KnowledgeBaseRepository;
import com.agenthub.domain.model.KnowledgeBase;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.domain.model.rag.RetrievalChunk;
import com.agenthub.infrastructure.tools.annotations.AgentTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;

import java.util.List;

import static com.agenthub.infrastructure.tools.SystemToolsUtils.getAgentContext;

/**
 * @author huangdayu
 */
@AgentTools(name = "RagTools", description = "RAG工具，提供知识库查询功能")
@RequiredArgsConstructor
public class RagTools {

    private final RetrievalAugmentedGenerationPort retrievalAugmentedGenerationPort;

    @Tool(description = "知识库检索")
    public List<RetrievalChunk> ragQuery(String question, List<String> kdIds, ToolContext toolContext) {
        ReActAgentContext context = getAgentContext(toolContext);
        RagCommand ragCommand = RagCommand.builder().kbIds(kdIds).prompt(question)
                .agentId(context.getAgent().getId())
                .strategy(context.getRetrievalStrategy())
                .build();
        return retrievalAugmentedGenerationPort.ragRetrieve(ragCommand);
    }

}

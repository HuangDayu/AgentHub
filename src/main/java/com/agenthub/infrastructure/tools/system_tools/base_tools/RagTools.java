package com.agenthub.infrastructure.tools.system_tools.base_tools;

import com.agenthub.application.command.RagCommand;
import com.agenthub.application.port.out.rag.RetrievalAugmentedGenerationPort;
import com.agenthub.application.port.out.repositories.KnowledgeBaseRepository;
import com.agenthub.domain.model.KnowledgeBase;
import com.agenthub.domain.model.ReActAgentContext;
import com.agenthub.domain.model.RetrievalChunk;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;

import java.util.List;

import static com.agenthub.common.constants.AgentConstants.AGENT_CONTEXT_KEY;

/**
 * @author huangdayu
 */
@AgentTools(name = "RagTools", description = "RAG工具，提供知识库查询功能")
@RequiredArgsConstructor
public class RagTools {

    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final RetrievalAugmentedGenerationPort retrievalAugmentedGenerationPort;

    @Tool(description = "获取知识库列表")
    public List<KnowledgeBase> getKnowledgeBases(ToolContext toolContext) {
        ReActAgentContext context = (ReActAgentContext) toolContext.getContext().get(AGENT_CONTEXT_KEY);
        return knowledgeBaseRepository.findByIds(context.getKnowledgeIds());
    }

    @Tool(description = "知识库查询")
    public List<RetrievalChunk> ragQuery(String question, List<String> kdIds, ToolContext toolContext) {
        ReActAgentContext context = (ReActAgentContext) toolContext.getContext().get(AGENT_CONTEXT_KEY);
        RagCommand ragCommand = RagCommand.builder().kbIds(kdIds).prompt(question)
                .agentId(context.getAgent().getId())
                .strategy(context.getRetrievalStrategy())
                .build();
        return retrievalAugmentedGenerationPort.ragRetrieve(ragCommand);
    }

}

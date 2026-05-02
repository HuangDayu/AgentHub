package com.agenthub.application.port.out.rag;

import com.agenthub.application.command.RagCommand;
import com.agenthub.application.command.RetrievalCommand;
import com.agenthub.application.dto.RetrievalOutput;
import com.agenthub.domain.model.RetrievalChunk;

import java.util.List;

/**
 * 检索端口 - 执行知识检索
 */
public interface RagRetrievalPort {

    List<RetrievalChunk> retrieve(RagCommand query);

    RetrievalOutput retrieve(RetrievalCommand retrievalCommand);
}

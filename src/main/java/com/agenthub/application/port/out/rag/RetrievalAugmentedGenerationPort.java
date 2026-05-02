package com.agenthub.application.port.out.rag;

import com.agenthub.application.command.RagCommand;
import reactor.core.publisher.Flux;

/**
 * RAG（检索，增强，生成）的用例是通过从数据体中提取相关信息，增强生成模型的能力，从而提升生成输出的质量和相关性。
 *
 * @author huangdayu
 */
public interface RetrievalAugmentedGenerationPort {

    String ragChat(RagCommand ragCommand);

    Flux<String> ragStream(RagCommand ragCommand);
}

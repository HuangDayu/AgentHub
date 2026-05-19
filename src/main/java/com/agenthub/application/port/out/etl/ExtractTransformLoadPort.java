package com.agenthub.application.port.out.etl;

import com.agenthub.application.command.EtlCommand;
import com.agenthub.domain.model.etl.DocumentChunk;

import java.util.List;

/**
 * ETL（提取，转换，加载）流水线协调从原始数据源到结构化向量存储的流程，确保数据格式为AI模型检索的最佳格式。
 *
 * @author huangdayu
 */
public interface ExtractTransformLoadPort {

    List<DocumentChunk> etl(EtlCommand etlCommand);

    boolean delete(List<DocumentChunk> documentChunks);

}

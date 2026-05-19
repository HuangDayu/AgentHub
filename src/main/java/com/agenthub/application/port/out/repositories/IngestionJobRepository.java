package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.etl.IngestionJob;

import java.util.Optional;

/**
 * 入库任务仓储接口。
 */
public interface IngestionJobRepository {

    /**
     * 保存入库任务。
     *
     * @param job 待保存的入库任务
     * @return 保存后的入库任务
     */
    IngestionJob save(IngestionJob job);

    /**
     * 根据任务ID查询入库任务。
     *
     * @param jobId 任务ID
     * @return 包含入库任务的Optional，不存在时为empty
     */
    Optional<IngestionJob> findById(String jobId);
}

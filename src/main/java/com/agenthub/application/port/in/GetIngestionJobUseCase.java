package com.agenthub.application.port.in;

import com.agenthub.domain.model.IngestionJob;

/**
 * 查询入库任务用例接口。
 * <p>
 * 定义查询入库任务的业务操作契约。
 * </p>
 */
public interface GetIngestionJobUseCase {

    /**
     * 根据任务ID查询入库任务。
     *
     * @param jobId 任务ID
     * @return 入库任务
     */
    IngestionJob getJobById(String jobId);
}

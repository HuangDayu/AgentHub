package com.agenthub.application.port.in;

import com.agenthub.application.command.CreateIngestionJobCommand;
import com.agenthub.domain.model.IngestionJob;

/**
 * 创建入库任务用例接口。
 * <p>
 * 定义创建入库任务的业务操作契约。
 * </p>
 */
public interface CreateIngestionJobUseCase {

    /**
     * 创建入库任务。
     *
     * @param command 创建任务命令
     * @return 创建的入库任务
     */
    IngestionJob createJob(CreateIngestionJobCommand command);
}

package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.HttpTool;

import java.util.List;
import java.util.Optional;

/**
 * 工具仓储接口，定义工具持久化操作。
 * <p>
 * 实现类负责工具的增删改查。
 */
public interface HttpToolRepository {

    /**
     * 保存工具。
     *
     * @param httpTool 工具领域对象
     * @return 保存后的工具
     */
    HttpTool save(HttpTool httpTool);

    /**
     * 根据 ID 查找工具。
     *
     * @param id 工具标识
     * @return 工具（可能为空）
     */
    Optional<HttpTool> findById(String id);

    /**
     * 查询所有工具。
     *
     * @return 工具列表
     */
    List<HttpTool> findAll();

    List<HttpTool> findByWorkspaceId(String workspaceId);

    List<HttpTool> findByIds(List<String> toolIds);

}

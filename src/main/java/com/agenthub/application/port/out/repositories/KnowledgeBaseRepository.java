package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.KnowledgeBase;
import com.agenthub.domain.model.PageResult;

import java.util.List;
import java.util.Optional;

/**
 * 知识库仓储接口。
 *
 * <p>定义知识库的 CRUD 操作和分页查询契约，
 * 由基础设施层提供具体实现（内存实现 / MyBatis-Plus 实现）。</p>
 *
 */
public interface KnowledgeBaseRepository {

    /**
     * 判断知识库是否存在。
     *
     * @param kbId 知识库ID
     * @return 存在返回 true，否则返回 false
     */
    boolean existsById(String kbId);

    /**
     * 保存知识库（新增或更新）。
     *
     * @param knowledgeBase 待保存的知识库
     * @return 保存后的知识库
     */
    KnowledgeBase save(KnowledgeBase knowledgeBase);

    /**
     * 根据 ID 查询知识库。
     *
     * @param kbId 知识库ID
     * @return 包含知识库的 Optional，不存在时为 empty
     */
    Optional<KnowledgeBase> findById(String kbId);

    /**
     * 查询所有知识库。
     *
     * @return 知识库列表
     */
    List<KnowledgeBase> findAll();

    /**
     * 分页查询知识库。
     *
     * @param page 页码（从 0 开始）
     * @param size 每页记录数
     * @return 分页结果
     */
    PageResult<KnowledgeBase> findAll(int page, int size);

    /**
     * 根据 ID 删除知识库。
     *
     * @param kbId 知识库ID
     */
    void deleteById(String kbId);

    /**
     * 根据租户id查询知识库
     *
     * @param tenantId
     * @return
     */
    List<KnowledgeBase> findByTenantId(String tenantId);

    /**
     * 根据租户id查询知识库
     *
     * @param tenantId
     * @return
     */
    Optional<KnowledgeBase> findByKbIdAndTenantId(String tenantId, String kbId);

    /**
     * 根据知识库编码查询知识库
     *
     * @param kbCode
     * @return
     */
    boolean existsByKbCode(String kbCode);

    List<KnowledgeBase> findByWorkspace(String workspaceId);
}


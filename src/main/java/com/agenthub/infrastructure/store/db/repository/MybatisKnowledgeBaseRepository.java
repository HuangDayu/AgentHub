package com.agenthub.infrastructure.store.db.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.agenthub.application.port.out.repositories.KnowledgeBaseRepository;
import com.agenthub.domain.model.KnowledgeBase;
import com.agenthub.domain.model.PageResult;
import com.agenthub.infrastructure.store.db.entity.KnowledgeBaseEntity;
import com.agenthub.infrastructure.store.db.mapper.KnowledgeBaseMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 知识库仓储接口。
 */
@Repository
@Primary
public class MybatisKnowledgeBaseRepository implements KnowledgeBaseRepository {
    private static final String VERSION_SPLITTER = "|";
    private final KnowledgeBaseMapper mapper;

    /**
     * 构造函数，注入 MyBatis Mapper。
     *
     * @param mapper 知识库数据访问 Mapper
     */
    public MybatisKnowledgeBaseRepository(KnowledgeBaseMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 判断指定知识库是否存在。
     *
     * @param kbId 知识库ID
     * @return 存在返回 true
     */
    @Override
    public boolean existsById(String kbId) {
        LambdaQueryWrapper<KnowledgeBaseEntity> qw = new LambdaQueryWrapper<>();
        qw.eq(KnowledgeBaseEntity::getId, kbId);
        return mapper.selectOne(qw) != null;
    }

    /**
     * 保存知识库（新增或更新）。
     *
     * <p>根据 kbCode 判断是否已存在：存在则更新，不存在则插入。</p>
     *
     * @param knowledgeBase 知识库领域对象
     * @return 保存后的知识库
     */
    @Override
    public KnowledgeBase save(KnowledgeBase knowledgeBase) {
        KnowledgeBaseEntity entity = toEntity(knowledgeBase);
        upsert(entity);
        return toDomain(entity);
    }

    /**
     * 根据 ID 查询知识库。
     *
     * @param kbId 知识库ID
     * @return 包含知识库的 Optional，不存在时为 empty
     */
    @Override
    public Optional<KnowledgeBase> findById(String kbId) {
        LambdaQueryWrapper<KnowledgeBaseEntity> qw = new LambdaQueryWrapper<>();
        qw.eq(KnowledgeBaseEntity::getId, kbId);
        KnowledgeBaseEntity entity = mapper.selectOne(qw);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    /**
     * 查询所有知识库，按名称排序。
     *
     * @return 知识库列表
     */
    @Override
    public List<KnowledgeBase> findAll() {
        LambdaQueryWrapper<KnowledgeBaseEntity> query = new LambdaQueryWrapper<>();
        query.orderByAsc(KnowledgeBaseEntity::getName);
        return mapper.selectList(query).stream().map(this::toDomain).toList();
    }

    /**
     * 分页查询知识库，按名称排序。
     *
     * @param page 页码（从 0 开始）
     * @param size 每页记录数
     * @return 分页结果
     */
    @Override
    public PageResult<KnowledgeBase> findAll(int page, int size) {
        Page<KnowledgeBaseEntity> pageParam = new Page<>(page + 1, size);
        LambdaQueryWrapper<KnowledgeBaseEntity> query = new LambdaQueryWrapper<>();
        query.orderByAsc(KnowledgeBaseEntity::getName);
        IPage<KnowledgeBaseEntity> pageResult = mapper.selectPage(pageParam, query);
        List<KnowledgeBase> content = pageResult.getRecords().stream()
                .map(this::toDomain)
                .toList();
        return new PageResult<>(content, pageResult.getTotal(), page, size);
    }

    /**
     * 根据 ID 删除知识库。
     *
     * @param kbId 知识库ID
     */
    @Override
    public void deleteById(String kbId) {
        LambdaQueryWrapper<KnowledgeBaseEntity> qw = new LambdaQueryWrapper<>();
        qw.eq(KnowledgeBaseEntity::getId, kbId);
        mapper.delete(qw);
    }

    @Override
    public List<KnowledgeBase> findByTenantId(String tenantId) {
        LambdaQueryWrapper<KnowledgeBaseEntity> query = new LambdaQueryWrapper<>();
        query.eq(KnowledgeBaseEntity::getTenantId, tenantId);
        query.orderByAsc(KnowledgeBaseEntity::getName);
        return mapper.selectList(query).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<KnowledgeBase> findByKbIdAndTenantId(String tenantId, String kbId) {
        LambdaQueryWrapper<KnowledgeBaseEntity> query = new LambdaQueryWrapper<>();
        query.eq(KnowledgeBaseEntity::getId, kbId);
        query.eq(KnowledgeBaseEntity::getTenantId, tenantId);
        KnowledgeBaseEntity entity = mapper.selectOne(query);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public boolean existsByKbCode(String kbCode) {
        LambdaQueryWrapper<KnowledgeBaseEntity> qw = new LambdaQueryWrapper<>();
        qw.eq(KnowledgeBaseEntity::getKbCode, kbCode);
        return mapper.selectOne(qw) != null;
    }

    @Override
    public List<KnowledgeBase> findByWorkspace(String workspaceId) {
        LambdaQueryWrapper<KnowledgeBaseEntity> query = new LambdaQueryWrapper<>();
        query.eq(KnowledgeBaseEntity::getWorkspaceId, workspaceId);
        return mapper.selectList(query).stream().map(this::toDomain).toList();
    }

    /**
     * 执行插入或更新操作，根据 kbCode 判断是否存在。
     *
     * @param entity 知识库数据库实体
     */
    private void upsert(KnowledgeBaseEntity entity) {
        LambdaQueryWrapper<KnowledgeBaseEntity> qw = new LambdaQueryWrapper<>();
        qw.eq(KnowledgeBaseEntity::getId, entity.getId());
        KnowledgeBaseEntity existing = mapper.selectOne(qw);
        if (existing == null) {
            mapper.insert(entity);
        } else {
            entity.setId(existing.getId());
            mapper.updateById(entity);
        }
    }

    /**
     * 将领域对象转换为数据库实体。
     */
    private KnowledgeBaseEntity toEntity(KnowledgeBase model) {
        KnowledgeBaseEntity entity = createBaseEntity(model);
        if (model.vectorStoreConfigId() != null) {
            entity.setVectorStoreConfigId(model.vectorStoreConfigId().toString());
        } else {
            entity.setVectorStoreConfigId(null);
        }
        return entity;
    }

    /**
     * 创建基础实体并填充核心字段。
     */
    private KnowledgeBaseEntity createBaseEntity(KnowledgeBase model) {
        KnowledgeBaseEntity entity = new KnowledgeBaseEntity();
        entity.setId(model.id());
        entity.setKbCode(model.kbCode());
        entity.setName(model.name());
        entity.setTenantId(model.tenantId());
        entity.setDescription(model.description());
        entity.setStatus("ACTIVE");
        entity.setVectorStoreConfigId(model.vectorStoreConfigId());
        entity.setEmbeddingModelConfigId(model.embeddingModelConfigId());
        entity.setChatModelConfigId(model.chatModelConfigId());
        entity.setCreatedAt(model.createdAt());
        entity.setUpdatedAt(model.updatedAt());
        return entity;
    }




    /**
     * 将数据库实体转换为领域对象。
     *
     * @param entity 知识库数据库实体
     * @return 知识库领域对象
     */
    private KnowledgeBase toDomain(KnowledgeBaseEntity entity) {
        Instant createdAt = defaultInstant(entity.getCreatedAt());
        Instant updatedAt = defaultInstant(entity.getUpdatedAt());
        return new KnowledgeBase(
                entity.getId(),
                entity.getKbCode(),
                entity.getName(),
                entity.getTenantId(),
                entity.getDescription(),
                entity.getVectorStoreConfigId(),
                entity.getEmbeddingModelConfigId(),
                entity.getChatModelConfigId(),
                createdAt,
                updatedAt
        );
    }

    /**
     * 拆分管道符分隔的索引版本字符串。
     *
     * @param rawVersions 原始版本字符串
     * @return 版本列表
     */
    private List<String> splitVersions(String rawVersions) {
        if (rawVersions == null || rawVersions.isBlank()) {
            return List.of("v1");
        }
        return List.of(rawVersions.split("\\|"));
    }

    /**
     * 返回非空的 Instant，若输入为 null 则返回当前时间。
     *
     * @param instant 原始时间
     * @return 非空的 Instant
     */
    private Instant defaultInstant(Instant instant) {
        return instant == null ? Instant.now() : instant;
    }

    /**
     * 解析版本号字符串（如 "v1"→1），解析失败时返回默认值 1。
     *
     * @param version 版本字符串
     * @return 版本号整数
     */
    private int parseVersionNumber(String version) {
        if (version == null || version.isBlank()) {
            return 1;
        }
        try {
            // 去掉前缀 "v" 后解析数字
            String num = version.startsWith("v") ? version.substring(1) : version;
            return Integer.parseInt(num);
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}

package com.agenthub.infrastructure.store.db.repository;

import com.agenthub.application.port.out.repositories.IngestionJobRepository;
import com.agenthub.domain.model.IngestionJob;
import com.agenthub.infrastructure.store.db.entity.IngestionJobEntity;
import com.agenthub.infrastructure.store.db.mapper.IngestionJobMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * MyBatis实现的入库任务仓库。
 */
@Repository
@Primary
public class MybatisIngestionJobRepository implements IngestionJobRepository {
    private final IngestionJobMapper mapper;

    /**
     * 构造函数。
     */
    public MybatisIngestionJobRepository(IngestionJobMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 保存任务。
     */
    @Override
    public IngestionJob save(IngestionJob job) {
        IngestionJobEntity entity = toEntity(job);
        upsert(entity);
        return job;
    }

    /**
     * 根据ID查找任务。
     */
    @Override
    public Optional<IngestionJob> findById(String jobId) {
        IngestionJobEntity entity = mapper.selectById(jobId);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    /**
     * 执行插入或更新操作。
     */
    private void upsert(IngestionJobEntity entity) {
        if (mapper.selectById(entity.getId()) == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
    }

    /**
     * 将领域对象转换为数据库实体。
     */
    private IngestionJobEntity toEntity(IngestionJob job) {
        IngestionJobEntity entity = new IngestionJobEntity();
        entity.setId(job.getJobId());
        entity.setKbId(job.getKbId());
        entity.setStatus(job.getStatus().name());
        entity.setProgress(0);
        entity.setDocumentCount(job.getDocumentCount());
        entity.setErrorMessage(job.getErrorMessage());
        entity.setCreatedAt(job.getCreatedAt());
        entity.setUpdatedAt(job.getUpdatedAt());
        return entity;
    }

    /**
     * 将数据库实体转换为领域对象。
     */
    private IngestionJob toDomain(IngestionJobEntity entity) {
        int count = entity.getDocumentCount() == null ? 0 : entity.getDocumentCount();
        IngestionJob job = IngestionJob.create(entity.getId(), entity.getKbId(), count);
        String status = entity.getStatus();
        if (status != null) {
            job = applyStatus(job, status, entity.getErrorMessage());
        }
        return job;
    }

    /**
     * 应用状态转换。
     */
    private IngestionJob applyStatus(IngestionJob job, String status, String errorMessage) {
        return switch (status) {
            case "PENDING" -> job;
            case "PARSING" -> job.markParsing();
            case "CLEANING" -> job.markCleaning();
            case "CHUNKING" -> job.markChunking();
            case "VECTORIZING" -> job.markVectorizing();
            case "COMPLETED" -> job.markCompleted();
            case "FAILED" -> job.markFailed(errorMessage != null ? errorMessage : "Unknown error");
            default -> job;
        };
    }
}

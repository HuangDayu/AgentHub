package com.agenthub.infrastructure.store.db.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.application.port.out.repositories.IngestionDocumentRepository;
import com.agenthub.domain.model.etl.IngestionDocument;
import com.agenthub.infrastructure.store.db.entity.IngestionDocumentEntity;
import com.agenthub.infrastructure.store.db.mapper.IngestionDocumentMybatisMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MyBatis实现的入库文档仓库。
 * <p>
 * 使用MyBatis Plus进行数据库操作，支持文档的CRUD查询。
 * </p>
 */
@Repository
@Primary
public class MybatisIngestionDocumentRepository implements IngestionDocumentRepository {
    private final IngestionDocumentMybatisMapper mapper;

    public MybatisIngestionDocumentRepository(IngestionDocumentMybatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void saveAll(List<IngestionDocument> documents) {
        documents.stream().map(this::toEntity).forEach(mapper::insert);
    }

    @Override
    public IngestionDocument save(IngestionDocument documents) {
        IngestionDocumentEntity entity = toEntity(documents);
        mapper.insert(entity);
        return toDomain(entity);
    }

    @Override
    public List<IngestionDocument> findByJobId(String jobId) {
        LambdaQueryWrapper<IngestionDocumentEntity> query = new LambdaQueryWrapper<>();
        query.eq(IngestionDocumentEntity::getJobId, jobId);
        return mapper.selectList(query).stream().map(this::toDomain).toList();
    }

    @Override
    public List<IngestionDocument> findByKbId(String kbId) {
        LambdaQueryWrapper<IngestionDocumentEntity> query = new LambdaQueryWrapper<>();
        query.eq(IngestionDocumentEntity::getKbId, kbId);
        return mapper.selectList(query).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(String documentId) {
        mapper.deleteById(documentId);
    }

    @Override
    public void updateAll(List<IngestionDocument> documents) {
        documents.stream().map(this::toEntity).forEach(mapper::updateById);
    }

    @Override
    public IngestionDocument findByDocId(String docId) {
        IngestionDocumentEntity document = mapper.selectById(docId);
        if (document == null) {
            throw new NotFoundException("未找到入库文档");
        }
        return toDomain(document);
    }

    /**
     * 将领域对象转换为数据库实体。
     *
     * @param document 入库文档领域对象
     * @return 数据库实体
     */
    private IngestionDocumentEntity toEntity(IngestionDocument document) {
        IngestionDocumentEntity entity = new IngestionDocumentEntity();
        entity.setId(document.getId());
        entity.setKbId(document.getKbId());
        entity.setJobId(document.getJobId());
        entity.setFileName(document.getFileName());
        entity.setContentType(document.getContentType());
        entity.setSize(document.getSize());
        entity.setStoragePath(document.getStoragePath());
        entity.setStatus(document.getStatus().name());
        return entity;
    }

    /**
     * 将数据库实体转换为领域对象。
     *
     * @param entity 数据库实体
     * @return 入库文档领域对象
     */
    private IngestionDocument toDomain(IngestionDocumentEntity entity) {
        IngestionDocument.Snapshot snapshot = new IngestionDocument.Snapshot(
                entity.getId(),
                entity.getKbId(),
                entity.getJobId(),
                entity.getFileName(),
                entity.getContentType(),
                entity.getSize() != null ? entity.getSize() : 0L,
                entity.getStoragePath(),
                IngestionDocument.DocumentStatus.valueOf(entity.getStatus())
        );
        return IngestionDocument.reconstruct(snapshot);
    }
}


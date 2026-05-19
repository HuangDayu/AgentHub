package com.agenthub.infrastructure.store.db.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.agenthub.application.port.out.repositories.IngestionDocumentChunkRepository;
import com.agenthub.domain.model.etl.DocumentChunk;
import com.agenthub.infrastructure.store.db.entity.DocumentChunkEntity;
import com.agenthub.infrastructure.store.db.mapper.DocumentChunkMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MyBatis实现的文档分块仓库。
 * <p>
 * 使用MyBatis Plus进行数据库操作，支持文档分块的CRUD查询。
 * </p>
 */
@Repository
@Primary
public class MybatisDocumentChunkRepository implements IngestionDocumentChunkRepository {

    private final DocumentChunkMapper mapper;

    public MybatisDocumentChunkRepository(DocumentChunkMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void saveAll(List<DocumentChunk> allChunks) {
        allChunks.stream().map(this::toEntity).forEach(mapper::insert);
    }

    @Override
    public List<DocumentChunk> findList(String kbId, String docId) {
        LambdaQueryWrapper<DocumentChunkEntity> query = buildQueryWrapper(kbId, docId);
        return mapper.selectList(query).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteAll(String kbId, String docId) {
        LambdaQueryWrapper<DocumentChunkEntity> query = buildQueryWrapper(kbId, docId);
        mapper.delete(query);
    }

    private LambdaQueryWrapper<DocumentChunkEntity> buildQueryWrapper(String kbId, String docId) {
        return new LambdaQueryWrapper<DocumentChunkEntity>()
                .eq(DocumentChunkEntity::getKbId, kbId)
                .eq(DocumentChunkEntity::getDocumentId, docId);
    }

    private DocumentChunkEntity toEntity(DocumentChunk chunk) {
        DocumentChunkEntity entity = new DocumentChunkEntity();
        entity.setChunkId(chunk.getChunkId());
        entity.setDocumentId(chunk.getDocumentId());
        entity.setKbId(chunk.getKbId());
        entity.setChunkIndex(chunk.getChunkIndex());
        entity.setTokenCount(chunk.getTokenCount());
        return entity;
    }

    private DocumentChunk toDomain(DocumentChunkEntity entity) {
        return DocumentChunk.reconstruct(
                entity.getChunkId(), entity.getDocumentId(), entity.getKbId(),
                entity.getChunkIndex(), null, entity.getTokenCount(),null
        );
    }
}

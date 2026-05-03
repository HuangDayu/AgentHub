package com.agenthub.infrastructure.persistence.db.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.agenthub.application.port.out.repositories.RetrievalStrategyRepository;
import com.agenthub.domain.model.RetrievalStrategy;
import com.agenthub.infrastructure.persistence.db.entity.RetrievalStrategyEntity;
import com.agenthub.infrastructure.persistence.db.mapper.RetrievalStrategyMybatisMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MybatisRetrievalStrategyRepository implements RetrievalStrategyRepository {
    private final RetrievalStrategyMybatisMapper mapper;

    public MybatisRetrievalStrategyRepository(RetrievalStrategyMybatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public RetrievalStrategy save(RetrievalStrategy strategy) {
        RetrievalStrategyEntity entity = toEntity(strategy);
        mapper.insertOrUpdate(entity);
        return strategy;
    }

    @Override
    public Optional<RetrievalStrategy> findById(String id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public List<RetrievalStrategy> findByWorkspace(String workspaceId) {
        LambdaQueryWrapper<RetrievalStrategyEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RetrievalStrategyEntity::getWorkspaceId, workspaceId);
        return mapper.selectList(wrapper).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(id);
    }

    private RetrievalStrategyEntity toEntity(RetrievalStrategy strategy) {
        RetrievalStrategyEntity entity = new RetrievalStrategyEntity();
        entity.setId(strategy.getId());
        entity.setTenantId(strategy.getTenantId());
        entity.setWorkspaceId(strategy.getWorkspaceId());
        entity.setName(strategy.getName());
        entity.setDescription(strategy.getDescription());
        entity.setRetrievalType(strategy.getRetrievalType().name());
        entity.setTopK(strategy.getTopK());
        entity.setScoreThreshold(strategy.getScoreThreshold());
        entity.setEnableQueryRewrite(strategy.isEnableQueryRewrite());
        entity.setEnableTextSearch(strategy.isEnableTextSearch());
        entity.setEnableVectorSearch(strategy.isEnableVectorSearch());
        entity.setKeywordWeight(strategy.getKeywordWeight());
        entity.setEnableRerank(strategy.isEnableRerank());
        entity.setRerankModel(strategy.getRerankModel());
        entity.setVectorWeight(strategy.getVectorWeight());
        entity.setKeywordWeight(strategy.getKeywordWeight());
        entity.setCreatedAt(strategy.getCreatedAt());
        entity.setUpdatedAt(strategy.getUpdatedAt());
        return entity;
    }

    private RetrievalStrategy toDomain(RetrievalStrategyEntity entity) {
        return RetrievalStrategy.rebuild(
                entity.getId(),
                entity.getWorkspaceId(),
                entity.getName(),
                entity.getDescription(),
                RetrievalStrategy.RetrievalType.valueOf(entity.getRetrievalType()),
                entity.getTopK(),
                entity.getScoreThreshold(),
                Boolean.TRUE.equals(entity.getEnableQueryRewrite()),
                Boolean.TRUE.equals(entity.getEnableTextSearch()),
                Boolean.TRUE.equals(entity.getEnableVectorSearch()),
                Boolean.TRUE.equals(entity.getEnableRerank()),
                entity.getRerankModel(),
                entity.getVectorWeight(),
                entity.getKeywordWeight(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

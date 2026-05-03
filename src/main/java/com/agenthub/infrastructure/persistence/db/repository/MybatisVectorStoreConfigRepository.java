package com.agenthub.infrastructure.persistence.db.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.agenthub.application.port.out.repositories.VectorStoreConfigRepository;
import com.agenthub.domain.model.VectorStoreConfig;
import com.agenthub.domain.model.VectorStoreType;
import com.agenthub.infrastructure.persistence.db.entity.VectorStoreConfigEntity;
import com.agenthub.infrastructure.persistence.db.mapper.VectorStoreConfigMybatisMapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 向量库配置仓储实现（MyBatis Plus）。
 * <p>
 * 负责领域对象 VectorStoreConfig 和持久化对象 VectorStoreConfigPo 之间的转换。
 * </p>
 */
@Repository
public class MybatisVectorStoreConfigRepository implements VectorStoreConfigRepository {

    private final VectorStoreConfigMybatisMapper mapper;

    public MybatisVectorStoreConfigRepository(VectorStoreConfigMybatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<VectorStoreConfig> findById(String id) {
        VectorStoreConfigEntity po = mapper.selectById(id);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public List<VectorStoreConfig> findAllByTenantId(String tenantId) {
        LambdaQueryWrapper<VectorStoreConfigEntity> query = new LambdaQueryWrapper<>();
        query.eq(VectorStoreConfigEntity::getTenantId, tenantId.toString())
                .orderByDesc(VectorStoreConfigEntity::getUpdatedAt);
        return mapper.selectList(query).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<VectorStoreConfig> findByName(String name) {
        VectorStoreConfigEntity po = mapper.selectByTenantIdAndName(name);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public VectorStoreConfig save(VectorStoreConfig config) {
        VectorStoreConfigEntity po = toPersistence(config);
        VectorStoreConfigEntity existing = mapper.selectById(po.getId());
        if (existing == null) {
            mapper.insert(po);
        } else {
            mapper.updateById(po);
        }
        return toDomain(po);
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(id);
    }

    @Override
    public Collection<VectorStoreConfig> findAll() {
        LambdaQueryWrapper<VectorStoreConfigEntity> query = new LambdaQueryWrapper<>();
        query.orderByDesc(VectorStoreConfigEntity::getUpdatedAt);
        return mapper.selectList(query).stream()
                .map(this::toDomain)
                .toList();
    }

    /**
     * 领域对象 → 持久化对象
     */
    private VectorStoreConfigEntity toPersistence(VectorStoreConfig domain) {
        VectorStoreConfigEntity po = new VectorStoreConfigEntity();
        po.setId(domain.id());
        po.setName(domain.name());
        po.setType(domain.type().name());
        po.setHost(domain.host());
        po.setPort(domain.port());
        po.setApiKey(domain.apiKey());
        po.setCollectionName(domain.collectionName());
        po.setExtraParams(domain.extraParams());
        po.setEnabled(domain.enabled());
        po.setCreatedAt(domain.createdAt());
        po.setUpdatedAt(domain.updatedAt());
        return po;
    }

    /**
     * 持久化对象 → 领域对象
     */
    private VectorStoreConfig toDomain(VectorStoreConfigEntity po) {
        return new VectorStoreConfig(
                po.getId(),
                po.getName(),
                VectorStoreType.valueOf(po.getType()),
                po.getHost(),
                po.getPort(),
                po.getApiKey(),
                po.getCollectionName(),
                po.getExtraParams(),
                po.getEnabled(),
                po.getCreatedAt(),
                po.getUpdatedAt()
        );
    }
}

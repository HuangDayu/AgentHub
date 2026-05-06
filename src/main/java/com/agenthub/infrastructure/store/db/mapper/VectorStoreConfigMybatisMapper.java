package com.agenthub.infrastructure.store.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.store.db.entity.VectorStoreConfigEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 向量库配置 MyBatis Mapper。
 */
@Mapper
public interface VectorStoreConfigMybatisMapper extends BaseMapper<VectorStoreConfigEntity> {

    /**
     * 根据租户 ID 和名称查询。
     *
     * @param tenantId 租户 ID
     * @param name     配置名称
     * @return 持久化对象
     */
    default VectorStoreConfigEntity selectByTenantIdAndName(String name) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<VectorStoreConfigEntity>()
                .eq(VectorStoreConfigEntity::getName, name));
    }
}

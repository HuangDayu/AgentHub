package com.agenthub.infrastructure.persistence.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.persistence.db.entity.TenantEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户数据映射器.
 * <p>
 * 提供对app.tenant表的CRUD操作。
 * </p>
 */
@Mapper
public interface TenantMapper extends BaseMapper<TenantEntity> {
}

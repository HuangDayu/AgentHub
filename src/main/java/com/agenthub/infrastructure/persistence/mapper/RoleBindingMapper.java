package com.agenthub.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.persistence.entity.RoleBindingEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色绑定数据映射器.
 * <p>
 * 提供对app.role_binding表的CRUD操作。
 * </p>
 */
@Mapper
public interface RoleBindingMapper extends BaseMapper<RoleBindingEntity> {
}

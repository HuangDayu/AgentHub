package com.agenthub.infrastructure.persistence.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.persistence.db.entity.AppUserEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 应用用户数据映射器.
 * <p>
 * 提供对app.app_user表的CRUD操作。
 * </p>
 */
@Mapper
public interface AppUserMapper extends BaseMapper<AppUserEntity> {
}

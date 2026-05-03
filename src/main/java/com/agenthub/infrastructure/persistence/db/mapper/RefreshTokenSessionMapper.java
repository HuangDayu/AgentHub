package com.agenthub.infrastructure.persistence.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.persistence.db.entity.RefreshTokenSessionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 刷新令牌会话数据映射器.
 * <p>
 * 提供对iam_refresh_token_session表的CRUD操作。
 * </p>
 */
@Mapper
public interface RefreshTokenSessionMapper extends BaseMapper<RefreshTokenSessionEntity> {
}

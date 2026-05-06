package com.agenthub.infrastructure.store.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.store.db.entity.SessionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会话数据映射器。
 */
@Mapper
public interface SessionMybatisMapper extends BaseMapper<SessionEntity> {
}


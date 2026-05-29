package com.agenthub.infrastructure.store.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.store.db.entity.SubsessionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 子会话数据映射器。
 */
@Mapper
public interface SubsessionMybatisMapper extends BaseMapper<SubsessionEntity> {
}

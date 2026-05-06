package com.agenthub.infrastructure.store.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.store.db.entity.SkillEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SkillMybatisMapper extends BaseMapper<SkillEntity> {
}

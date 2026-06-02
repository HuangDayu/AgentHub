package com.agenthub.infrastructure.store.db.mapper;

import com.agenthub.infrastructure.store.db.entity.SkillConfigEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

/**
 * 技能配置 Mapper。
 */
@Mapper
public interface SkillConfigMybatisMapper extends BaseMapper<SkillConfigEntity> {
}

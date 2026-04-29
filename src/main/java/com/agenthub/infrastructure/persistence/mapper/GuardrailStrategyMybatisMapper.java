package com.agenthub.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.persistence.entity.GuardrailStrategyEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GuardrailStrategyMybatisMapper extends BaseMapper<GuardrailStrategyEntity> {
}

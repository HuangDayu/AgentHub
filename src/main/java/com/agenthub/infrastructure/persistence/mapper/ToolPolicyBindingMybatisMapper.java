package com.agenthub.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.persistence.entity.ToolPolicyBindingEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ToolPolicyBindingMybatisMapper extends BaseMapper<ToolPolicyBindingEntity> {
}

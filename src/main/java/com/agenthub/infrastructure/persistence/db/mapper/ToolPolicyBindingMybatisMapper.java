package com.agenthub.infrastructure.persistence.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.persistence.db.entity.ToolPolicyBindingEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ToolPolicyBindingMybatisMapper extends BaseMapper<ToolPolicyBindingEntity> {
}

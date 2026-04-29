package com.agenthub.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.persistence.entity.SecurityPolicyEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SecurityPolicyMybatisMapper extends BaseMapper<SecurityPolicyEntity> {
}

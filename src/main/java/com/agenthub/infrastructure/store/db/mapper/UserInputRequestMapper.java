package com.agenthub.infrastructure.store.db.mapper;

import com.agenthub.infrastructure.store.db.entity.UserInputRequestEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户输入请求Mapper.
 */
@Mapper
public interface UserInputRequestMapper extends BaseMapper<UserInputRequestEntity> {
}

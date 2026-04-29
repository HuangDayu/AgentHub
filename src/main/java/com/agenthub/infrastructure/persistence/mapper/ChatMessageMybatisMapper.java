package com.agenthub.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.persistence.entity.ChatMessageEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天消息数据映射器。
 */
@Mapper
public interface ChatMessageMybatisMapper extends BaseMapper<ChatMessageEntity> {
}


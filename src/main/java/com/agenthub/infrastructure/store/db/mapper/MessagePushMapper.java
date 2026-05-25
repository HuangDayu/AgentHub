package com.agenthub.infrastructure.store.db.mapper;

import com.agenthub.infrastructure.store.db.entity.MessagePushEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息推送Mapper.
 */
@Mapper
public interface MessagePushMapper extends BaseMapper<MessagePushEntity> {
}

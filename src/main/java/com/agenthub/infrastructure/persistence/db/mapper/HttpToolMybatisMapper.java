package com.agenthub.infrastructure.persistence.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.persistence.db.entity.HttpToolsEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工具 MyBatis Mapper 接口。
 * <p>
 * 基于 MyBatis-Plus BaseMapper，提供工具实体的数据库 CRUD 操作。
 */
@Mapper
public interface HttpToolMybatisMapper extends BaseMapper<HttpToolsEntity> {
}

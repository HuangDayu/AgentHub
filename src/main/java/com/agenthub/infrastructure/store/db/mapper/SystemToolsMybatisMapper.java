package com.agenthub.infrastructure.store.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.store.db.entity.SystemToolsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SystemToolsMybatisMapper extends BaseMapper<SystemToolsEntity> {

    @Update("UPDATE app.system_tools SET enabled = #{enabled}, updated_at = NOW() WHERE id = #{id}")
    void updateEnabled(@Param("id") String id, @Param("enabled") boolean enabled);
}

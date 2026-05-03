package com.agenthub.infrastructure.persistence.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.persistence.db.entity.FunctionToolsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface FunctionToolsMybatisMapper extends BaseMapper<FunctionToolsEntity> {

    @Update("UPDATE app.function_tools SET enabled = #{enabled}, updated_at = NOW() WHERE id = #{id}")
    void updateEnabled(@Param("id") String id, @Param("enabled") boolean enabled);
}

package com.agenthub.infrastructure.store.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.store.db.entity.ModelConfigEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 模型配置 MyBatis Mapper 接口。
 */
@Mapper
public interface ModelConfigMybatisMapper extends BaseMapper<ModelConfigEntity> {

    @Select("SELECT * FROM model_config WHERE id = #{id} AND tenant_id = #{tenantId}")
    ModelConfigEntity selectByIdAndTenant(@Param("id") String id, @Param("tenantId") String tenantId);

    @Select("SELECT * FROM model_config WHERE tenant_id = #{tenantId} ORDER BY created_at DESC")
    List<ModelConfigEntity> selectByTenant(@Param("tenantId") String tenantId);

    @Select("SELECT * FROM model_config WHERE tenant_id = #{tenantId} AND type = #{type} ORDER BY created_at DESC")
    List<ModelConfigEntity> selectByTenantAndType(@Param("tenantId") String tenantId, @Param("type") String type);

    @Select("SELECT * FROM model_config WHERE type = #{type} ORDER BY created_at DESC")
    List<ModelConfigEntity> selectByType(@Param("type") String type);


    @Select("SELECT * FROM model_config WHERE tenant_id = #{tenantId} AND enabled = #{enabled} ORDER BY created_at DESC")
    List<ModelConfigEntity> selectByTenantAndEnabled(@Param("tenantId") String tenantId, @Param("enabled") Boolean enabled);

    @Delete("DELETE FROM model_config WHERE id = #{id} AND tenant_id = #{tenantId}")
    int deleteByIdAndTenant(@Param("id") String id, @Param("tenantId") String tenantId);

    @Select("SELECT * FROM model_config WHERE enabled = #{enabled} ORDER BY created_at DESC")
    List<ModelConfigEntity> selectByEnabled(@Param("enabled") Boolean enabled);
}
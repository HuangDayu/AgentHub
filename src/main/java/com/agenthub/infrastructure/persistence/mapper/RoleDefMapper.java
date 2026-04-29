package com.agenthub.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.persistence.entity.RoleDefEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 角色定义数据映射器.
 * <p>
 * 提供对app.role_def表的CRUD操作。
 * </p>
 */
@Mapper
public interface RoleDefMapper extends BaseMapper<RoleDefEntity> {
    /**
     * 根据角色代码查找角色ID。
     */
    @Select("SELECT id FROM app.role_def WHERE role_code = #{roleCode}")
    String findIdByCode(@Param("roleCode") String roleCode);
}

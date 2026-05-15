package com.agenthub.infrastructure.store.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agenthub.infrastructure.store.db.entity.PromptTemplateEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PromptTemplateMybatisMapper extends BaseMapper<PromptTemplateEntity> {
    @Select("SELECT * FROM prompt_template WHERE workspace_id = #{workspaceId} ORDER BY created_at DESC")
    List<PromptTemplateEntity> selectByWorkspaceId(@Param("workspaceId") String workspaceId);

    @Select("SELECT * FROM prompt_template WHERE workspace_id = #{workspaceId} AND category = #{category} ORDER BY created_at DESC")
    List<PromptTemplateEntity> selectByWorkspaceIdAndCategory(@Param("workspaceId") String workspaceId, @Param("category") String category);
}

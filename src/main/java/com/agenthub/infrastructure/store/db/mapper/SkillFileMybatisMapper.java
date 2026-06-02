package com.agenthub.infrastructure.store.db.mapper;

import com.agenthub.infrastructure.store.db.entity.SkillFileEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 技能文件 Mapper。
 */
@Mapper
public interface SkillFileMybatisMapper extends BaseMapper<SkillFileEntity> {



    /**
     * 根据扩展名查找。
     */
    @Select("SELECT * FROM skill_file WHERE skill_id = #{skillId} AND file_ext = #{ext}")
    List<SkillFileEntity> selectBySkillIdAndExt(@Param("skillId") String skillId,
                                                 @Param("ext") String ext);

    /**
     * 获取文件统计。
     */
    @Select("SELECT COUNT(*) as file_count, COALESCE(SUM(file_size), 0) as total_size " +
            "FROM skill_file WHERE skill_id = #{skillId} AND is_directory = false")
    Object[] selectStats(@Param("skillId") String skillId);
}

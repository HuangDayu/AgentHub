package com.agenthub.domain.model;

import cn.hutool.core.util.ReflectUtil;
import com.agenthub.domain.annotation.AgentDataModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 数据模型元数据，描述一个可被 Agent 操作的实体。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataModelMetadata {

    /**
     * 模型名称（Agent 看到的名称）
     */
    private String name;

    /**
     * 模型描述
     */
    private String description;

    /**
     * 所属领域
     */
    private String domain;

    /**
     * 图标名称
     */
    private String icon;

    /**
     * 实体类名
     */
    private String entityClassName;

    /**
     * Mapper 类名
     */
    private String mapperClassName;

    /**
     * 是否支持创建
     */
    private boolean creatable;

    /**
     * 是否支持更新
     */
    private boolean updatable;

    /**
     * 是否支持删除
     */
    private boolean deletable;

    private boolean hasTenantField;

    /**
     * 租户字段名
     */
    private String tenantField;

    private boolean hasWorkspaceField;

    /**
     * 工作空间字段名
     */
    private String workspaceField;

    /**
     * 字段元数据列表
     */
    private List<DataFieldMetadata> fields;

    /**
     * 从注解和类创建元数据
     */
    public static DataModelMetadata fromAnnotation(AgentDataModel annotation,
                                                   Class<?> clazz,
                                                   List<DataFieldMetadata> fields) {
        DataModelMetadataBuilder b = DataModelMetadata.builder();
        applyAnnotation(b, annotation, clazz);
        return b.fields(fields).build();
    }

    /**
     * 应用注解值到构建器
     */
    private static void applyAnnotation(DataModelMetadataBuilder b,
                                        AgentDataModel annotation,
                                        Class<?> clazz) {
        applyBasicInfo(b, annotation, clazz);
        applyFlags(b, clazz,annotation);
    }

    /**
     * 应用基本信息
     */
    private static void applyBasicInfo(DataModelMetadataBuilder b,
                                       AgentDataModel annotation,
                                       Class<?> clazz) {
        b.name(annotation.name()).description(annotation.description());
        b.domain(annotation.domain()).icon(annotation.icon());
        b.entityClassName(clazz.getName());
        b.mapperClassName(annotation.mapper().getName());
    }

    /**
     * 应用标志位
     */
    private static void applyFlags(DataModelMetadataBuilder b, Class<?> clazz, AgentDataModel annotation) {
        b.creatable(annotation.creatable()).updatable(annotation.updatable());
        b.deletable(annotation.deletable());
        b.tenantField(annotation.tenantField());
        b.workspaceField(annotation.workspaceField());
        b.hasTenantField(ReflectUtil.hasField(clazz, annotation.tenantField()));
        b.hasWorkspaceField(ReflectUtil.hasField(clazz, annotation.workspaceField()));
    }
}

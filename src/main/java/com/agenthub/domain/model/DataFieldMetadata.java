package com.agenthub.domain.model;

import com.agenthub.domain.annotation.AgentDataField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 字段元数据，描述数据模型中一个字段的 Agent 可见属性。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataFieldMetadata {

    /**
     * 字段名称
     */
    private String name;

    /**
     * 字段描述
     */
    private String description;

    /**
     * 字段类型（简单类名）
     */
    private String type;

    /**
     * 是否在列表中显示
     */
    private boolean listVisible;

    /**
     * 是否在详情中显示
     */
    private boolean detailVisible;

    /**
     * 是否可作为过滤条件
     */
    private boolean filterable;

    /**
     * 是否可排序
     */
    private boolean sortable;

    /**
     * 创建时是否必填
     */
    private boolean required;

    /**
     * 是否从 Agent 视图中隐藏
     */
    private boolean hidden;

    /**
     * 枚举值列表，如 ["ACTIVE", "INACTIVE", "DISABLED"]
     */
    private List<String> enumValues;

    /**
     * 是否敏感字段（读取时需脱敏）
     */
    private boolean sensitive;

    /**
     * 从字段和注解创建字段元数据
     */
    public static DataFieldMetadata fromField(Field field, AgentDataField annotation) {
        DataFieldMetadataBuilder b = DataFieldMetadata.builder();
        applyFieldAndAnnotation(b, field, annotation);
        return b.build();
    }

    /**
     * 应用字段和注解值到构建器
     */
    private static void applyFieldAndAnnotation(DataFieldMetadataBuilder b,
                                                Field field,
                                                AgentDataField annotation) {
        b.name(field.getName());
        b.description(resolveFieldDescription(field, annotation));
        b.type(field.getType().getSimpleName());
        applyVisibility(b, annotation);
        applyExtras(b, annotation);
    }

    /**
     * 应用可见性属性
     */
    private static void applyVisibility(DataFieldMetadataBuilder b, AgentDataField annotation) {
        b.listVisible(isListVisible(annotation));
        b.detailVisible(isDetailVisible(annotation));
        b.filterable(isFilterable(annotation));
        b.sortable(isSortable(annotation));
        b.required(isRequired(annotation));
        b.hidden(isHidden(annotation));
    }

    /**
     * 应用额外属性（枚举值、脱敏）
     */
    private static void applyExtras(DataFieldMetadataBuilder b, AgentDataField annotation) {
        b.enumValues(resolveEnumValues(annotation));
        b.sensitive(isSensitive(annotation));
    }

    /**
     * 解析枚举值，优先从 enumType 提取，其次使用 enumValues
     */
    private static List<String> resolveEnumValues(AgentDataField annotation) {
        if (annotation == null) return new ArrayList<>();
        if (isEnumTypeSet(annotation)) return extractEnumNames(annotation.enumType());
        if (annotation.enumValues().length > 0) return Arrays.asList(annotation.enumValues());
        return new ArrayList<>();
    }

    /**
     * 检查 enumType 是否已设置
     */
    private static boolean isEnumTypeSet(AgentDataField annotation) {
        return annotation != null && annotation.enumType() != Enum.class;
    }

    /**
     * 从枚举类提取所有常量名
     */
    @SuppressWarnings("unchecked")
    private static List<String> extractEnumNames(Class<? extends Enum> enumType) {
        List<String> names = new ArrayList<>();
        for (Enum<?> constant : enumType.getEnumConstants()) {
            names.add(constant.name());
        }
        return names;
    }

    private static String resolveFieldDescription(Field field, AgentDataField annotation) {
        if (annotation != null && !annotation.description().isEmpty()) {
            return annotation.description();
        }
        return field.getName();
    }

    private static boolean isListVisible(AgentDataField annotation) {
        return annotation != null && annotation.listVisible();
    }

    private static boolean isDetailVisible(AgentDataField annotation) {
        return annotation != null && annotation.detailVisible();
    }

    private static boolean isFilterable(AgentDataField annotation) {
        return annotation != null && annotation.filterable();
    }

    private static boolean isSortable(AgentDataField annotation) {
        return annotation != null && annotation.sortable();
    }

    private static boolean isRequired(AgentDataField annotation) {
        return annotation != null && annotation.required();
    }

    private static boolean isHidden(AgentDataField annotation) {
        return annotation != null && annotation.hidden();
    }

    private static boolean isSensitive(AgentDataField annotation) {
        return annotation != null && annotation.sensitive();
    }
}

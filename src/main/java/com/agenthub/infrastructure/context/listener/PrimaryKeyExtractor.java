package com.agenthub.infrastructure.context.listener;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.SqlCommandType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 主键提取器
 */
@Slf4j
class PrimaryKeyExtractor {

    List<String> extract(Object parameter, Class<?> entityClass, SqlCommandType commandType) {
        TableInfo tableInfo = getTableInfo(entityClass);
        if (tableInfo == null) {
            return new ArrayList<>();
        }

        String keyProperty = tableInfo.getKeyProperty();
        if (isEmpty(keyProperty)) {
            return new ArrayList<>();
        }

        return extractFromParameter(parameter, keyProperty, commandType);
    }

    private TableInfo getTableInfo(Class<?> entityClass) {
        return TableInfoHelper.getTableInfo(entityClass);
    }

    private boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    private List<String> extractFromParameter(Object parameter, String keyProperty, SqlCommandType commandType) {
        if (parameter instanceof Map) {
            return extractFromMap((Map<?, ?>) parameter, keyProperty);
        }
        return extractFromObject(parameter, keyProperty);
    }

    private List<String> extractFromMap(Map<?, ?> paramMap, String keyProperty) {
        Object entity = paramMap.get("et");
        if (entity != null) {
            return extractSingleKey(entity, keyProperty);
        }

        Object wrapper = paramMap.get("ew");
        if (wrapper != null) {
            return extractIdsFromWrapper(wrapper);
        }

        return new ArrayList<>();
    }

    private List<String> extractFromObject(Object parameter, String keyProperty) {
        if (parameter == null) {
            return new ArrayList<>();
        }
        return extractSingleKey(parameter, keyProperty);
    }

    private List<String> extractSingleKey(Object entity, String keyProperty) {
        List<String> keys = new ArrayList<>();
        Object pkValue = getFieldValue(entity, keyProperty);
        if (pkValue != null) {
            keys.add(pkValue.toString());
        }
        return keys;
    }

    private Object getFieldValue(Object obj, String fieldName) {
        try {
            Field field = findField(obj.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                return field.get(obj);
            }
        } catch (Exception e) {
            log.warn("获取字段值失败: {}", fieldName);
        }
        return null;
    }

    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null && clazz != Object.class) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    private List<String> extractIdsFromWrapper(Object wrapper) {
        String sqlSegment = wrapper.toString();
        if (!containsInClause(sqlSegment)) {
            return new ArrayList<>();
        }

        return parseIdsFromSql(sqlSegment);
    }

    private boolean containsInClause(String sql) {
        return sql.contains(" IN (");
    }

    private List<String> parseIdsFromSql(String sql) {
        String[] parts = sql.split(" IN \\(");
        if (parts.length < 2) {
            return new ArrayList<>();
        }

        String idsPart = extractIdsPart(parts[1]);
        return splitAndCleanIds(idsPart);
    }

    private String extractIdsPart(String remaining) {
        return remaining.split("\\)")[0];
    }

    private List<String> splitAndCleanIds(String idsPart) {
        return Arrays.stream(idsPart.split(","))
            .map(String::trim)
            .map(this::removeQuotes)
            .collect(Collectors.toList());
    }

    private String removeQuotes(String id) {
        return id.replace("'", "").replace("\"", "");
    }
}

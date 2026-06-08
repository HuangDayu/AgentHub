package com.agenthub.test.schema;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * 扫描 entity 包，产出方言无关的 TableDefinition 列表。
 */
public final class EntitySchemaScanner {

    private static final String ENTITY_PACKAGE = "com.agenthub.infrastructure.store.db.entity";
    private static final Reflections REFLECTIONS = new Reflections(ENTITY_PACKAGE);

    private EntitySchemaScanner() {
    }

    /**
     * 扫描并返回所有 Entity 类的表定义（按表名字母排序）
     */
    public static List<TableDefinition> scan() {
        Set<Class<?>> entities = REFLECTIONS.getTypesAnnotatedWith(TableName.class);
        List<TableDefinition> tables = new ArrayList<>();
        for (Class<?> entity : entities) {
            TableDefinition def = parseEntity(entity);
            if (def != null) {
                tables.add(def);
            }
        }
        tables.sort(Comparator.comparing(t -> t.tableName));
        return tables;
    }

    private static TableDefinition parseEntity(Class<?> entity) {
        TableName ann = entity.getAnnotation(TableName.class);
        if (ann == null) {
            return null;
        }
        TableDefinition def = new TableDefinition();
        def.tableName = ann.value();
        for (Field field : entity.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            ColumnDefinition col = parseColumn(field);
            if (col != null) {
                def.columns.add(col);
            }
        }
        return def;
    }

    private static ColumnDefinition parseColumn(Field field) {
        TableField tf = field.getAnnotation(TableField.class);
        if (tf != null && !tf.exist()) {
            return null;
        }
        ColumnDefinition col = new ColumnDefinition();
        col.fieldName = field.getName();
        col.javaType = field.getType();
        col.columnName = (tf != null && !tf.value().isEmpty()) ? tf.value() : camelToSnake(field.getName());
        TableId tid = field.getAnnotation(TableId.class);
        col.isPrimaryKey = tid != null;
        col.isAutoIncrement = tid != null && tid.type() == IdType.AUTO;
        col.nullable = !col.isPrimaryKey;
        return col;
    }

    private static String camelToSnake(String name) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (i > 0 && Character.isUpperCase(c)) {
                sb.append('_').append(Character.toLowerCase(c));
            } else {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    /**
     * 表定义
     */
    public static class TableDefinition {
        public String tableName;
        public final List<ColumnDefinition> columns = new ArrayList<>();
    }

    /**
     * 列定义
     */
    public static class ColumnDefinition {
        public String fieldName;
        public Class<?> javaType;
        public String columnName;
        public boolean isPrimaryKey;
        public boolean isAutoIncrement;
        public boolean nullable;
    }
}

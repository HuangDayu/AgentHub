package com.agenthub.test.architecture;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 豁免方法的内存索引键。
 * <p>
 * 匹配规则：{@code className} 与 {@code methodName} 必须完全一致；
 * {@code parameterTypes} 任一方为空时视为匹配该类的所有重载方法，
 * 均非空时按参数类型列表完全相等匹配。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
class MethodKey {

    private String className;

    private String methodName;

    private List<String> parameterTypes;

    public List<String> getParameterTypes() {
        return parameterTypes == null ? Collections.emptyList() : parameterTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MethodKey other)) {
            return false;
        }
        if (!Objects.equals(className, other.className)) {
            return false;
        }
        if (!Objects.equals(methodName, other.methodName)) {
            return false;
        }
        List<String> selfTypes = this.getParameterTypes();
        List<String> otherTypes = other.getParameterTypes();
        if (selfTypes.isEmpty() || otherTypes.isEmpty()) {
            return true;
        }
        return Objects.equals(selfTypes, otherTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, methodName);
    }
}

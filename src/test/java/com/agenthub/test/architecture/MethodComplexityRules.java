package com.agenthub.test.architecture;

import com.tngtech.archunit.core.domain.JavaMethod;

import java.util.regex.Pattern;

/**
 * 业务方法复杂度的统一阈值与排除规则。
 * <p>
 * 供 {@link AgentHubCleanArchitectureTest}（架构测试）和
 * {@link MethodViolationDumper}（违规扫描器）共享。
 */
final class MethodComplexityRules {

    static final int MAX_METHOD_LINES = 10;
    static final int MAX_METHOD_PARAMS = 3;

    /**
     * 排除的方法名模式：转换方法、builder、toString/equals/hashCode、canEqual。
     * <p>
     * 注意：getter/setter/getXxx/isXxx 不在此排除范围，
     * 因为 Lombok 生成的 getter/setter 已通过 isLombokGenerated() 排除，
     * 而手动编写的 getXxx 业务方法（如 getWeatherDescription）也应被行数/参数规则检查。
     * 简单的 1 行 getter/setter 自然满足 ≤10 行和 ≤3 参数的限制，不会被误报。
     */
    private static final Pattern EXCLUDED_NAME = Pattern.compile(
            "^(" +
                    "toString|equals|hashCode|canEqual|" +
                    "clone|finalize" +
                    ")$"
    );

    private MethodComplexityRules() {
    }

    /**
     * 判断方法是否应被行数/参数检查跳过。
     * <p>
     * 排除：转换方法、getter/setter、Lombok 生成方法、枚举自带方法、{@code $} 开头的方法（合成）。
     */
    static boolean shouldSkip(JavaMethod method) {
        if (EXCLUDED_NAME.matcher(method.getName()).matches()) {
            return true;
        }
        if (method.getName().startsWith("$")) {
            return true;
        }
        if (isLombokGenerated(method)) {
            return true;
        }
        if (isEnumMethod(method)) {
            return true;
        }
        return false;
    }

    private static boolean isLombokGenerated(JavaMethod method) {
        return method.getAnnotations().stream()
                .anyMatch(a -> a.getRawType().getName().startsWith("lombok.Generated"));
    }

    private static boolean isEnumMethod(JavaMethod method) {
        if (!method.getOwner().isEnum()) {
            return false;
        }
        String name = method.getName();
        return "values".equals(name) || "valueOf".equals(name);
    }
}

package com.agenthub.test.architecture;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 架构违规豁免条目。
 * <p>
 * 唯一标识由 {@code className} + {@code methodName} 决定，
 * {@code parameterTypes} 为可选字段：留空表示豁免该类所有同名方法，
 * 填写时按参数类型完全匹配以区分重载。
 * <p>
 * {@code severity} 字段记录违规等级：HEAVY / MEDIUM / LIGHT，
 * 便于后续按优先级清理。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
class ExemptedMethod {

    private String className;

    private String methodName;

    private List<String> parameterTypes = new ArrayList<>();

    private String reason;

    private String severity;
}

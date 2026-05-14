package com.agenthub.domain.exception;

/**
 * 工具未找到异常。
 * <p>
 * 当尝试访问不存在的工具时抛出此异常。
 */
public class ToolNotFoundException extends RuntimeException {

    /**
     * 构造工具未找到异常。
     *
     * @param toolId 工具标识
     */
    public ToolNotFoundException(String toolId) {
        super("tool not found: " + toolId);
    }
}

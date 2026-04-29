package com.agenthub.common.exception;

/** 路由未找到异常，当无法匹配到路由策略时抛出。 */
public class RouteNotFoundException extends RuntimeException {

    /**
     * 构造路由未找到异常。
     *
     * @param message 异常消息
     */
    public RouteNotFoundException(String message) {
        super(message);
    }
}

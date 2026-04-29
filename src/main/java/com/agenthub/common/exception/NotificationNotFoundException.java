package com.agenthub.common.exception;

/** 通知未找到异常，当请求的通知不存在时抛出。 */
public class NotificationNotFoundException extends RuntimeException {

    /**
     * 构造通知未找到异常。
     *
     * @param id 通知标识
     */
    public NotificationNotFoundException(String id) {
        super("Notification not found: " + id);
    }
}

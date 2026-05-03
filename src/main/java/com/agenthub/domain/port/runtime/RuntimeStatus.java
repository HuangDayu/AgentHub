package com.agenthub.domain.port.runtime;

/**
 * 运行时状态枚举。
 */
public enum RuntimeStatus {
    /**
     * 已初始化，未启动。
     */
    INITIALIZED,
    
    /**
     * 运行中。
     */
    RUNNING,
    
    /**
     * 已暂停。
     */
    PAUSED,
    
    /**
     * 已停止。
     */
    STOPPED,
    
    /**
     * 错误状态。
     */
    ERROR,
    
    /**
     * 已销毁。
     */
    DESTROYED
}

package com.agenthub.infrastructure.skills;

/**
 * 技能管理异常。
 * 
 * @author huangdayu
 */
public class SkillManagementException extends RuntimeException {
    
    public SkillManagementException(String message) {
        super(message);
    }
    
    public SkillManagementException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.agenthub.infrastructure.tools.skills_tools;

import lombok.Data;

import java.nio.file.Path;
import java.time.Instant;

/**
 * 技能包，表示文件系统中的一个技能。
 * 
 * @author huangdayu
 */
@Data
public class SkillPackage {
    
    /**
     * 技能清单
     */
    private SkillManifest manifest;
    
    /**
     * 技能包路径
     */
    private Path path;
    
    /**
     * 技能包名称（目录名）
     */
    private String name;
    
    /**
     * 安装状态
     */
    private InstallStatus status;
    
    /**
     * 安装时间
     */
    private Instant installedAt;
    
    /**
     * 校验状态
     */
    private ValidationStatus validationStatus;
    
    /**
     * 校验消息
     */
    private String validationMessage;
    
    /**
     * 安装状态枚举
     */
    public enum InstallStatus {
        /**
         * 已安装
         */
        INSTALLED,
        
        /**
         * 未安装
         */
        NOT_INSTALLED,
        
        /**
         * 安装中
         */
        INSTALLING,
        
        /**
         * 安装失败
         */
        INSTALL_FAILED
    }
    
    /**
     * 校验状态枚举
     */
    public enum ValidationStatus {
        /**
         * 有效
         */
        VALID,
        
        /**
         * 无效
         */
        INVALID,
        
        /**
         * 未校验
         */
        NOT_VALIDATED
    }
    
    /**
     * 创建技能包实例
     */
    public static SkillPackage of(Path path, SkillManifest manifest) {
        SkillPackage skillPackage = new SkillPackage();
        skillPackage.setPath(path);
        skillPackage.setManifest(manifest);
        skillPackage.setName(path.getFileName().toString());
        skillPackage.setStatus(InstallStatus.NOT_INSTALLED);
        skillPackage.setValidationStatus(ValidationStatus.NOT_VALIDATED);
        return skillPackage;
    }
    
    /**
     * 检查技能包是否已安装
     */
    public boolean isInstalled() {
        return status == InstallStatus.INSTALLED;
    }
    
    /**
     * 检查技能包是否有效
     */
    public boolean isValid() {
        return validationStatus == ValidationStatus.VALID;
    }
}

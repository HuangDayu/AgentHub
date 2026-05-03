package com.agenthub.application.port.out;

/**
 * Runtime工厂端口接口。
 * <p>
 * 用于创建Agent和AgentTeam的运行时实例。
 * </p>
 */
public interface RuntimeFactory {
    
    /**
     * 创建Agent运行时。
     *
     * @param frameworkType 框架类型
     * @return Agent运行时实例
     */
    AgentRuntime createAgentRuntime(String frameworkType);
    
    /**
     * 创建Agent团队运行时。
     *
     * @param frameworkType 框架类型
     * @return Agent团队运行时实例
     */
    AgentTeamRuntime createAgentTeamRuntime(String frameworkType);
    
    /**
     * 检查框架类型是否支持。
     *
     * @param frameworkType 框架类型
     * @return 是否支持
     */
    boolean isSupported(String frameworkType);
    
    /**
     * 获取支持的框架类型列表。
     *
     * @return 框架类型数组
     */
    String[] getSupportedFrameworks();
}

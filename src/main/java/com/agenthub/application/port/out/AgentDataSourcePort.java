package com.agenthub.application.port.out;

import com.agenthub.domain.model.AgentDataSource;
import com.agenthub.domain.model.AgentDataSourceDescriptor;
import com.agenthub.domain.model.DataSourceSchema;

import java.util.List;
import java.util.Map;

/**
 * Agent 数据源运行时操作端口 - 由基础设施 Camel 实现
 */
public interface AgentDataSourcePort {
    /**
     * 启动数据源（Camel 路由注册）
     */
    void bootstrap(AgentDataSource source);

    /**
     * 关闭数据源
     */
    void shutdown(String dataSourceId);

    /**
     * 测试连接
     */
    AgentDataSourceTestResult test(AgentDataSource source);

    /**
     * 调用数据源
     */
    AgentDataSourceInvokeResult invoke(AgentDataSource source, Map<String, Object> headers, String body);

    /**
     * 列出所有支持的协议描述符
     */
    List<AgentDataSourceDescriptor> listDescriptors();

    /**
     * 内省数据源 schema（Camel 适配器仅 JDBC/SQL 协议支持）
     */
    DataSourceSchema introspect(AgentDataSource source);

    class AgentDataSourceTestResult {
        private boolean success;
        private long elapsedMs;
        private String message;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public long getElapsedMs() { return elapsedMs; }
        public void setElapsedMs(long elapsedMs) { this.elapsedMs = elapsedMs; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    class AgentDataSourceInvokeResult {
        private boolean success;
        private Object data;
        private long elapsedMs;
        private String exchangeId;
        private String errorMessage;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
        public long getElapsedMs() { return elapsedMs; }
        public void setElapsedMs(long elapsedMs) { this.elapsedMs = elapsedMs; }
        public String getExchangeId() { return exchangeId; }
        public void setExchangeId(String exchangeId) { this.exchangeId = exchangeId; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}

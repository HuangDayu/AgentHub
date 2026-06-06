package com.agenthub.domain.enums;

import lombok.Getter;

/**
 * Agent 数据源协议枚举
 * <p>覆盖 Apache Camel 4.x 支持的核心协议。</p>
 */
@Getter
public enum AgentDataSourceProtocol {
    JDBC("JDBC", "jdbc"),
    JMS("JMS", "jms"),
    KAFKA("Kafka", "kafka"),
    HTTP("HTTP", "http"),
    HTTPS("HTTPS", "https"),
    FTP("FTP", "ftp"),
    SFTP("SFTP", "sftp"),
    FILE("File", "file"),
    MAIL("Mail", "smtp"),
    MONGODB("MongoDB", "mongodb"),
    REDIS("Redis", "redis"),
    SQL("SQL", "sql"),
    REST("REST", "rest"),
    DIRECT("Direct", "direct"),
    TIMER("Timer", "timer");

    private final String displayName;
    private final String camelScheme;

    AgentDataSourceProtocol(String displayName, String camelScheme) {
        this.displayName = displayName;
        this.camelScheme = camelScheme;
    }
}

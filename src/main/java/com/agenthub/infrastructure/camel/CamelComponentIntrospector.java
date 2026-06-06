package com.agenthub.infrastructure.camel;

import com.agenthub.domain.model.AgentDataSourceDescriptor;
import com.agenthub.domain.model.AgentDataSourceField;
import com.agenthub.domain.enums.AgentDataSourceProtocol;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Camel Component 描述符反射器
 * <p>列出所有支持的协议 + 字段模板。</p>
 */
@Component
public class CamelComponentIntrospector {

    /**
     * 列出所有支持的协议描述符
     */
    public List<AgentDataSourceDescriptor> listDescriptors() {
        List<AgentDataSourceDescriptor> list = new ArrayList<>();
        for (AgentDataSourceProtocol proto : AgentDataSourceProtocol.values()) {
            list.add(buildDescriptor(proto));
        }
        return list;
    }

    private AgentDataSourceDescriptor buildDescriptor(AgentDataSourceProtocol proto) {
        AgentDataSourceDescriptor d = new AgentDataSourceDescriptor();
        d.setProtocol(proto.name());
        d.setScheme(proto.getCamelScheme());
        d.setDisplayName(proto.getDisplayName());
        d.setDescription(proto.getDisplayName() + " data source");
        d.setSyntaxHint(buildSyntaxHint(proto));
        d.setFields(buildFields(proto));
        return d;
    }

    private String buildSyntaxHint(AgentDataSourceProtocol proto) {
        return switch (proto) {
            case JDBC -> "jdbc:postgresql://host:port/db?user=xxx&password=xxx";
            case HTTP -> "http://host:port/path";
            case HTTPS -> "https://host:port/path";
            case KAFKA -> "kafka:topic?brokers=host:9092";
            case FTP -> "ftp://host/path?username=xxx&password=xxx";
            case SFTP -> "sftp://host/path?username=xxx&password=xxx";
            case FILE -> "file:/path?noop=true";
            case MAIL -> "smtp://host:port?username=xxx&password=xxx";
            case MONGODB -> "mongodb:host:27017/database?collection=xxx";
            case REDIS -> "redis://host:6379";
            case SQL -> "sql:SELECT * FROM table?dataSource=#mySql";
            case REST -> "rest:http://host:port/path?method=GET";
            case JMS -> "jms:queue:myQueue";
            case DIRECT -> "direct:endpointName";
            case TIMER -> "timer:tick?period=1000";
        };
    }

    private List<AgentDataSourceField> buildFields(AgentDataSourceProtocol proto) {
        return switch (proto) {
            case JDBC -> Arrays.asList(
                setField("host", "string", true, "localhost", "Database host"),
                setField("port", "integer", true, "5432", "Database port"),
                setField("database", "string", true, null, "Database name"),
                setField("username", "string", true, null, "Username"),
                setField("password", "password", true, null, "Password")
            );
            case HTTP, HTTPS, REST -> Arrays.asList(
                setField("host", "string", true, "localhost", "HTTP host"),
                setField("port", "integer", true, "8080", "HTTP port"),
                setField("path", "string", false, "/", "URL path"),
                setField("method", "string", false, "GET", "HTTP method")
            );
            case KAFKA -> Arrays.asList(
                setField("brokers", "string", true, "localhost:9092", "Bootstrap servers"),
                setField("topic", "string", true, null, "Topic name")
            );
            default -> new ArrayList<>();
        };
    }

    private AgentDataSourceField setField(String name, String type, boolean required,
                                          String defaultValue, String description) {
        AgentDataSourceField f = new AgentDataSourceField();
        f.setName(name);
        f.setType(type);
        f.setRequired(required);
        f.setDefaultValue(defaultValue);
        f.setDescription(description);
        return f;
    }
}

package com.agenthub.infrastructure.camel;

import com.agenthub.domain.model.AgentDataSourceDescriptor;
import com.agenthub.domain.model.AgentDataSourceField;
import com.agenthub.domain.enums.AgentDataSourceProtocol;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static com.agenthub.domain.enums.AgentDataSourceProtocol.*;

/**
 * Camel Component 描述符反射器
 * <p>列出所有支持的协议 + 字段模板。</p>
 */
@Component
public class CamelComponentIntrospector {

    private static final Map<AgentDataSourceProtocol, String> HINTS = Map.ofEntries(
        Map.entry(JDBC, "jdbc:postgresql://host:port/db?user=xxx&password=xxx"),
        Map.entry(HTTP, "http://host:port/path"),
        Map.entry(HTTPS, "https://host:port/path"),
        Map.entry(KAFKA, "kafka:topic?brokers=host:9092"),
        Map.entry(FTP, "ftp://host/path?username=xxx&password=xxx"),
        Map.entry(SFTP, "sftp://host/path?username=xxx&password=xxx"),
        Map.entry(FILE, "file:/path?noop=true"),
        Map.entry(MAIL, "smtp://host:port?username=xxx&password=xxx"),
        Map.entry(MONGODB, "mongodb:host:27017/database?collection=xxx"),
        Map.entry(REDIS, "redis://host:6379"),
        Map.entry(SQL, "sql:SELECT * FROM table?dataSource=#mySql"),
        Map.entry(REST, "rest:http://host:port/path?method=GET"),
        Map.entry(JMS, "jms:queue:myQueue"),
        Map.entry(DIRECT, "direct:endpointName"),
        Map.entry(TIMER, "timer:tick?period=1000")
    );

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
        return HINTS.getOrDefault(proto, "");
    }

    private List<AgentDataSourceField> buildFields(AgentDataSourceProtocol proto) {
        return switch (proto) {
            case JDBC -> jdbcFields();
            case HTTP, HTTPS, REST -> httpFields();
            case KAFKA -> kafkaFields();
            default -> new ArrayList<>();
        };
    }

    private List<AgentDataSourceField> jdbcFields() {
        return Arrays.asList(
            setField(new FieldDef("host", "string", true, "localhost", "Database host")),
            setField(new FieldDef("port", "integer", true, "5432", "Database port")),
            setField(new FieldDef("database", "string", true, null, "Database name")),
            setField(new FieldDef("username", "string", true, null, "Username")),
            setField(new FieldDef("password", "password", true, null, "Password"))
        );
    }

    private List<AgentDataSourceField> httpFields() {
        return Arrays.asList(
            setField(new FieldDef("host", "string", true, "localhost", "HTTP host")),
            setField(new FieldDef("port", "integer", true, "8080", "HTTP port")),
            setField(new FieldDef("path", "string", false, "/", "URL path")),
            setField(new FieldDef("method", "string", false, "GET", "HTTP method"))
        );
    }

    private List<AgentDataSourceField> kafkaFields() {
        return Arrays.asList(
            setField(new FieldDef("brokers", "string", true, "localhost:9092", "Bootstrap servers")),
            setField(new FieldDef("topic", "string", true, null, "Topic name"))
        );
    }

    private AgentDataSourceField setField(FieldDef def) {
        AgentDataSourceField f = new AgentDataSourceField();
        f.setName(def.getName());
        f.setType(def.getType());
        f.setRequired(def.isRequired());
        f.setDefaultValue(def.getDefaultValue());
        f.setDescription(def.getDescription());
        return f;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class FieldDef {
        private String name;
        private String type;
        private boolean required;
        private String defaultValue;
        private String description;
    }
}

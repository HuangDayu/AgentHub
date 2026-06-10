package com.agenthub.infrastructure.tools.data_source.standard;

import com.agenthub.domain.enums.AgentDataSourceProtocol;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.agenthub.infrastructure.tools.data_source.standard.ProtocolStandard.ProtocolParam;
import static java.util.List.of;

@Component
public class ProtocolStandardRegistry {

    private final Map<AgentDataSourceProtocol, ProtocolStandard> standards = new LinkedHashMap<>();

    public ProtocolStandard get(AgentDataSourceProtocol protocol) {
        return standards.get(protocol);
    }

    @PostConstruct
    public void init() {
        for (var s : List.of(Jdbc(), Sql(), Http(), Https(), Rest(), MongoDb(), Redis(), Kafka(),
            Ftp(), Sftp(), File(), Mail(), Jms(), Direct(), Timer())) {
            standards.put(s.getProtocol(), s);
        }
    }

    private static ProtocolStandard Jdbc() {
        return new ProtocolStandard(AgentDataSourceProtocol.JDBC, "JDBC", DESC_JDBC, SYNTAX_JDBC, OPS_JDBC, EX_JDBC, ERR_JDBC, SEC_JDBC, BEST_JDBC, of(new ProtocolParam("sql", "string", true, "SQL SELECT 查询语句")));
    }

    private static ProtocolStandard Sql() {
        return new ProtocolStandard(AgentDataSourceProtocol.SQL, "SQL", DESC_SQL, SYNTAX_SQL, OPS_SQL, EX_SQL, ERR_SQL, SEC_SQL, BEST_SQL, of(new ProtocolParam("sql", "string", true, "SQL 查询语句，支持 #{name} 参数占位符")));
    }

    private static ProtocolStandard Http() {
        return new ProtocolStandard(AgentDataSourceProtocol.HTTP, "HTTP", DESC_HTTP, SYNTAX_HTTP, OPS_HTTP, EX_HTTP, ERR_HTTP, SEC_HTTP, BEST_HTTP, of(new ProtocolParam("method", "string", true, "HTTP 方法: GET/POST/PUT/DELETE/PATCH"), new ProtocolParam("path", "string", true, "请求路径"), new ProtocolParam("body", "string", false, "请求体 JSON（POST/PUT 时必填）"), new ProtocolParam("queryParams", "string", false, "查询参数"), new ProtocolParam("headers", "string", false, "请求头 JSON")));
    }

    private static ProtocolStandard Https() {
        return new ProtocolStandard(AgentDataSourceProtocol.HTTPS, "HTTPS", DESC_HTTPS, SYNTAX_HTTPS, OPS_HTTPS, EX_HTTPS, ERR_HTTPS, SEC_HTTPS, BEST_HTTPS, of(new ProtocolParam("method", "string", true, "HTTP 方法: GET/POST/PUT/DELETE/PATCH"), new ProtocolParam("path", "string", true, "请求路径"), new ProtocolParam("body", "string", false, "请求体 JSON"), new ProtocolParam("queryParams", "string", false, "查询参数"), new ProtocolParam("headers", "string", false, "请求头 JSON")));
    }

    private static ProtocolStandard Rest() {
        return new ProtocolStandard(AgentDataSourceProtocol.REST, "REST", DESC_REST, SYNTAX_REST, OPS_REST, EX_REST, ERR_REST, SEC_REST, BEST_REST, of(new ProtocolParam("method", "string", true, "HTTP 方法: GET/POST/PUT/DELETE/PATCH"), new ProtocolParam("path", "string", true, "资源路径"), new ProtocolParam("body", "string", false, "请求体 JSON"), new ProtocolParam("queryParams", "string", false, "查询参数"), new ProtocolParam("headers", "string", false, "请求头 JSON")));
    }

    private static ProtocolStandard MongoDb() {
        return new ProtocolStandard(AgentDataSourceProtocol.MONGODB, "MongoDB", DESC_MONGO, SYNTAX_MONGO, OPS_MONGO, EX_MONGO, ERR_MONGO, SEC_MONGO, BEST_MONGO, of(new ProtocolParam("collection", "string", true, "集合名称"), new ProtocolParam("operation", "string", true, "操作: find/findOne/insertOne/updateOne/deleteOne/aggregate"), new ProtocolParam("query", "string", false, "查询条件 JSON（find/update/delete 时必填）"), new ProtocolParam("document", "string", false, "文档内容 JSON（insertOne 时必填）")));
    }

    private static ProtocolStandard Redis() {
        return new ProtocolStandard(AgentDataSourceProtocol.REDIS, "Redis", DESC_REDIS, SYNTAX_REDIS, OPS_REDIS, EX_REDIS, ERR_REDIS, SEC_REDIS, BEST_REDIS, of(new ProtocolParam("command", "string", true, "Redis 命令: GET/SET/DEL/HSET/..."), new ProtocolParam("key", "string", true, "键名"), new ProtocolParam("value", "string", false, "值（SET/HSET 时必填）"), new ProtocolParam("args", "string", false, "额外参数 JSON 数组，如 [0, -1]")));
    }

    private static ProtocolStandard Kafka() {
        return new ProtocolStandard(AgentDataSourceProtocol.KAFKA, "Kafka", DESC_KAFKA, SYNTAX_KAFKA, OPS_KAFKA, EX_KAFKA, ERR_KAFKA, SEC_KAFKA, BEST_KAFKA, of(new ProtocolParam("message", "string", true, "消息内容，建议 JSON 格式"), new ProtocolParam("topic", "string", false, "目标 topic 名称"), new ProtocolParam("key", "string", false, "消息键，用于分区路由"), new ProtocolParam("headers", "string", false, "消息头 JSON")));
    }

    private static ProtocolStandard Ftp() {
        return new ProtocolStandard(AgentDataSourceProtocol.FTP, "FTP", DESC_FTP, SYNTAX_FTP, OPS_FTP, EX_FTP, ERR_FTP, SEC_FTP, BEST_FTP, of(new ProtocolParam("operation", "string", true, "操作: read/write/list/delete"), new ProtocolParam("path", "string", true, "远程文件路径"), new ProtocolParam("content", "string", false, "文件内容（write 时必填）")));
    }

    private static ProtocolStandard Sftp() {
        return new ProtocolStandard(AgentDataSourceProtocol.SFTP, "SFTP", DESC_SFTP, SYNTAX_SFTP, OPS_SFTP, EX_SFTP, ERR_SFTP, SEC_SFTP, BEST_SFTP, of(new ProtocolParam("operation", "string", true, "操作: read/write/list/delete"), new ProtocolParam("path", "string", true, "远程文件路径"), new ProtocolParam("content", "string", false, "文件内容（write 时必填）")));
    }

    private static ProtocolStandard File() {
        return new ProtocolStandard(AgentDataSourceProtocol.FILE, "File", DESC_FILE, SYNTAX_FILE, OPS_FILE, EX_FILE, ERR_FILE, SEC_FILE, BEST_FILE, of(new ProtocolParam("operation", "string", true, "操作: read/write/list/delete/exists"), new ProtocolParam("path", "string", true, "文件路径"), new ProtocolParam("content", "string", false, "文件内容（write 时必填）")));
    }

    private static ProtocolStandard Mail() {
        return new ProtocolStandard(AgentDataSourceProtocol.MAIL, "Mail", DESC_MAIL, SYNTAX_MAIL, OPS_MAIL, EX_MAIL, ERR_MAIL, SEC_MAIL, BEST_MAIL, of(new ProtocolParam("operation", "string", true, "操作: send"), new ProtocolParam("to", "string", true, "收件人邮箱，多个用逗号分隔"), new ProtocolParam("cc", "string", false, "抄送邮箱"), new ProtocolParam("bcc", "string", false, "密送邮箱"), new ProtocolParam("subject", "string", true, "邮件主题"), new ProtocolParam("body", "string", true, "邮件正文"), new ProtocolParam("contentType", "string", false, "内容类型: text/plain 或 text/html")));
    }

    private static ProtocolStandard Jms() {
        return new ProtocolStandard(AgentDataSourceProtocol.JMS, "JMS", DESC_JMS, SYNTAX_JMS, OPS_JMS, EX_JMS, ERR_JMS, SEC_JMS, BEST_JMS, of(new ProtocolParam("operation", "string", true, "操作: send"), new ProtocolParam("destination", "string", true, "队列名称或 topic://主题"), new ProtocolParam("message", "string", true, "消息内容，建议 JSON 格式"), new ProtocolParam("headers", "string", false, "消息属性 JSON")));
    }

    private static ProtocolStandard Direct() {
        return new ProtocolStandard(AgentDataSourceProtocol.DIRECT, "Direct", DESC_DIRECT, SYNTAX_DIRECT, OPS_DIRECT, EX_DIRECT, ERR_DIRECT, SEC_DIRECT, BEST_DIRECT, of(new ProtocolParam("body", "string", true, "消息体内容")));
    }

    private static ProtocolStandard Timer() {
        return new ProtocolStandard(AgentDataSourceProtocol.TIMER, "Timer", DESC_TIMER, SYNTAX_TIMER, OPS_TIMER, EX_TIMER, ERR_TIMER, SEC_TIMER, BEST_TIMER, of(new ProtocolParam("body", "string", false, "触发消息")));
    }

    // ---- text constants (field lines don't count toward method line limit) ----

    private static final String DESC_JDBC = "关系型数据库查询，通过 JDBC 协议执行 SQL 查询。支持 PostgreSQL、MySQL、Oracle 等主流数据库。";
    private static final String SYNTAX_JDBC = "使用标准 SQL SELECT 语法。仅支持只读查询。\n基本语法: SELECT [columns] FROM [table] [WHERE conditions] [GROUP BY columns] [ORDER BY columns] [LIMIT n]\n表名和列名可以使用双引号引用保留字。\nJOIN 语法: SELECT * FROM t1 JOIN t2 ON t1.id = t2.fk_id\n聚合函数: COUNT(*), SUM(col), AVG(col), MIN(col), MAX(col)\n分页: 使用 LIMIT offset, count 或 LIMIT count OFFSET offset";
    private static final String OPS_JDBC = "数据查询: SELECT 语句获取数据\n数据量评估: 推荐使用 COUNT(*) 评估数据量，避免大结果集\n分批查询: 大结果集建议使用 LIMIT/OFFSET 分批\n说明: 仅支持只读 SELECT，不支持 INSERT/UPDATE/DELETE/DDL";
    private static final String EX_JDBC = "查询用户表: sql=SELECT id, name, email FROM users WHERE status = 'active' LIMIT 10\n多表 JOIN: sql=SELECT o.id, u.name FROM orders o JOIN users u ON o.user_id = u.id\n聚合统计: sql=SELECT department, COUNT(*) cnt, AVG(salary) avg_sal FROM employees GROUP BY department";
    private static final String ERR_JDBC = "语法错误: 检查 SQL 语法和表名列名是否存在\n表不存在: 先通过 describeDataSourceSchema 查看可用表\n超时: 复杂查询可能超时，尝试简化或分批\n空结果: 查询返回空结果集属于正常情况";
    private static final String SEC_JDBC = "仅执行只读 SELECT 查询，禁止任何写操作\n使用参数化查询的字段值不要拼接 SQL 片段\n敏感数据不应在查询结果中明文返回";
    private static final String BEST_JDBC = "查询前先用 describeDataSourceSchema 了解表结构\n使用 WHERE 条件过滤，避免全表扫描\n使用 LIMIT 控制返回行数，默认建议加 LIMIT 100";

    private static final String DESC_SQL = "通过 Camel SQL 组件执行命名 SQL 查询，支持参数化占位符 #{name} 和更灵活的数据源路由。";
    private static final String SYNTAX_SQL = "使用标准 SELECT 语法，支持 #{name} 参数占位符。\n基本语法: SELECT [columns] FROM [table] [WHERE col = #{param}]\n参数占位符使用 #{paramName} 语法，自动防注入。\n支持存储过程调用: CALL procedure_name(#{param})";
    private static final String OPS_SQL = "命名查询: 使用 #{param} 占位符的参数化查询\n存储过程: 调用数据库存储过程\n仅支持只读操作";
    private static final String EX_SQL = "参数化查询: sql=SELECT * FROM users WHERE status = #{status} AND age > #{minAge}\n存储过程: sql=CALL get_user_stats(#{deptId})";
    private static final String ERR_SQL = "占位符语法错误: 确保 #{name} 格式正确\n存储过程不存在: 检查数据库存储过程名称";
    private static final String SEC_SQL = "参数占位符自动防注入，但不要拼接外部输入到 SQL 文本中\n仅允许只读 SELECT 和存储过程调用";
    private static final String BEST_SQL = "优先使用 SQL 协议的参数化查询而非 JDBC 协议以获得防注入保护\n复杂查询建议先用 explain 分析执行计划";

    private static final String DESC_HTTP = "通过 HTTP 协议调用 RESTful API，支持 GET/POST/PUT/DELETE/PATCH 等标准方法。";
    private static final String SYNTAX_HTTP = "RESTful API 调用遵循 HTTP/1.1 规范。\nGET: 获取资源，参数通过 queryParams 传递\nPOST: 创建资源，请求体放在 body 字段\nPUT: 更新资源（全量替换）\nPATCH: 部分更新资源\nDELETE: 删除资源\n状态码: 2xx 成功, 4xx 客户端错误, 5xx 服务端错误\nContent-Type: POST/PUT 建议 application/json";
    private static final String OPS_HTTP = "RESTful CRUD: GET/POST/PUT/DELETE/PATCH\n文件上传: multipart/form-data（通过 body 传入）\n认证: Bearer Token 在 headers 中传入";
    private static final String EX_HTTP = "GET 请求: method=GET, path=/api/v1/users, queryParams=page=1&size=20\nPOST 创建: method=POST, path=/api/v1/users, body={\"name\":\"张三\"}\n带认证: method=GET, path=/api/v1/orders, headers={\"Authorization\":\"Bearer eyJ...\"}";
    private static final String ERR_HTTP = "404 未找到: path 路径可能错误，检查 API 端点\n401/403 未授权: 需要添加认证 headers\n4xx 客户端错误: 检查请求参数格式\n5xx 服务端错误: 外部服务异常，稍后重试\n超时: 网络问题或服务响应慢";
    private static final String SEC_HTTP = "不要在 URL 中明文传递敏感信息（Token、密码等）\n使用 headers 传递认证凭据而非 queryParams\n验证外部 API 的 SSL 证书有效性";
    private static final String BEST_HTTP = "先了解目标 API 的文档再构造请求\n错误响应中通常包含 errorMessage 字段\n大批量数据建议使用分页参数";

    private static final String DESC_HTTPS = "通过 HTTPS 协议安全调用 RESTful API，使用 TLS/SSL 加密传输。适用于生产环境 API 调用。";
    private static final String SYNTAX_HTTPS = "同 HTTP 协议规范，但增加 TLS 加密。\n证书验证: 默认验证服务端 SSL 证书有效性\n所有数据传输均加密，适合敏感信息传输";
    private static final String OPS_HTTPS = "同 HTTP 协议的 CRUD 操作\n推荐用于所有生产环境 API 调用";
    private static final String EX_HTTPS = "GET 请求: method=GET, path=/api/v1/orders\nPOST 创建: method=POST, path=/api/v1/payments, body={\"amount\":100}";
    private static final String ERR_HTTPS = "SSL 证书错误: 检查证书是否过期或自签名\nTLS 握手失败: 检查 TLS 版本兼容性";
    private static final String SEC_HTTPS = "始终验证服务端证书，不要禁用证书检查\n使用 HTTPS 而非 HTTP 传输敏感数据";
    private static final String BEST_HTTPS = "生产环境优先使用 HTTPS";

    private static final String DESC_REST = "通过 Camel REST 组件调用 RESTful 服务，支持 URI 模板和内容协商。";
    private static final String SYNTAX_REST = "RESTful 架构风格，资源导向的 API 设计。\nURI 模板: /users/{id} 自动替换路径变量\n内容协商: Accept/Content-Type 头部指定数据格式\n标准方法: GET /users, GET /users/{id}, POST /users, PUT /users/{id}, DELETE /users/{id}";
    private static final String OPS_REST = "资源 CRUD: 通过标准 HTTP 方法操作资源\nURI 模板: 使用 {param} 占位符的动态路径\n内容协商: 通过 Accept 头指定返回格式";
    private static final String EX_REST = "查询资源: method=GET, path=/api/users, queryParams=role=admin\n路径参数: method=GET, path=/api/users/42";
    private static final String ERR_REST = "同 HTTP 协议的错误处理\nURI 模板匹配失败: 检查路径参数格式";
    private static final String SEC_REST = "遵循 RESTful 资源命名规范\n避免在路径中暴露数据库主键";
    private static final String BEST_REST = "使用复数名词命名资源路径\n利用 HTTP 状态码判断操作结果";

    private static final String DESC_MONGO = "通过 MongoDB 协议操作文档型 NoSQL 数据库。支持文档的 CRUD 和聚合管道操作。";
    private static final String SYNTAX_MONGO = "MongoDB 使用 JSON/BSON 文档模型。\n查询语法: {\"field\": value} 精确匹配, {\"field\": {\"$gt\": value}} 范围查询\n常用运算符:\n  比较: $eq, $ne, $gt, $gte, $lt, $lte, $in, $nin\n  逻辑: $and, $or, $not, $nor\n  数组: $all, $elemMatch, $size\n  正则: $regex\n聚合管道: $match, $group, $sort, $project, $limit, $lookup(JOIN)";
    private static final String OPS_MONGO = "数据查询: find/findOne\n数据写入: insertOne/insertMany\n数据更新: updateOne/updateMany/replaceOne\n数据删除: deleteOne/deleteMany\n聚合分析: aggregate";
    private static final String EX_MONGO = "等值查询: collection=users, operation=find, query={\"status\":\"active\"}\n范围查询: collection=orders, operation=find, query={\"amount\":{\"$gt\":100}}\n聚合统计: collection=sales, operation=aggregate, query=[{\"$group\":{...}}]";
    private static final String ERR_MONGO = "集合不存在: 检查集合名称\n查询语法错误: 检查 JSON 格式和运算符名称\nObjectId 格式: 使用 {\"_id\": {\"$oid\": \"...\"}} 格式";
    private static final String SEC_MONGO = "避免在查询条件中注入 JavaScript 表达式\n敏感字段应考虑在投影中排除";
    private static final String BEST_MONGO = "查询条件使用双引号包裹字段名\n聚合管道建议从 $match 开始以利用索引\nlimit 操作应配合 sort 保证结果顺序";

    private static final String DESC_REDIS = "通过 Redis 协议操作内存键值数据库。支持字符串、哈希、列表、集合、有序集合等数据结构。";
    private static final String SYNTAX_REDIS = "Redis 命令遵循 Redis 官方命令规范。\n字符串: GET key, SET key value, DEL key, EXISTS key, TTL key, INCR key\n哈希: HGET key field, HSET key field value, HGETALL key\n列表: LPUSH key value, RPUSH key value, LPOP key, RPOP key, LRANGE key start stop\n集合: SADD key member, SMEMBERS key, SISMEMBER key member\n有序集合: ZADD key score member, ZRANGE key start stop, ZRANK key member\n键管理: KEYS pattern, SCAN cursor, TYPE key, EXPIRE key seconds, DEL key";
    private static final String OPS_REDIS = "数据读写: GET/SET/HGET/HSET 等\n批量操作: MGET/MSET/HMGET/HMSET\n原子操作: INCR/DECR 计数器\n过期管理: EXPIRE/TTL\n事务: MULTI/EXEC\n注意: KEYS 命令在生产环境慎用，建议使用 SCAN";
    private static final String EX_REDIS = "字符串: command=SET, key=user:42, value={\"name\":\"张三\"}\n哈希: command=HGETALL, key=user:42\n列表: command=LRANGE, key=logs, args=[0, -1]";
    private static final String ERR_REDIS = "不支持的命令: Redis 协议可能限制了部分管理命令\n连接超时: 检查 Redis 服务是否可达\n数据类型不匹配: 如对列表使用 GET 命令";
    private static final String SEC_REDIS = "不要在键名中包含敏感信息\n注意键过期时间，避免内存泄漏\n生产环境避免使用 FLUSHALL/KEYS 等命令";
    private static final String BEST_REDIS = "键命名规范: 使用冒号分隔命名空间，如 user:42:profile\n使用 TTL 设置合理的过期时间\n大数据集使用 SCAN 而非 KEYS";

    private static final String DESC_KAFKA = "通过 Kafka 协议发送消息到消息队列。支持 topic 路由、消息键和消息头。适用于事件驱动架构和异步消息处理。";
    private static final String SYNTAX_KAFKA = "Kafka 消息发送遵循 Kafka 协议规范。\n消息结构: 消息内容 + 可选的键 + 可选的头部\nTopic: 消息分类标识，消费者按 topic 订阅\n消息键: 用于分区路由，相同键的消息发送到同一分区\n消息头: 元数据键值对，用于消息路由和过滤\n至少一次语义: 生产者默认保证消息不丢失\n消息顺序: 同一分区内消息有序";
    private static final String OPS_KAFKA = "消息发送: 发送消息到指定 topic\n批量发送: 多次调用发送多条消息\n说明: 当前支持消息发送，暂不支持消息消费";
    private static final String EX_KAFKA = "简单消息: message=订单已创建, topic=order-events\n带键消息: message={\"orderId\":1024}, topic=order-events, key=order-1024\n带头消息: headers={\"eventType\":\"payment\"}";
    private static final String ERR_KAFKA = "Topic 不存在: 可能需要自动创建 topic 权限\n消息过大: 超过 max.message.bytes 配置\nBroker 不可达: 检查 Kafka 集群连接配置\n认证失败: 检查 SASL/SSL 配置";
    private static final String SEC_KAFKA = "不要在消息内容中明文传输密码或密钥\n敏感数据应加密后再发送\n消息内容建议使用 JSON 格式";
    private static final String BEST_KAFKA = "消息建议使用结构化的 JSON 格式便于消费方解析\n为消息设置有意义的键以保持相关消息的顺序\n使用 headers 传递元数据而非嵌入消息体";

    private static final String DESC_FTP = "通过 FTP 协议操作远程文件系统。支持文件上传、下载、删除、列表等操作。适用于传统文件传输场景。";
    private static final String SYNTAX_FTP = "FTP 协议用于远程文件传输。\n路径: 使用 Unix 风格路径分隔符\n认证: 用户名和密码认证\n模式: 支持主动/被动模式\n不加密传输，适合非敏感文件\n目录不存在时会自动创建";
    private static final String OPS_FTP = "文件下载: read 操作下载文件内容\n文件上传: write 操作上传文件\n文件列表: list 操作列出目录\n文件删除: delete 操作删除文件";
    private static final String EX_FTP = "下载文件: operation=read, path=/data/report.csv\n上传文件: operation=write, path=/data/output.json, content=文件内容\n列出目录: operation=list, path=/data/";
    private static final String ERR_FTP = "连接失败: 检查主机地址和端口\n认证失败: 检查用户名密码\n文件不存在: read/delete 前先 list 确认\n权限错误: 检查目录写入权限";
    private static final String SEC_FTP = "FTP 不加密，请勿传输敏感数据\n敏感文件建议使用 SFTP";
    private static final String BEST_FTP = "上传前先确认目录存在\n大文件注意传输时间和超时设置";

    private static final String DESC_SFTP = "通过 SFTP 协议安全操作远程文件系统。基于 SSH 加密传输，适合敏感文件的传输和管理。";
    private static final String SYNTAX_SFTP = "SFTP 基于 SSH 协议，所有数据传输加密。\n路径: 使用 Unix 风格路径分隔符\n认证: SSH 密钥或密码认证\n端口: 默认 22\n加密传输，适合敏感和机密文件";
    private static final String OPS_SFTP = "文件下载: read 操作下载文件内容\n文件上传: write 操作上传文件\n文件列表: list 操作列出目录\n文件删除: delete 操作删除文件";
    private static final String EX_SFTP = "下载文件: operation=read, path=/data/finance/report.xlsx\n上传文件: operation=write, path=/data/backup/data.json\n列出目录: operation=list, path=/data/";
    private static final String ERR_SFTP = "连接失败: 检查主机和 SSH 配置\n主机密钥验证失败: 检查 known_hosts\n认证失败: 检查 SSH 密钥或密码";
    private static final String SEC_SFTP = "SFTP 加密传输，适合敏感文件\n使用 SSH 密钥认证比密码更安全";
    private static final String BEST_SFTP = "优先使用 SFTP 而非 FTP\n大文件注意传输时间和磁盘空间";

    private static final String DESC_FILE = "操作本地文件系统。支持文件的读、写、列表、删除等基本操作。";
    private static final String SYNTAX_FILE = "本地文件系统操作。\nWindows 路径: C:/path/to/file.txt\nLinux 路径: /path/to/file.txt\n当前工作目录: 相对于应用启动目录";
    private static final String OPS_FILE = "文件读取: read\n文件写入: write\n目录列表: list\n文件删除: delete\n文件检查: exists";
    private static final String EX_FILE = "读取文件: operation=read, path=/data/config.json\n写入文件: operation=write, path=/data/output.txt, content=hello world\n列出目录: operation=list, path=/data/";
    private static final String ERR_FILE = "文件不存在: 检查路径是否正确\n权限错误: 检查应用对目录的读写权限\n路径遍历: 路径不应包含 .. 序列";
    private static final String SEC_FILE = "避免路径遍历攻击，不要接受外部传入的路径\n敏感文件不应写入公共可读目录";
    private static final String BEST_FILE = "文件操作后确认结果\n大文件注意内存使用";

    private static final String DESC_MAIL = "通过 SMTP 协议发送电子邮件。支持发送文本和 HTML 格式邮件，支持附件和收件人管理。";
    private static final String SYNTAX_MAIL = "SMTP 邮件发送规范。\n收件人: To 字段（多个收件人用逗号分隔）\n抄送: CC 字段\n密送: BCC 字段\n正文: 支持纯文本和 HTML 格式";
    private static final String OPS_MAIL = "发送邮件: 发送文本或 HTML 格式邮件\n批量发送: 同一邮件发送给多个收件人（使用 BCC）\n带附件: 支持添加文件附件";
    private static final String EX_MAIL = "文本邮件: operation=send, to=user@example.com, subject=周报, body=本周完成了...\nHTML 邮件: contentType=text/html\n多收件人: to=a@ex.com,b@ex.com, cc=manager@ex.com";
    private static final String ERR_MAIL = "SMTP 服务器连接失败: 检查邮件服务器配置\n收件人地址错误: 验证邮箱格式\n附件过大: 检查附件大小限制\n被拒收: 可能被目标邮件服务器判定为垃圾邮件";
    private static final String SEC_MAIL = "不要在邮件正文中包含密码等敏感信息\n使用 BCC 保护收件人隐私（批量发送时）";
    private static final String BEST_MAIL = "发送重要邮件后确认发送成功\n使用有意义的主题行\n正式邮件建议使用 HTML 格式";

    private static final String DESC_JMS = "通过 JMS 协议发送和接收消息。支持队列（点对点）和主题（发布订阅）两种消息模型。";
    private static final String SYNTAX_JMS = "JMS 消息规范。\n队列 (Queue): 点对点模型，一条消息被一个消费者处理\n主题 (Topic): 发布订阅模型，一条消息被所有订阅者接收\n消息类型: TextMessage（文本）, MapMessage（键值对）\n消息属性: 自定义属性用于消息过滤\n持久化: 设置 deliveryMode 为持久化确保消息不丢失";
    private static final String OPS_JMS = "消息发送: 发送消息到队列或主题\n消息类型: 推荐使用 TextMessage（JSON 格式）\n属性设置: 通过 headers 传递消息属性用于过滤";
    private static final String EX_JMS = "发送到队列: operation=send, destination=order.queue, message={\"orderId\":1024}\n发送到主题: operation=send, destination=topic://order.events\n带属性: headers={\"priority\":\"high\"}";
    private static final String ERR_JMS = "连接失败: 检查 JMS broker 连接配置\n目的地不存在: 可能需要自动创建权限\n事务回滚: 检查消息是否被成功确认";
    private static final String SEC_JMS = "不要在消息中明文传输密码\n使用消息属性而非消息体进行路由\n敏感消息使用加密传输";
    private static final String BEST_JMS = "消息内容使用 JSON 格式便于跨系统解析\n使用有意义的队列/主题命名规范\n关键消息启用持久化确保不丢失";

    private static final String DESC_DIRECT = "Camel 同步直连端点，用于在同一 Camel 上下文中的路由之间进行同步调用。适用于内部处理流程编排。";
    private static final String SYNTAX_DIRECT = "Direct 是 Camel 内部同步调用机制。\n同步: 调用方等待被调用方处理完成\n同上下文: 仅在同一 Camel 上下文中可用\n点对点: 一个 direct 端点只能被一个消费者消费\n无需网络: 纯内存调用，性能高";
    private static final String OPS_DIRECT = "调用内部路由: 触发同一上下文中其他路由\n数据传递: 通过消息体传递数据";
    private static final String EX_DIRECT = "调用 direct 端点: body={\"action\":\"process\"}";
    private static final String ERR_DIRECT = "端点不存在: 检查路由中是否定义了该 direct 端点\n多个消费者: direct 端点只能有一个消费者";
    private static final String SEC_DIRECT = "Direct 端点内部使用，不暴露给外部\n注意避免循环调用";
    private static final String BEST_DIRECT = "用于内部路由编排，不跨进程使用";

    private static final String DESC_TIMER = "Camel 定时器端点，用于定时触发任务。适用于定时轮询、定时数据处理、定时通知等场景。";
    private static final String SYNTAX_TIMER = "Timer 支持定时/周期性任务触发。\nfixedRate: 固定频率触发\ncron 表达式: 支持标准 cron 语法\n延迟启动: initialDelay 参数控制首次触发延迟";
    private static final String OPS_TIMER = "定时器状态: 获取当前定时器配置信息\n手动触发: 触发定时器执行";
    private static final String EX_TIMER = "触发定时器: body=trigger";
    private static final String ERR_TIMER = "定时器未启动: 检查路由配置\n周期不准确: 检查处理时间是否超过周期";
    private static final String SEC_TIMER = "Timer 仅用于内部定时任务\n处理时间不应超过触发周期";
    private static final String BEST_TIMER = "合理设置周期避免任务堆积";
}

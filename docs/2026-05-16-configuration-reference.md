# 配置参考

> 本文档提供 AgentHub 所有配置属性的完整参考。

---

## 目录

- [服务器配置](#1-服务器配置)
- [Spring 核心配置](#2-spring-核心配置)
- [数据库配置](#3-数据库配置)
- [Redis 配置](#4-redis-配置)
- [AI 模型配置](#5-ai-模型配置)
- [向量存储配置](#6-向量存储配置)
- [JWT 认证配置](#7-jwt-认证配置)
- [对象存储配置](#8-对象存储配置)
- [RAG 配置](#9-rag-配置)
- [ETL 配置](#10-etl-配置)
- [MyBatis-Plus 配置](#11-mybatis-plus-配置)
- [Actuator 监控配置](#12-actuator-监控配置)
- [日志配置](#13-日志配置)
- [完整配置示例](#14-完整配置示例)

---

## 1. 服务器配置

```yaml
server:
  port: 8080                # 服务端口，默认 8080
```

---

## 2. Spring 核心配置

```yaml
spring:
  main:
    allow-bean-definition-overriding: true   # 允许 Bean 覆盖
    allow-circular-references: true          # 允许循环引用（开发环境）
  sql:
    init:
      mode: always                          # 数据库初始化模式
      schema-locations: file:${user.dir}\sql\schema.sql  # Schema 脚本路径
      data-locations: file:${user.dir}\sql\init.sql      # 初始化数据脚本路径
      platform: postgresql                  # 数据库平台
  servlet:
    multipart:
      max-file-size: 50MB                   # 最大上传文件大小
      max-request-size: 50MB                # 最大请求大小
      file-size-threshold: 2MB              # 文件大小阈值
  http:
    clients:
      connect-timeout: 120000               # 连接超时（毫秒）
      read-timeout: 600000                  # 读取超时（毫秒）
```

### 2.1 配置说明

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `spring.main.allow-bean-definition-overriding` | false | 是否允许 Bean 定义覆盖 |
| `spring.main.allow-circular-references` | false | 是否允许循环引用 |
| `spring.sql.init.mode` | embedded | 数据库初始化模式（always/embedded/never） |
| `spring.servlet.multipart.max-file-size` | 1MB | 单个文件最大上传大小 |
| `spring.servlet.multipart.max-request-size` | 10MB | 单次请求最大大小 |

---

## 3. 数据库配置

### 3.1 PostgreSQL 数据源

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/agenthub?schema=app  # 数据库连接 URL
    username: things                                            # 用户名
    password: things123                                         # 密码
    driver-class-name: org.postgresql.Driver                    # 驱动类
    hikari:
      schema: app                                              # Hikari 连接池 Schema
```

### 3.2 JPA 配置

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update                     # DDL 策略（update/create/create-drop/none）
    show-sql: false                        # 是否显示 SQL
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect  # 数据库方言
```

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `spring.jpa.hibernate.ddl-auto` | none | 自动 DDL 策略 |
| `spring.jpa.show-sql` | false | 是否打印 SQL 语句 |

---

## 4. Redis 配置

```yaml
spring:
  data:
    redis:
      host: localhost                      # Redis 主机地址
      port: 6379                           # Redis 端口
      # password:                          # Redis 密码（如有）
      # database: 0                       # 数据库索引
      # timeout: 3000ms                   # 连接超时
```

---

## 5. AI 模型配置

### 5.1 OpenAI 配置

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}           # API 密钥（建议使用环境变量）
      base-url: https://api.openai.com     # API 基础地址
      embedding:
        base-url: http://127.0.0.1:9090    # Embedding 服务地址
        options:
          model: qwen3-embedding           # Embedding 模型
      chat:
        options:
          model: gpt-4                     # 对话模型
```

### 5.2 模型选择

```yaml
spring:
  ai:
    model:
      embedding: openai                    # 默认 Embedding 模型供应商
      chat: openai                         # 默认对话模型供应商
```

### 5.3 重试配置

```yaml
spring:
  ai:
    retry:
      backoff:
        initial-interval: 20000ms          # 重试初始间隔
```

### 5.4 聊天记忆配置

```yaml
spring:
  ai:
    chat:
      memory:
        repository:
          jdbc:
            initialize-schema: always      # 记忆表自动初始化
```

---

## 6. 向量存储配置

### 6.1 Qdrant

```yaml
spring:
  ai:
    vectorstore:
      qdrant:
        host: localhost                    # Qdrant 主机
        port: 6334                         # gRPC 端口
        collection-name: document_chunks   # 集合名称
        initialize-schema: true            # 自动初始化 Schema
```

---

## 7. JWT 认证配置

```yaml
agenthub:
  jwt:
    secret: your-jwt-secret-key-here       # JWT 签名密钥（生产环境请更换为强密钥）
    expiration: 3600000                    # Token 过期时间（毫秒），默认 1 小时
```

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `agenthub.jwt.secret` | - | JWT 签名密钥，生产环境必须更换 |
| `agenthub.jwt.expiration` | 3600000 | Token 过期时间 |

---

## 8. 对象存储配置

### 8.1 MinIO 配置

```yaml
agenthub:
  storage:
    provider: minio                        # 存储供应商
    minio:
      endpoint: http://localhost:9000      # MinIO 服务端点
      access-key: minioadmin               # 访问密钥
      secret-key: minioadmin               # 秘密密钥
      bucket: agenthub                    # 默认 Bucket 名称
```

---

## 9. RAG 配置

```yaml
agenthub:
  rag:
    type: customize                   # RAG 实现类型（customize/spring）
    reranker:
      type: keyword                        # 重排器类型（keyword/cross-encoder）
```

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `agenthub.rag.type` | customize | RAG 管道实现类型 |
| `agenthub.rag.reranker.type` | keyword | 重排器类型 |

---

## 10. ETL 配置

```yaml
agenthub:
  etl:
    type: customize                   # ETL 实现类型（customize/spring）
```

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `agenthub.etl.type` | customize | ETL 管道实现类型 |

---

## 11. MyBatis-Plus 配置

```yaml
mybatis-plus:
  type-handlers-package: com.agenthub.infrastructure.store.db.handler  # 类型处理器包
  configuration:
    map-underscore-to-camel-case: true                                 # 下划线转驼峰
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl               # 日志实现
  global-config:
    db-config:
      logic-delete-field: deleted                                      # 逻辑删除字段
      logic-delete-value: 1                                            # 逻辑已删除值
      logic-not-delete-value: 0                                        # 逻辑未删除值
```

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `mybatis-plus.configuration.map-underscore-to-camel-case` | true | 数据库下划线映射 Java 驼峰 |
| `mybatis-plus.global-config.db-config.logic-delete-field` | deleted | 逻辑删除字段名 |

---

## 12. Actuator 监控配置

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics   # 暴露的端点
```

| 端点 | 说明 | 默认暴露 |
|------|------|---------|
| `/actuator/health` | 健康检查 | ✅ |
| `/actuator/info` | 应用信息 | ✅ |
| `/actuator/metrics` | 性能指标 | ✅ |

---

## 13. 日志配置

```yaml
logging:
  level:
    com.agenthub: DEBUG                  # AgentHub 日志级别
    org.springframework.security: WARN    # Spring Security 日志级别
```

推荐日志级别：

| 环境 | 日志级别 | 说明 |
|------|---------|------|
| 开发 | DEBUG | 输出详细调试信息 |
| 测试 | INFO | 输出一般信息 |
| 生产 | WARN | 仅输出警告和错误 |

---

## 14. 完整配置示例

以下是 `src/main/resources/application.yml` 的完整示例：

```yaml
server:
  port: 8080

spring:
  main:
    allow-bean-definition-overriding: true
    allow-circular-references: true
  sql:
    init:
      mode: always
      schema-locations: file:${user.dir}\sql\schema.sql
      data-locations: file:${user.dir}\sql\init.sql
      platform: postgresql
  datasource:
    url: jdbc:postgresql://localhost:5432/agenthub?schema=app
    username: things
    password: things123
    driver-class-name: org.postgresql.Driver
    hikari:
      schema: app
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB
      file-size-threshold: 2MB
  data:
    redis:
      host: localhost
      port: 6379
  kafka:
    bootstrap-servers: localhost:9092
  mail:
    host: localhost
    port: 25
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: https://api.openai.com
      embedding:
        base-url: http://127.0.0.1:9090
        options:
          model: qwen3-embedding
      chat:
        options:
          model: gpt-4
    vectorstore:
      qdrant:
        host: localhost
        port: 6334
        collection-name: document_chunks
        initialize-schema: true
    model:
      embedding: openai
      chat: openai
    retry:
      backoff:
        initial-interval: 20000ms
    chat:
      memory:
        repository:
          jdbc:
            initialize-schema: always
  http:
    clients:
      connect-timeout: 120000
      read-timeout: 600000

mybatis-plus:
  type-handlers-package: com.agenthub.infrastructure.store.db.handler
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

agenthub:
  jwt:
    secret: monolith-dev-secret-key-please-change-in-production-2026
    expiration: 3600000
  storage:
    provider: minio
    minio:
      endpoint: http://localhost:9000
      access-key: minioadmin
      secret-key: minioadmin
      bucket: agenthub
  rag:
    type: customize
    reranker:
      type: keyword
  etl:
    type: customize

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics

logging:
  level:
    com.agenthub: DEBUG
    org.springframework.security: WARN
```

### 生产环境建议调整项

| 配置项 | 开发环境 | 生产环境 |
|--------|---------|---------|
| `spring.jpa.hibernate.ddl-auto` | update | none（手动管理） |
| `spring.sql.init.mode` | always | never（手动初始化） |
| `logging.level.com.agenthub` | DEBUG | WARN |
| `agenthub.jwt.secret` | 默认密钥 | 强随机密钥 |
| `agenthub.jwt.expiration` | 3600000 | 根据安全策略调整 |
| `spring.main.allow-circular-references` | true | false（消除循环引用） |

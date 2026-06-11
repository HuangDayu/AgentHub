# 部署指南

> 本文档提供 AgentHub 的详细环境配置和部署步骤。

---

## 目录

- [环境要求](#1-环境要求)
- [基础设施部署](#2-基础设施部署)
- [应用配置](#3-应用配置)
- [构建与运行](#4-构建与运行)
- [Docker 部署](#5-docker-部署)
- [多环境配置](#6-多环境配置)
- [生产环境最佳实践](#7-生产环境最佳实践)
- [运维指南](#8-运维指南)

---

## 1. 环境要求

### 硬件要求

| 环境 | CPU | 内存 | 磁盘 | 说明 |
|------|-----|------|------|------|
| 开发环境 | 4 核+ | 8GB+ | 20GB+ | 可运行在单机上 |
| 测试环境 | 4 核+ | 16GB+ | 50GB+ | 建议独立服务器 |
| 生产环境 | 8 核+ | 32GB+ | 100GB+ | 建议集群部署 |

### 软件依赖

| 组件 | 版本要求 | 必须 | 说明 |
|------|---------|------|------|
| JDK | 21+ | ✅ | 运行环境 |
| PostgreSQL | 14+ | ✅ | 主数据库 |
| Redis | 7+ | ✅ | 缓存和会话 |
| Qdrant | 1.7+ | ❌ | 向量数据库（选择一个即可） |
| MinIO | 最新 | ❌ | 对象存储 |
| Kafka | 最新 | ❌ | 消息队列 |
| Elasticsearch | 8.x | ❌ | 全文检索引擎 |
| Nginx | 最新 | ❌ | 反向代理（生产建议） |

### 向量数据库选择

可以选择以下任意一种向量数据库：

| 向量数据库 | 推荐场景 | 说明 |
|-----------|---------|------|
| Qdrant | 通用场景 | Rust 实现，性能优异，推荐首选 |
| Milvus | 大规模场景 | 分布式，支持百亿级向量 |
| Weaviate | 小型项目 | Go 实现，部署简单 |
| Chroma | 开发测试 | 轻量级，适合本地开发 |
| PGVector | 简单场景 | PostgreSQL 插件，无需额外服务 |

---

## 2. 基础设施部署

### 2.1 PostgreSQL

```bash
# Windows (使用 Docker)
docker run -d \
  --name postgres \
  -e POSTGRES_USER=things \
  -e POSTGRES_PASSWORD=things123 \
  -e POSTGRES_DB=agenthub \
  -p 5432:5432 \
  postgres:16

# 创建 schema
docker exec -it postgres psql -U things -d agenthub -c "CREATE SCHEMA IF NOT EXISTS app;"
```

### 2.2 Redis

```bash
# Windows (使用 Docker)
docker run -d \
  --name redis \
  -p 6379:6379 \
  redis:7
```

### 2.3 Qdrant

```bash
# Windows (使用 Docker)
docker run -d \
  --name qdrant \
  -p 6333:6333 \
  -p 6334:6334 \
  qdrant/qdrant
```

### 2.4 MinIO

```bash
# Windows (使用 Docker)
docker run -d \
  --name minio \
  -e MINIO_ROOT_USER=minioadmin \
  -e MINIO_ROOT_PASSWORD=minioadmin \
  -p 9000:9000 \
  -p 9001:9001 \
  minio/minio server /data --console-address ":9001"
```

### 2.5 Elasticsearch（可选）

```bash
docker run -d \
  --name elasticsearch \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  -p 9200:9200 \
  -p 9300:9300 \
  docker.elastic.co/elasticsearch/elasticsearch:8.17.2
```

### 2.6 Kafka（可选）

```bash
# 使用 docker-compose 启动 Kafka + Zookeeper
docker run -d \
  --name zookeeper \
  -p 2181:2181 \
  confluentinc/cp-zookeeper:latest

docker run -d \
  --name kafka \
  -p 9092:9092 \
  -e KAFKA_ZOOKEEPER_CONNECT=localhost:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  confluentinc/cp-kafka:latest
```

---

## 3. 应用配置

### 3.1 配置文件

主要配置文件位于 `src/main/resources/application.yml`。

```yaml
# 核心配置
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/agenthub?schema=app
    username: things
    password: things123
  data:
    redis:
      host: localhost
      port: 6379
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: https://api.openai.com

agenthub:
  jwt:
    secret: your-jwt-secret-key-here
    expiration: 3600000
  storage:
    provider: minio
    minio:
      endpoint: http://localhost:9000
      access-key: minioadmin
      secret-key: minioadmin
      bucket: agenthub
```

### 3.2 环境变量

| 环境变量 | 说明 | 默认值 |
|---------|------|--------|
| `OPENAI_API_KEY` | OpenAI API 密钥 | - |
| `SPRING_PROFILES_ACTIVE` | 激活的配置文件 | - |

### 3.3 数据库初始化

应用启动时会自动执行 `schema.sql` 和 `init.sql` 初始化数据库结构和基础数据。

```bash
# 手动初始化（如需）
psql -U things -d agenthub -f sql/schema.sql
psql -U things -d agenthub -f sql/init.sql
```

---

## 4. 构建与运行

### 4.1 本地开发

```bash
# 构建项目
./gradlew clean build

# 运行测试
./gradlew test

# 启动应用（开发模式）
./gradlew bootRun

# 构建可执行 JAR
./gradlew bootJar

# 运行 JAR
java -jar build/libs/AgentHub-*.jar
```

### 4.2 前端构建

```bash
# 进入前端目录
cd src/main/web

# 安装依赖
npm install

# 开发模式
npm run dev

# 生产构建
npm run build
```

---

## 5. Docker 部署

### 5.1 使用 Dockerfile

```dockerfile
# 多阶段构建
FROM gradle:8-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle clean build --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
COPY --from=build /app/sql/ ./sql/
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

构建和运行：

```bash
# 构建镜像
docker build -t agenthub .

# 运行容器
docker run -d \
  --name agenthub \
  -p 8080:8080 \
  -e OPENAI_API_KEY=your-key \
  --network host \
  agenthub
```

### 5.2 使用 Docker Compose

以下是一个完整的 `docker-compose.yml` 示例：

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_USER: things
      POSTGRES_PASSWORD: things123
      POSTGRES_DB: agenthub
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  qdrant:
    image: qdrant/qdrant
    ports:
      - "6333:6333"
      - "6334:6334"
    volumes:
      - qdrant_data:/qdrant/storage

  minio:
    image: minio/minio
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    ports:
      - "9000:9000"
      - "9001:9001"
    command: server /data --console-address ":9001"
    volumes:
      - minio_data:/data

  agenthub:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/agenthub?schema=app
      SPRING_DATASOURCE_USERNAME: things
      SPRING_DATASOURCE_PASSWORD: things123
      SPRING_REDIS_HOST: redis
      SPRING_AI_OPENAI_API_KEY: ${OPENAI_API_KEY}
    depends_on:
      - postgres
      - redis
      - qdrant

volumes:
  postgres_data:
  redis_data:
  qdrant_data:
  minio_data:
```

启动：

```bash
docker-compose up -d
```

---

## 6. 多环境配置

### 6.1 配置文件结构

```
src/main/resources/
├── application.yml           # 公共配置
├── application-dev.yml       # 开发环境
├── application-test.yml      # 测试环境
└── application-prod.yml      # 生产环境
```

### 6.2 激活环境

```bash
# 通过环境变量
export SPRING_PROFILES_ACTIVE=prod

# 通过命令行参数
java -jar app.jar --spring.profiles.active=prod
```

---

## 7. 生产环境最佳实践

### 7.1 安全配置

1. **JWT 密钥**：使用强随机密钥，不要使用默认值
2. **数据库密码**：使用密码加密或环境变量注入
3. **API Key**：所有 API Key 通过环境变量配置
4. **HTTPS**：配置 SSL 证书强制 HTTPS
5. **CORS**：限制允许的跨域源

### 7.2 性能优化

1. **连接池**：根据并发量调整 Hikari 连接池大小
2. **缓存**：配置 Redis 缓存过期策略
3. **线程池**：调整异步任务线程池大小
4. **索引**：确保数据库表有适当索引
5. **日志**：生产环境关闭 DEBUG 日志

### 7.3 监控配置

应用已集成 Spring Boot Actuator：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

可用端点：
- `/actuator/health` - 健康检查
- `/actuator/info` - 应用信息
- `/actuator/metrics` - 性能指标

### 7.4 Nginx 反向代理配置

```nginx
upstream agenthub {
    server 127.0.0.1:8080;
}

server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://agenthub;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # SSE 支持
    location /api/v1/ {
        proxy_pass http://agenthub;
        proxy_buffering off;
        proxy_cache off;
        proxy_set_header Connection '';
        chunked_transfer_encoding on;
    }
}
```

---

## 8. 运维指南

### 8.1 日志管理

日志文件位于 `logs/` 目录：

```
logs/
├── interface/        # 接口日志
├── performance/      # 性能日志
└── run/             # 运行日志
```

### 8.2 健康检查

```bash
# 应用健康状态
curl http://localhost:8080/actuator/health

# 期望响应
{"status":"UP"}
```

### 8.3 备份策略

1. **数据库备份**：定期使用 pg_dump 备份 PostgreSQL
2. **对象存储**：MinIO 数据定期同步到异地存储
3. **配置文件**：使用版本控制管理配置文件

### 8.4 常见问题

| 问题 | 解决方案 |
|------|---------|
| 数据库连接失败 | 检查 PostgreSQL 服务和连接配置 |
| Redis 连接失败 | 检查 Redis 服务是否启动 |
| 向量数据库连接失败 | 检查 Qdrant/Milvus 等向量数据库服务 |
| 文件上传失败 | 检查 MinIO 服务和 Bucket 配置 |
| JWT 认证失败 | 检查 JWT 密钥配置和时间同步 |

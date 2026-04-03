# CrackView 项目初始化 — 实施计划 (Day 1-2)

## 技术决策

| 决策项 | 选择 | 理由 |
|--------|------|------|
| 数据库 | MySQL 8.0 | 更熟悉，面试中更常被问到 |
| 主键策略 | BIGINT AUTO_INCREMENT | MySQL 下性能优于 UUID，索引更友好 |
| Schema 管理 | Flyway | 低配置成本，体现工程规范，面试加分 |
| 构建工具 | Maven | Spring Boot 生态主流 |

## 实施步骤

### Step 1：Docker Compose 基础设施

创建 `docker-compose.yml`，包含三个服务：

- **MySQL 8.0** — 端口 3306，初始化 `crackview` 数据库
- **Redis 7** — 端口 6379，用于面试会话上下文缓存
- **Kafka**（KRaft 模式，无需 ZooKeeper）— 端口 9092

每个服务配置 volume 持久化和 healthcheck。

### Step 2：Spring Boot 项目骨架

创建 `backend/` 目录下的 Maven 项目。

**核心依赖：**

| 依赖 | 用途 |
|------|------|
| Spring Boot 3.x Starter Web | REST API |
| Spring Boot Starter WebSocket | 面试实时对话 |
| Spring Boot Starter Data JPA | ORM / Repository |
| MySQL Connector/J | 数据库驱动 |
| Spring Boot Starter Data Redis | Redis 缓存 |
| Spring Kafka | 消息队列 |
| Flyway (mysql) | 数据库版本迁移 |
| Lombok | 减少样板代码 |
| Spring Boot Starter Validation | 参数校验 |

**包结构：**

```
com.crackview
├── CrackViewApplication.java   # 启动类
├── agent/                       # Agent定义 + Tools（后续阶段）
├── controller/                  # REST + WebSocket controllers
├── service/                     # 业务逻辑
├── repository/                  # JPA repositories
├── model/                       # Entity classes
├── config/                      # Redis, Kafka 配置类
└── dto/                         # Request/Response DTOs
```

**配置文件：**

- `application.yml` — 通用配置
- `application-dev.yml` — 开发环境（本地 Docker 连接信息）

### Step 3：Flyway 数据库迁移

在 `src/main/resources/db/migration/` 下创建 SQL 迁移文件：

| 文件名 | 内容 |
|--------|------|
| `V1__create_users_table.sql` | 用户表 |
| `V2__create_knowledge_graph_tables.sql` | `knowledge_nodes` + `knowledge_edges` |
| `V3__create_user_knowledge_table.sql` | 用户知识掌握度（含 SM-2 字段） |
| `V4__create_interview_tables.sql` | `interview_sessions` + `interview_messages` |
| `V5__create_study_plans_table.sql` | 学习计划表 |

**MySQL 适配要点：**

- 主键使用 `BIGINT AUTO_INCREMENT` 代替 PostgreSQL 的 `UUID`
- 每张表增加 `uuid CHAR(36)` 字段作为业务层外部标识
- `JSONB` 改为 MySQL 的 `JSON` 类型
- `gen_random_uuid()` 改为 Java 层生成 UUID

### Step 4：JPA Entity + Repository

为每张表创建对应的 Entity 和 Repository：

| Entity | 对应表 | Repository |
|--------|--------|------------|
| `User` | `users` | `UserRepository` |
| `KnowledgeNode` | `knowledge_nodes` | `KnowledgeNodeRepository` |
| `KnowledgeEdge` | `knowledge_edges` | `KnowledgeEdgeRepository` |
| `UserKnowledge` | `user_knowledge` | `UserKnowledgeRepository` |
| `InterviewSession` | `interview_sessions` | `InterviewSessionRepository` |
| `InterviewMessage` | `interview_messages` | `InterviewMessageRepository` |
| `StudyPlan` | `study_plans` | `StudyPlanRepository` |

### Step 5：Config 配置类

- `RedisConfig.java` — RedisTemplate 序列化配置
- `KafkaConfig.java` — Producer/Consumer 基础配置（预留，MVP 阶段可先不启用）

### Step 6：知识图谱种子数据

创建 `src/main/resources/seed/knowledge_graph_seed.json`，包含四大 domain 约 80 个节点：

- **Java 基础**（~25 nodes）：JVM、并发、集合框架及其子节点
- **分布式系统**（~20 nodes）：CAP、一致性、分布式事务、消息队列
- **数据库**（~20 nodes）：MySQL、Redis 及其子主题
- **系统设计**（~15 nodes）：负载均衡、缓存策略、微服务等

同时预置节点间的边关系（prerequisite / related / contains）。

通过 `V6__seed_knowledge_graph.sql` Flyway 迁移脚本导入种子数据。

### Step 7：验证与冒烟测试

- 编写 `HealthController` — `GET /api/health` 返回服务状态
- `docker compose up` 启动所有基础设施
- Spring Boot 启动成功并连接 MySQL、Redis
- Flyway 迁移全部执行，种子数据正确写入
- 访问 `/api/health` 返回 200

## 产出物清单

```
CrackView/
├── backend/
│   ├── pom.xml
│   ├── src/main/java/com/crackview/
│   │   ├── CrackViewApplication.java
│   │   ├── model/
│   │   │   ├── User.java
│   │   │   ├── KnowledgeNode.java
│   │   │   ├── KnowledgeEdge.java
│   │   │   ├── UserKnowledge.java
│   │   │   ├── InterviewSession.java
│   │   │   ├── InterviewMessage.java
│   │   │   └── StudyPlan.java
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   ├── KnowledgeNodeRepository.java
│   │   │   ├── KnowledgeEdgeRepository.java
│   │   │   ├── UserKnowledgeRepository.java
│   │   │   ├── InterviewSessionRepository.java
│   │   │   ├── InterviewMessageRepository.java
│   │   │   └── StudyPlanRepository.java
│   │   ├── config/
│   │   │   ├── RedisConfig.java
│   │   │   └── KafkaConfig.java
│   │   ├── controller/
│   │   │   └── HealthController.java
│   │   └── dto/
│   └── src/main/resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── db/migration/
│       │   ├── V1__create_users_table.sql
│       │   ├── V2__create_knowledge_graph_tables.sql
│       │   ├── V3__create_user_knowledge_table.sql
│       │   ├── V4__create_interview_tables.sql
│       │   ├── V5__create_study_plans_table.sql
│       │   └── V6__seed_knowledge_graph.sql
│       └── seed/
│           └── knowledge_graph_seed.json
├── docker-compose.yml
└── .gitignore
```

## Flyway 简介

### 为什么用 Flyway？

Flyway 是数据库 schema 版本管理工具，解决的核心问题：

> 随着开发推进，会不断修改表结构（加字段、改索引、加新表）。Flyway 把每次变更记录成编号 SQL 文件（V1, V2, V3...），应用启动时自动按顺序执行未运行的迁移。

**项目价值：**

- 面试时体现工程规范意识
- 部署时保证数据库结构一致
- 种子数据导入也可以用迁移脚本管理
- 配置成本低，就是写 SQL 文件

### 工作原理

1. 在 `db/migration/` 下放置 `V{版本号}__{描述}.sql` 文件
2. Spring Boot 启动时 Flyway 自动扫描并执行未运行的迁移
3. Flyway 在数据库中维护 `flyway_schema_history` 表记录已执行的迁移
4. 只执行新增的迁移，已执行的不会重复运行

## MySQL 适配说明

| 项目 | PostgreSQL 原方案 | MySQL 适配 |
|------|------------------|------------|
| 主键 | `UUID DEFAULT gen_random_uuid()` | `BIGINT AUTO_INCREMENT` |
| 外部标识 | 主键即 UUID | 额外 `uuid CHAR(36)` 字段 |
| JSON | `JSONB`（支持索引） | `JSON`（MySQL 8.0 支持） |
| 驱动 | `org.postgresql:postgresql` | `com.mysql:mysql-connector-j` |
| 连接串 | `jdbc:postgresql://host:5432/db` | `jdbc:mysql://host:3306/db` |
| Docker | `postgres:16` | `mysql:8.0` |

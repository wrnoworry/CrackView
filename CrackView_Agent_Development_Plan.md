# CrackView Agent — 开发计划

## 项目定位

AI Agent驱动的智能面试教练系统。区别于传统"看题-看答案"模式，CrackView通过Multi-Agent协作，实现自主面经分析、动态追问、知识图谱可视化、自适应复习规划。

## 技术栈

| 层级 | 技术选型 | 用途 |
|------|---------|------|
| 后端框架 | Java 17 + Spring Boot 3.x | REST API, WebSocket, 业务逻辑 |
| Agent框架 | LangChain4j 或 Spring AI | Agent编排, Tool Use, ReAct模式 |
| 前端 | React 18 + TypeScript | SPA应用 |
| 知识图谱可视化 | D3.js (force-directed graph) | 交互式知识图谱 |
| 主数据库 | PostgreSQL | 用户、题目、答题记录、知识图谱节点/边 |
| 缓存 | Redis | 面试会话上下文、热点题目缓存 |
| 消息队列 | Kafka | 异步AI评估任务 |
| AI模型 | Claude API (Sonnet) | Agent的LLM backbone |
| 部署 | Docker + Docker Compose (MVP) | 本地/云端部署 |
| CI/CD | GitHub Actions | 自动化测试和部署 |

## 核心Agent设计

### 1. Planning Agent（规划Agent）

**职责**：分析目标公司面试特点，结合用户知识图谱现状，制定个性化学习路径。

**可用Tools**：
- `search_company_questions(company, role)` — 通过预置面经数据+Web搜索获取公司高频考点
- `query_knowledge_graph(userId, domain)` — 查询用户在某领域的掌握程度
- `get_study_history(userId, days)` — 获取最近N天的学习记录
- `create_study_plan(userId, plan)` — 生成并保存学习计划

**ReAct示例**：
```
Input: "准备Google后端面试"
Thought: 需要先了解Google后端的高频考点
Action: search_company_questions("Google", "backend")
Observation: 系统设计60%, Java并发20%, 数据库15%, 其他5%
Thought: 再看看用户当前水平
Action: query_knowledge_graph("user123", "all")
Observation: {系统设计: 55, Java并发: 75, Redis: 40, MySQL: 65}
Thought: Redis只有40分且Google考数据库15%，应该先补Redis，再攻系统设计
Action: create_study_plan("user123", {
  week1: ["Redis基础", "Redis持久化", "Redis集群"],
  week2: ["系统设计-缓存", "系统设计-消息队列"],
  ...
})
Output: 个性化学习计划
```

### 2. Interview Agent（面试官Agent）

**职责**：模拟真实面试官，根据用户回答动态追问，直到探到知识边界。

**可用Tools**：
- `get_topic_questions(topic, difficulty)` — 获取某主题的面试题
- `query_knowledge_graph(userId, topic)` — 实时查看用户某topic掌握度
- `record_answer(userId, questionId, answer, score)` — 记录用户回答

**关键设计 — 追问链**：
```
System Prompt:
你是一个严格但友善的技术面试官。你的目标是通过追问找到候选人的知识边界。
规则：
1. 如果候选人回答正确但不深入，追问细节
2. 如果候选人回答错误，先纠正，然后从更基础的角度重新提问
3. 如果候选人连续3次回答正确，升级难度
4. 每个session控制在5-8轮追问
5. 每轮结束后，用record_answer工具记录评分
```

**面试会话状态（存Redis）**：
```json
{
  "sessionId": "sess_001",
  "userId": "user123",
  "targetCompany": "Google",
  "currentTopic": "Redis",
  "roundNumber": 3,
  "conversationHistory": [...],
  "topicScores": {"redis-basics": 80, "redis-persistence": 30},
  "nextAction": "drill_down_persistence"
}
```

### 3. Evaluation Agent（评估Agent）

**职责**：面试结束后异步评估，更新知识图谱，安排复习。

**可用Tools**：
- `evaluate_session(sessionId)` — 综合评估本次面试表现
- `update_knowledge_graph(userId, nodeId, score)` — 更新知识节点分数
- `schedule_review(userId, topic, nextDate)` — SM-2间隔重复排期
- `generate_report(sessionId)` — 生成面试报告

**SM-2间隔重复算法**：
```
// 核心公式
nextInterval = previousInterval * easeFactor
easeFactor = max(1.3, EF + (0.1 - (5-quality) * (0.08 + (5-quality) * 0.02)))

// quality是用户回答质量 (0-5)
// 0-2: 重置interval为1天
// 3: 间隔不变
// 4-5: 间隔增长
```

## 数据库设计 (MySQL)

### 核心表

```sql
-- 用户表
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    target_company VARCHAR(100),
    target_role VARCHAR(100),
    created_at TIMESTAMP DEFAULT NOW()
);

-- 知识图谱 - 节点表
CREATE TABLE knowledge_nodes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,           -- e.g., "Redis持久化"
    domain VARCHAR(50) NOT NULL,          -- e.g., "database", "java", "system-design"
    parent_id UUID REFERENCES knowledge_nodes(id),
    depth INT DEFAULT 0,
    description TEXT
);

-- 知识图谱 - 边表（节点间的关系）
CREATE TABLE knowledge_edges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_id UUID REFERENCES knowledge_nodes(id),
    target_id UUID REFERENCES knowledge_nodes(id),
    relation_type VARCHAR(50),            -- "prerequisite", "related", "contains"
    weight FLOAT DEFAULT 1.0
);

-- 用户知识掌握度
CREATE TABLE user_knowledge (
    user_id UUID REFERENCES users(id),
    node_id UUID REFERENCES knowledge_nodes(id),
    score FLOAT DEFAULT 0,                -- 0-100
    ease_factor FLOAT DEFAULT 2.5,        -- SM-2
    interval_days INT DEFAULT 1,          -- SM-2
    next_review_date DATE,
    last_reviewed_at TIMESTAMP,
    review_count INT DEFAULT 0,
    PRIMARY KEY (user_id, node_id)
);

-- 面试会话
CREATE TABLE interview_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    target_company VARCHAR(100),
    domain VARCHAR(50),
    status VARCHAR(20) DEFAULT 'active',  -- active, completed, evaluated
    total_score FLOAT,
    started_at TIMESTAMP DEFAULT NOW(),
    completed_at TIMESTAMP
);

-- 面试对话记录
CREATE TABLE interview_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID REFERENCES interview_sessions(id),
    role VARCHAR(20) NOT NULL,            -- 'agent' or 'user'
    content TEXT NOT NULL,
    related_node_id UUID REFERENCES knowledge_nodes(id),
    score FLOAT,                          -- 单题得分
    created_at TIMESTAMP DEFAULT NOW()
);

-- 学习计划
CREATE TABLE study_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    target_company VARCHAR(100),
    plan_data JSONB NOT NULL,
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT NOW()
);
```

### 预置知识图谱节点（种子数据示例）

```
Java基础
├── JVM
│   ├── 内存模型
│   ├── 垃圾回收 (GC)
│   ├── 类加载机制
│   └── JIT编译
├── 并发
│   ├── 线程池
│   ├── synchronized vs ReentrantLock
│   ├── volatile
│   ├── CAS
│   └── AQS
├── 集合框架
│   ├── HashMap原理
│   ├── ConcurrentHashMap
│   └── ArrayList vs LinkedList

分布式系统
├── CAP定理
├── 一致性
│   ├── 强一致性
│   ├── 最终一致性
│   └── Raft/Paxos
├── 分布式事务
│   ├── 2PC
│   ├── Saga模式
│   └── TCC
├── 消息队列
│   ├── Kafka
│   ├── RabbitMQ
│   └── 消息可靠性

数据库
├── MySQL
│   ├── 索引 (B+树)
│   ├── 事务隔离级别
│   ├── MVCC
│   ├── 主从复制
│   └── 分库分表
├── Redis
│   ├── 数据结构
│   ├── 持久化 (AOF/RDB)
│   ├── 主从/哨兵/集群
│   ├── 缓存穿透/击穿/雪崩
│   └── 分布式锁

系统设计
├── 负载均衡
├── 缓存策略
├── 数据库设计
├── 限流降级
├── 微服务架构
└── 高可用设计
```

## API设计

### REST API

```
POST   /api/auth/register         — 注册
POST   /api/auth/login            — 登录

GET    /api/knowledge/graph       — 获取知识图谱（含用户掌握度）
GET    /api/knowledge/weak-spots  — 获取薄弱知识点

POST   /api/plan/generate         — 触发Planning Agent生成学习计划
GET    /api/plan/current          — 获取当前学习计划

POST   /api/interview/start       — 开始面试（创建session）
POST   /api/interview/answer      — 提交回答（触发Interview Agent追问）
POST   /api/interview/end         — 结束面试（触发Evaluation Agent）
GET    /api/interview/history     — 历史面试记录

GET    /api/review/today          — 今日待复习知识点
GET    /api/dashboard/stats       — 战力雷达图数据
GET    /api/dashboard/progress    — 学习进度趋势
```

### WebSocket

```
/ws/interview/{sessionId}   — 面试实时对话（流式返回Agent回复）
```

## 前端页面

1. **Dashboard首页** — 战力雷达图（6维：Java/分布式/数据库/系统设计/算法/DevOps）+ 今日待复习 + 学习streak
2. **知识图谱页** — D3.js力导向图，节点颜色=掌握度（红→黄→绿），点击节点展开子节点/开始练习
3. **面试模拟页** — Chat UI，实时流式显示Agent回复，侧边栏显示当前topic和评分
4. **学习计划页** — 甘特图/日历视图，每日任务打卡
5. **历史报告页** — 每次面试的详细报告，分数趋势折线图

## 3.5周开发Timeline

### Week 1：后端基础 + Agent骨架 (Day 1-7)

**Day 1-2: 项目初始化**
- [ ] Spring Boot项目搭建（Spring Initializr）
- [ ] MySQL + Redis + Kafka Docker Compose配置
- [ ] 数据库建表 + Flyway migration
- [ ] 知识图谱种子数据导入（Java/分布式/数据库/系统设计四大domain，约80个节点）

**Day 3-4: Agent框架搭建**
- [ ] 引入LangChain4j依赖（或Spring AI）
- [ ] 配置Claude API连接
- [ ] 定义Tool接口（@Tool注解），实现第一个Tool: `query_knowledge_graph`
- [ ] 实现基础ReAct循环：Thought → Action → Observation → Thought

**Day 5-7: Interview Agent核心**
- [ ] 实现Interview Agent的System Prompt
- [ ] 实现Tools: `get_topic_questions`, `record_answer`
- [ ] Redis存储面试会话状态
- [ ] WebSocket endpoint实现（面试实时对话）
- [ ] 单元测试：Agent能完成一轮追问

### Week 2：Agent完善 + 评估系统 (Day 8-14)

**Day 8-9: Planning Agent**
- [ ] 实现`search_company_questions` Tool（先用预置数据，后续可接Web Search）
- [ ] 实现`create_study_plan` Tool
- [ ] Planning Agent完整流程：输入公司→分析→出计划

**Day 10-11: Evaluation Agent + SM-2**
- [ ] Kafka Producer/Consumer配置
- [ ] 面试结束后发送评估消息到Kafka
- [ ] Evaluation Agent消费消息，调用Claude评分
- [ ] 实现SM-2间隔重复算法
- [ ] `update_knowledge_graph`和`schedule_review` Tools

**Day 12-14: API层完善**
- [ ] 所有REST API endpoint实现
- [ ] JWT认证
- [ ] 全局异常处理
- [ ] Swagger/OpenAPI文档
- [ ] 集成测试

### Week 3：前端开发 (Day 15-21)

**Day 15-16: 前端基础**
- [ ] React + TypeScript + Vite项目搭建
- [ ] 路由配置（React Router）
- [ ] 全局状态管理（Zustand或Context）
- [ ] API客户端封装（Axios + 拦截器）
- [ ] UI组件库配置（Ant Design或shadcn/ui）

**Day 17-18: 核心页面**
- [ ] Dashboard页：战力雷达图（Recharts）+ 今日任务卡片
- [ ] 面试Chat页：WebSocket连接 + 流式消息渲染 + Markdown支持

**Day 19-21: 知识图谱可视化**
- [ ] D3.js力导向图组件
- [ ] 节点颜色映射（掌握度 → 红黄绿渐变）
- [ ] 节点点击交互（展开子节点、查看详情、开始练习）
- [ ] 图谱缩放、拖拽、tooltip
- [ ] 历史报告页

### Week 3.5：部署 + 打磨 (Day 22-25)

**Day 22-23: Docker化 + 部署**
- [ ] 多阶段Dockerfile（后端 + 前端）
- [ ] Docker Compose生产配置
- [ ] GitHub Actions CI/CD pipeline
- [ ] 部署到云平台（推荐Railway或Render，免费tier）

**Day 24-25: 打磨 + 文档**
- [ ] 端到端测试：完整走一遍 "选公司→AI出计划→面试→评估→知识图谱更新→复习提醒"
- [ ] 修bug、优化体验
- [ ] README.md（含架构图、技术选型理由、演示截图）
- [ ] 录制Demo视频（2-3分钟）

## MVP范围（3.5周必须完成）

**必须有**：
- Interview Agent完整追问链（核心卖点）
- 知识图谱CRUD + D3.js可视化（技术亮点）
- SM-2间隔重复复习（算法亮点）
- 面试评估报告
- Docker部署

**可以砍**：
- Planning Agent的Web Search（先用预置面经数据）
- 多用户+JWT认证（MVP单用户即可）
- Kafka异步评估（MVP可同步，Kafka留架构位）
- 学习计划甘特图（用简单list代替）
- 算法题模块（聚焦八股文）

## 面试怎么讲这个项目

### 一句话介绍
"我构建了一个基于Multi-Agent架构的智能面试教练系统，Agent通过ReAct模式自主规划学习路径、模拟追问式面试、并用SM-2算法管理知识复习。"

### 技术深度切入点

1. **Agent设计** — 三个Agent如何协作，Tool Use机制，ReAct循环
2. **知识图谱** — 图数据建模，掌握度传播算法，D3.js可视化
3. **间隔重复** — SM-2算法原理，easeFactor动态调整
4. **异步架构** — Kafka解耦评估任务，为什么不用同步
5. **缓存设计** — Redis存面试会话上下文，cache aside策略
6. **WebSocket** — 面试实时对话，消息流式传输

### 可以延伸的系统设计问题
- "如果用户量到10万，你会怎么扩展？"（水平扩展Agent worker，Kafka partition）
- "AI调用很贵，怎么优化成本？"（缓存常见题目评估、batch evaluation）
- "如何保证AI回复质量？"（评估Agent的自检机制、用户反馈loop）

## 项目结构

```
crackview-agent/
├── backend/
│   ├── src/main/java/com/crackview/
│   │   ├── agent/               # Agent定义 + Tools
│   │   │   ├── PlanningAgent.java
│   │   │   ├── InterviewAgent.java
│   │   │   ├── EvaluationAgent.java
│   │   │   └── tools/
│   │   │       ├── KnowledgeGraphTool.java
│   │   │       ├── QuestionBankTool.java
│   │   │       ├── StudyHistoryTool.java
│   │   │       └── SM2SchedulerTool.java
│   │   ├── controller/          # REST + WebSocket controllers
│   │   ├── service/             # 业务逻辑
│   │   ├── repository/          # JPA repositories
│   │   ├── model/               # Entity classes
│   │   ├── config/              # Redis, Kafka, Security config
│   │   └── dto/                 # Request/Response DTOs
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── db/migration/        # Flyway SQL
│   │   └── seed/                # 知识图谱种子数据JSON
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   │   ├── KnowledgeGraph/  # D3.js图谱组件
│   │   │   ├── InterviewChat/   # 面试对话UI
│   │   │   ├── Dashboard/       # 雷达图+统计
│   │   │   └── common/
│   │   ├── pages/
│   │   ├── hooks/
│   │   ├── services/            # API calls
│   │   └── store/               # Zustand state
│   ├── package.json
│   └── vite.config.ts
├── docker-compose.yml
├── Dockerfile.backend
├── Dockerfile.frontend
├── .github/workflows/ci.yml
└── README.md
```

# CrackView Agent 框架搭建指南 (Day 3-4)

## 一、核心概念：从 Chatbot 到 Agent

### 1. 普通 Chatbot vs AI Agent

```
普通 Chatbot:
  用户 → "Redis持久化有几种方式？" → LLM → "RDB和AOF两种..." → 用户

AI Agent:
  用户 → "帮我准备Google后端面试"
       → LLM 思考："我需要先了解Google考什么"
       → LLM 调用工具：search_company_questions("Google", "backend")
       → 工具返回结果：{系统设计: 60%, 并发: 20%, ...}
       → LLM 思考："再看用户当前水平"
       → LLM 调用工具：query_knowledge_graph("user123", "all")
       → 工具返回结果：{系统设计: 55, Redis: 40, ...}
       → LLM 思考："Redis薄弱，应该先补"
       → LLM 调用工具：create_study_plan(...)
       → 最终输出个性化计划 → 用户
```

**关键区别**：Agent 能**自主决策**调用哪些工具（Tool）、按什么顺序调用、根据工具返回结果调整策略。LLM 不只是回答问题，而是作为"大脑"驱动一个完整的工作流。

### 2. Tool Use（工具调用）

Tool Use 是 Agent 的核心能力。原理：

```
1. 你定义一组 Tool（Java 方法），每个 Tool 有名称、描述、参数说明
2. 调用 LLM 时，把 Tool 列表一起发过去
3. LLM 不直接回答，而是返回"我要调用某个 Tool，参数是..."
4. 你的代码执行这个 Tool，把结果返回给 LLM
5. LLM 根据结果继续思考，可能再调用 Tool，或者最终给出回答
```

用 Java 来说就是：

```java
@Component
public class KnowledgeGraphTools {

    @Tool("查询用户在某领域的知识掌握程度，返回各知识点的分数")
    public String queryKnowledgeGraph(String userId, String domain) {
        // 这就是一个普通的 Java 方法！
        // 查数据库，拿数据，返回结果
        List<UserKnowledge> records = userKnowledgeRepository.findByUserId(userId);
        return formatAsJson(records);
    }
}
```

当你用 `@Tool` 注解标记一个方法时，LangChain4j 会：
- 提取方法名、参数名、描述信息
- 告诉 Claude："你有一个工具叫 queryKnowledgeGraph，接收 userId 和 domain 参数"
- Claude 在对话中如果需要这个信息，就会说"请调用 queryKnowledgeGraph"
- LangChain4j 自动执行这个 Java 方法，把结果喂回给 Claude

### 3. ReAct 模式（Reasoning + Acting）

ReAct 不是你需要自己写的循环代码，而是 LLM 自然的思考方式。流程：

```
Thought（思考）: 我需要先了解用户当前水平
  ↓
Action（行动）: 调用 query_knowledge_graph("user123", "java")
  ↓
Observation（观察）: {HashMap: 80, 线程池: 30, GC: 60}
  ↓
Thought（思考）: 线程池只有30分，需要重点练习
  ↓
Action（行动）: 调用 get_topic_questions("线程池", "medium")
  ↓
Observation（观察）: "请描述ThreadPoolExecutor的核心参数..."
  ↓
Thought（思考）: 好，我现在可以开始提问了
  ↓
Final Answer: 面试问题输出给用户
```

**你不需要写 ReAct 循环的代码**。Claude 天然支持这种模式——当你给它 Tools 和 System Prompt 后，它会自动进行"思考→调用工具→根据结果思考→..."直到得到答案。LangChain4j 框架自动处理这个循环。

### 4. AI Service（LangChain4j 的核心抽象）

LangChain4j 用 `@AiService` 把上述所有东西包装成一个简洁的 Java 接口：

```java
@AiService
public interface InterviewAgent {

    @SystemMessage("""
        你是一个严格但友善的技术面试官。
        你的目标是通过追问找到候选人的知识边界。
        规则：
        1. 如果候选人回答正确但不深入，追问细节
        2. 如果候选人回答错误，先纠正再重新提问
        3. 如果连续3次回答正确，升级难度
        """)
    String chat(@MemoryId String sessionId, @UserMessage String userMessage);
}
```

Spring Boot 启动时，LangChain4j 自动：
- 找到这个接口
- 绑定 ChatModel（Claude）
- 绑定所有 @Tool 方法
- 绑定 ChatMemory（对话记忆）
- 生成实现类，注册为 Spring Bean

你只需要 `@Autowired InterviewAgent agent` 然后调用 `agent.chat(...)` 就行。

## 二、架构总览

```
用户请求
   ↓
Controller (REST/WebSocket)
   ↓
AI Service 接口 (@AiService)
   ↓
├── ChatModel (Claude API)          ← LLM 大脑
├── ChatMemoryProvider (Redis)      ← 对话记忆
└── Tools (@Tool 注解的方法)         ← Agent 的手和脚
    ├── KnowledgeGraphTools         ← 查询/更新知识图谱
    ├── QuestionBankTools           ← 获取面试题
    └── StudyPlanTools              ← 创建学习计划
```

## 三、LangChain4j 关键组件说明

| 组件 | 作用 | 类比 |
|------|------|------|
| `ChatModel` | 与 Claude API 通信 | Agent 的大脑 |
| `StreamingChatModel` | 流式返回（打字机效果） | 大脑实时说话 |
| `@AiService` | 声明式定义 Agent 接口 | Spring `@Service` 的 AI 版 |
| `@SystemMessage` | 定义 Agent 的人设和行为规则 | Agent 的性格 |
| `@UserMessage` | 用户输入 | 用户说的话 |
| `@MemoryId` | 区分不同对话会话 | 哪个聊天窗口 |
| `@Tool` | 声明 Agent 可调用的工具 | Agent 的手 |
| `ChatMemory` | 保存对话历史 | Agent 的短期记忆 |
| `ChatMemoryProvider` | 为不同会话提供独立记忆 | 记忆管理器 |

## 四、实施计划

### Step 1: 添加 LangChain4j 依赖

在 `pom.xml` 中添加：

```xml
<!-- LangChain4j Spring Boot Starter（核心 + 自动装配） -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-spring-boot-starter</artifactId>
    <version>${langchain4j.version}</version>
</dependency>

<!-- LangChain4j Anthropic Spring Boot Starter（Claude API 集成） -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-anthropic-spring-boot-starter</artifactId>
    <version>${langchain4j.version}</version>
</dependency>
```

### Step 2: 配置 Claude API 连接

在 `application-dev.yml` 中添加：

```yaml
langchain4j:
  anthropic:
    chat-model:
      api-key: ${ANTHROPIC_API_KEY}
      model-name: claude-sonnet-4-5-20250929
      max-tokens: 4096
      temperature: 0.7
      log-requests: true
      log-responses: true
```

### Step 3: 实现第一个 Tool — KnowledgeGraphTools

```java
@Component
public class KnowledgeGraphTools {

    private final UserKnowledgeRepository userKnowledgeRepository;
    private final KnowledgeNodeRepository knowledgeNodeRepository;

    @Tool("查询用户在某个领域的知识掌握程度")
    public String queryKnowledgeGraph(
        @P("用户ID") Long userId,
        @P("知识领域，如 java/database/distributed/system-design") String domain
    ) {
        // 查数据库，返回结果
        // Claude 会根据返回内容继续推理
    }
}
```

### Step 4: 定义 Interview Agent AI Service

```java
@AiService
public interface InterviewAgent {

    @SystemMessage("""
        你是一个严格但友善的技术面试官...
        """)
    String chat(@MemoryId String sessionId, @UserMessage String userMessage);
}
```

### Step 5: 实现 ChatMemoryProvider（基于 Redis）

为每个面试 session 提供独立的对话记忆，存储在 Redis 中。

### Step 6: Controller 层接入

```java
@RestController
@RequestMapping("/api/interview")
public class InterviewController {

    @Autowired
    private InterviewAgent interviewAgent;

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody ChatRequest request) {
        String reply = interviewAgent.chat(request.getSessionId(), request.getMessage());
        return ResponseEntity.ok(reply);
    }
}
```

### Step 7: 测试

- 单元测试：Mock ChatModel 测试 Tool 方法逻辑
- 集成测试：验证 Agent 能调用 Tool 并完成一轮对话

## 五、最终实现 — 手写 ReAct（无 LangChain4j 依赖）

### 架构选择

最终采用**手写 ReAct 引擎**，不依赖 LangChain4j。优势：
- 完全理解底层原理，面试可以深入讲
- 零 AI 框架依赖，更轻量
- LLM 可插拔（Claude / GPT / 本地模型 / Mock）

### 核心组件

| 组件 | 作用 |
|------|------|
| `LlmClient` 接口 | LLM 抽象，可替换为任意模型 |
| `SimpleLlmClient` | 直接调 Anthropic HTTP API 的实现 |
| `StubLlmClient` | 无真实模型时的兜底实现 |
| `@AgentTool` / `@ToolParam` | 自定义注解，标记工具方法 |
| `ToolRegistry` | 扫描 @AgentTool，注册工具，反射执行 |
| `ReActEngine` | ReAct 循环核心：解析 Thought/Action/FinalAnswer |
| `InterviewAgent` | 面试官 Agent，组装 prompt + tools + memory |

### ReAct 循环流程

```
1. 构建 SystemPrompt = 人设 + 工具列表 + 格式要求
2. 发给 LLM
3. 解析返回文本：
   - 发现 "Action: xxx" → 用 ToolRegistry 执行 → 拼 Observation → 回到 2
   - 发现 "FinalAnswer: xxx" → 返回给用户
   - 超过 maxIterations → 强制返回
```

### 产出物清单

```
backend/src/main/java/com/crackview/
├── agent/
│   ├── InterviewAgent.java            # 面试官Agent实现
│   ├── InterviewAgentService.java     # Agent接口（用于Mock）
│   ├── core/
│   │   ├── AgentTool.java             # 自定义@AgentTool注解
│   │   ├── ToolParam.java             # 自定义@ToolParam注解
│   │   ├── ToolDefinition.java        # 工具元数据 record
│   │   ├── ToolRegistry.java          # 工具注册+反射执行
│   │   ├── LlmClient.java            # LLM抽象接口
│   │   ├── SimpleLlmClient.java       # Anthropic HTTP实现
│   │   ├── StubLlmClient.java         # 兜底Stub实现
│   │   └── ReActEngine.java           # ReAct循环核心
│   └── tools/
│       ├── KnowledgeGraphTools.java   # 知识图谱查询工具
│       └── QuestionBankTools.java     # 题库工具
├── controller/
│   └── InterviewController.java       # 面试REST接口
└── dto/
    ├── ChatRequest.java
    └── ChatResponse.java
```

### 测试覆盖（21 个新增测试）

| 测试类 | 数量 | 覆盖内容 |
|--------|------|----------|
| `ToolRegistryTest` | 7 | 工具注册、发现、执行、错误处理 |
| `ReActEngineTest` | 6 | 直接回答、单次/多次工具调用、未知工具、最大迭代、格式异常 |
| `InterviewAgentTest` | 5 | 对话、记忆保持、工具调用、Session清理 |
| `InterviewControllerTest` | 3 | REST接口、参数校验、Session删除 |

## 六、面试怎么讲

> "我使用 LangChain4j 框架集成 Claude API，通过 @AiService 注解声明式定义 Agent 接口，
> 用 @Tool 注解将业务方法暴露给 LLM。Agent 采用 ReAct 模式自主决策调用哪些工具——
> 比如面试时先查知识图谱了解用户水平，再动态调整追问策略。
> 对话上下文通过 ChatMemoryProvider 存储在 Redis 中，支持多会话隔离。"

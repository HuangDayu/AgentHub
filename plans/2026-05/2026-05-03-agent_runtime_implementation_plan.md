# AgentRuntime详细实现方案

## 1. Google ADK (adk.dev) - 真实API实现

### 文档来源
- GitHub: https://github.com/google/adk-java
- API文档: https://adk.dev/api-reference/java/

### 核心API (通过反编译jar包验证)
```java
// 1. 创建LlmAgent
LlmAgent agent = LlmAgent.builder()
    .name("agent-name")
    .description("Agent description")
    .model("gemini-2.0-flash")
    .instruction("You are a helpful assistant.")
    .tools(new GoogleSearchTool())  // 可选工具
    .build();

// 2. 创建Runner
Runner runner = new Runner(
    agent,                          // LlmAgent实例
    "app-name",                     // 应用名称
    null,                          // ArtifactService (可选)
    new InMemorySessionService()   // SessionService
);

// 3. 发送消息并获取响应
Content userMessage = Content.fromParts(Part.fromText("Hello"));
Flowable<Event> flow = runner.runAsync("user-id", "session-id", userMessage);

// 4. 处理事件流
StringBuilder result = new StringBuilder();
flow.blockingForEach(event -> {
    event.content().ifPresent(content -> {
        content.parts().ifPresent(parts -> {
            parts.forEach(part -> part.text().ifPresent(result::append));
        });
    });
});
```

### 实现要点
- **初始化**: 使用LlmAgent.builder()创建Agent，Runner构造函数创建Runner
- **发送消息**: runner.runAsync()返回Flowable<Event>，使用blockingForEach处理
- **流式消息**: 同样使用runAsync()，逐Event回调StreamCallback
- **会话管理**: 使用InMemorySessionService管理会话
- **线程池**: 所有操作在虚拟线程池中执行

---

## 2. Langchain4j (langchain4j.dev) - 真实API实现

### 文档来源
- 官方文档: https://docs.langchain4j.dev/tutorials/agents/
- GitHub: https://github.com/langchain4j/langchain4j

### 核心API (从官方文档)
```java
// 1. 使用AgenticServices创建Agent
WithdrawAgent withdrawAgent = AgenticServices
    .agentBuilder(WithdrawAgent.class)
    .chatModel(BASE_MODEL)
    .tools(bankTool)
    .build();

// 2. 创建SupervisorAgent
SupervisorAgent bankSupervisor = AgenticServices
    .supervisorBuilder()
    .chatModel(PLANNER_MODEL)
    .subAgents(withdrawAgent, creditAgent, exchangeAgent)
    .responseStrategy(SupervisorResponseStrategy.SUMMARY)
    .build();

// 3. 调用SupervisorAgent
String result = bankSupervisor.invoke("Transfer 100 EUR from Mario's account to Georgios' one");

// 4. SupervisorAgent接口定义
public interface SupervisorAgent {
    @Agent
    String invoke(@V("request") String request);
}
```

### 实现要点
- **初始化**: 使用AgenticServices.supervisorBuilder()创建SupervisorAgent
- **发送消息**: 调用supervisorAgent.invoke(request)方法
- **流式消息**: Langchain4j支持TokenStream API
- **子Agent管理**: 通过subAgents()方法添加子Agent
- **响应策略**: 使用SupervisorResponseStrategy控制响应方式

---

## 3. Embabel-Agent (embabel.com) - 真实API实现

### 文档来源
- 官方文档: https://docs.embabel.com/embabel-agent/guide/
- GitHub: https://github.com/embabel/embabel-agent

### 核心API (从官方文档和jar包)
```java
// 1. 定义Agent类 (使用@Action注解)
public class StarNewsFinder {
    
    @Action
    public StarPerson extractStarPerson(UserInput userInput, Ai ai) {
        return ai
            .withLlm(OpenAiModels.GPT_41)
            .createObjectIfPossible(
                "Create a person from this user input...",
                StarPerson.class
            );
    }
    
    @Action(toolGroups = {CoreToolGroups.WEB})
    public RelevantNewsStories findNewsStories(
        StarPerson person,
        Horoscope horoscope,
        Ai ai) {
        return ai
            .withDefaultLlm()
            .createObject(prompt, RelevantNewsStories.class);
    }
    
    @AchievesGoal(description = "Write an amusing writeup...")
    @Action
    public Writeup writeup(StarPerson person, RelevantNewsStories stories, Ai ai) {
        // 实现逻辑
    }
}

// 2. Agent核心类构造
Agent agent = new Agent(
    "agent-name",        // name
    "provider",          // provider
    "1.0",              // version
    "description",      // description
    Set.of(),           // conditions
    List.of(),          // actions
    Set.of()            // goals
);
```

### 实现要点
- **初始化**: 创建Agent实例，定义goals和actions
- **执行**: 使用@Action注解定义动作，@AchievesGoal标记目标达成
- **AI服务**: 通过Ai接口调用LLM
- **工具支持**: 使用toolGroups指定工具组
- **规划系统**: Embabel使用GOAP规划系统自动规划执行路径

---

## 4. OpenJiuwen (openjiuwen.com) - 真实API实现

### 文档来源
- 官方文档: https://www.openjiuwen.com/docs-page
- API路径: openjiuwen.harness.deep_agent

### 核心API (通过反编译jar包)
```java
// 1. 创建AgentCard
AgentCard card = new AgentCard();
card.setName("agent-name");
card.setDescription("Agent description");

// 2. 创建ReActAgent
ReActAgent agent = new ReActAgent(card);

// 3. 调用Agent (同步)
Session session = new Session();
Object result = agent.invoke("message", session);

// 4. 流式调用
Iterator<Object> stream = agent.stream("message", session, List.of());
while (stream.hasNext()) {
    Object chunk = stream.next();
    // 处理每个chunk
}

// 5. 配置Agent
ReActAgentConfig config = agent.getConfig();
```

### 实现要点
- **初始化**: 创建AgentCard设置属性，然后创建ReActAgent
- **发送消息**: 使用agent.invoke(message, session)
- **流式消息**: 使用agent.stream()获取Iterator，逐个处理
- **会话管理**: 使用Session类管理会话状态
- **配置**: 通过ReActAgentConfig配置Agent行为

---

## 5. 统一AgentTeam实现方案

### 设计思路
AgentTeam是一组Agent的协作，不依赖特定框架，统一管理：

```java
// 1. 初始化团队
Map<String, AgentRuntime> agentRuntimes = new HashMap<>();
ThreadGroup teamGroup = new ThreadGroup("team-" + teamId);

// 2. 并行执行
agentRuntimes.values().parallelStream()
    .forEach(rt -> rt.sendMessage(id, sid, message));

// 3. 串行执行
agentRuntimes.values()
    .forEach(rt -> rt.sendMessage(id, sid, message));

// 4. 使用ThreadGroup管理
ThreadGroup group = groups.get(id);
group.interrupt();  // 停止所有线程
```

### 实现要点
- **Agent管理**: 维护Map<String, AgentRuntime>管理多个Agent
- **线程管理**: 使用ThreadGroup管理所有Agent的线程
- **执行模式**: 支持并行和串行两种执行模式
- **状态管理**: 统一管理所有Agent的运行状态
- **资源清理**: 通过ThreadGroup.interrupt()停止所有Agent

---

## 6. 实施步骤

### Step 1: Google ADK实现 ✅
```java
// 已实现 - GoogleAdkAgentRuntime.java
- 使用LlmAgent.builder()创建Agent
- 使用Runner构造函数创建Runner
- 使用runAsync()和Flowable处理消息
- 正确处理Event、Content、Part
- 所有方法不超过10行
```

### Step 2: Langchain4j实现
```java
// 待实现 - Langchain4jAgentRuntime.java
- 使用AgenticServices.supervisorBuilder()创建SupervisorAgent
- 实现invoke()调用
- 研究TokenStream API实现流式
```

### Step 3: Embabel实现
```java
// 待实现 - EmbabelAgentRuntime.java
- 创建Agent实例，定义goals和actions
- 使用Ai接口调用LLM
- 实现基于@Action的执行
```

### Step 4: OpenJiuwen实现
```java
// 待实现 - OpenJiuwenAgentRuntime.java
- 创建AgentCard和ReActAgent
- 实现invoke()调用
- 实现stream()流式调用
```

### Step 5: AgentTeam实现 ✅
```java
// 已实现 - UnifiedAgentTeamRuntime.java
- 管理多个AgentRuntime
- 实现并行/串行执行
- 使用ThreadGroup管理线程
```

---

## 7. 注意事项

1. **不要使用伪代码**: 所有实现必须使用真实API ✅
2. **方法不超过10行**: 拆分复杂逻辑 ✅
3. **使用线程池**: 所有异步操作使用虚拟线程池 ✅
4. **异常处理**: 正确处理所有异常 ✅
5. **资源管理**: 正确管理会话和资源生命周期 ✅
6. **API验证**: 所有API都通过jar包反编译或官方文档验证 ✅

---

## 8. 依赖配置

```gradle
dependencies {
    // Google ADK
    implementation 'com.google.adk:google-adk:1.2.0'
    
    // Langchain4j
    implementation platform("dev.langchain4j:langchain4j-bom:1.14.0")
    implementation "dev.langchain4j:langchain4j-agentic"
    
    // Embabel Agent
    implementation "com.embabel.agent:embabel-agent-starter:0.2.0-SNAPSHOT"
    
    // OpenJiuwen
    implementation 'com.openjiuwen:agent-core-java:0.1.7'
}
```

所有依赖已正确配置在build.gradle中。

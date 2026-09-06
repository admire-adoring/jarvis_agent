# 贾维斯智能体（JARVIS Agent）能力清单与 MVP 迭代指南

> 定位：场景自适应个人生活助理 —— 按意图自动切换专业角色、动态调整 UI、调用工具、提供沉浸式体验。
> 适用读者：零基础 ~ 初级开发者，按 MVP 顺序逐步搭建，每个版本都是可运行的完整产品。
> 技术基线：Spring Boot 3.x + Spring AI 2.0.x + React 18 + TypeScript + Ant Design（Spring AI 2.0 配置无 `.options` 中间层，模型名直接配在 `spring.ai.openai.chat.model`）。

---

## 第一部分：Agent 核心能力清单

> 简历亮点描述均可直接引用；量化指标请按实际项目数据替换。

| # | 能力名称 | 核心要点 | 简历亮点描述 |
|---|---------|---------|-------------|
| 1 | 意图识别与自动角色切换 | 三层识别架构：规则匹配（<10ms）→ 向量相似度（<50ms）→ LLM 语义理解（<500ms），逐层降级；场景命中后动态切换系统提示词与专业角色 | 设计三层意图识别引擎（关键词规则 + Sentence-Transformers 向量召回 + LLM 语义兜底），实现 8+ 生活场景自动路由与专业角色切换，场景识别准确率达 95%，平均识别延迟 <50ms |
| 2 | 动态 UI 适配 | 场景-组件映射表、主题色/图标/布局配置化、组件按需加载（React.lazy / Module Federation）、切换动画 | 构建场景化动态 UI 系统，基于 React.lazy + 场景-组件映射表实现组件按需加载与主题毫秒级热切换，场景切换延迟 <100ms |
| 3 | 工具调用与编排（Function Calling / MCP） | 工具注册中心（ToolCallback[]）、LLM 自动选择与参数填充、MCP 协议接入第三方服务（地图/天气）、调用链编排与错误重试 | 基于 Spring AI ToolCallback 与 MCP（Model Context Protocol）实现 20+ 工具统一注册、LLM 自动选择与链式编排，接入高德地图/天气等外部服务，复杂任务完成率显著提升 |
| 4 | 上下文记忆与多轮对话管理 | 三层记忆：Redis 短期会话（TTL 30min）、Neo4j 长期画像（永久）、Milvus 知识记忆（可更新）；记忆提取与召回注入 | 设计三层记忆架构（Redis 会话级短期记忆 + Neo4j 用户画像长期记忆 + Milvus 知识记忆），支撑跨会话个性化服务，个性化回复准确率达 85% |
| 5 | 沉浸式交互体验 | SSE 流式输出与逐字渲染、WebSocket 双向通道、语音交互、数字人形象 | 基于 Spring MVC + SseEmitter（Servlet 3.1 异步）实现标准 SSE 全链路流式输出与前端逐字渲染，首字响应 <1s，并预留 WebSocket 与多模态语音交互扩展通道 |
| 6 | 知识增强（RAG + 知识图谱） | RAG 流水线：加载→智能切分→向量化→混合检索（向量+关键词+图谱）→重排序；多场景知识库隔离；空结果兜底 | 落地企业级 RAG 流水线（智能切分 → Milvus 混合检索 → Rerank 重排序），融合 Neo4j 知识图谱关系推理增强检索，答案准确率提升 35%，支撑 10 万+ 文档知识库 |
| 7 | 多 Agent 协作 | 主控 Agent（任务分解/调度/整合）+ 领域专家 Agent；并行咨询、流水线、辩论三种协作模式；共享记忆解决状态同步 | 设计主控 Agent + 领域专家 Agent 协作架构，支持并行咨询/流水线/辩论三种协作模式，通过共享记忆与消息队列解决 Agent 间状态同步，多 Agent 协作效率提升 50% |
| 8 | 推理能力（ReAct / CoT） | ReAct「思考-行动-观察」循环、CoT 思维链任务分解、多步工具链式调用、失败反思重试 | 基于 ReAct 推理模式与 CoT 思维链实现任务自主「分解-执行-反思」循环，支持多步工具链式调用与失败自动重试，提升复杂任务自主完成能力 |
| 9 | 模型路由 | 适配器模式封装 OpenAI/Claude/通义千问/本地模型；按场景复杂度、成本、延迟动态调度 | 基于适配器模式实现 ModelRouter 多模型动态路由（GPT-4 / Claude / 通义千问），按任务复杂度与成本智能调度，在保证质量前提下降低推理成本 30% |
| 10 | 预测性 UI | 马尔可夫链建模用户行为序列、预测下一场景、组件预加载、LRU 多级缓存（内存 + IndexedDB） | 基于马尔可夫链对用户行为序列建模预测下一场景，配合组件预加载与多级缓存（内存 + IndexedDB）实现场景切换零感知延迟 |
| 11 | Harness 层架构 | 统一 Agent 注册/调度/监控/日志的接入基础设施；Prometheus + Grafana 全链路可观测；插件化扩展 | 构建 Agent Harness 基础设施层，统一多 Agent 注册、调度、日志与监控，落地 Prometheus + Grafana 全链路可观测（QPS / token 成本 / 错误率），支撑 Agent 平行扩展 |
| 12 | 评测机制 | 三层评测：场景识别准确率、RAG 检索质量（Recall / MRR）、回答质量（RAGAS 忠实度/相关性/完整性）；评测看板 | 建设三层 Agent 评测体系（场景识别准确率、RAG 检索 Recall/MRR、RAGAS 回答忠实度与相关性），以数据驱动提示词与检索策略迭代优化 |
| 13 | 自进化能力 | 用户反馈（点赞/点踩/纠正）→ 记忆更新 → 提示词与策略优化的闭环；A/B 验证 | 设计用户反馈驱动的自进化闭环（反馈收集 → 记忆更新 → 策略迭代 → A/B 验证），主动服务采纳率达 70%，实现"越用越懂用户" |

**简历一句话定位（可放项目描述）：**

> 独立设计并实现场景自适应多 Agent 个人生活助理：集成三层意图识别、ReAct 推理、Function Calling + MCP 工具编排、企业级 RAG + 知识图谱、三层记忆架构与动态 UI 系统，支持 8+ 场景自动识别与沉浸式交互。

---

## 第二部分：MVP 迭代计划

### 版本划分调整说明

相对原 7 版方案做三处调整，共 **9 个 MVP**：

1. **原 MVP-6 拆分**为「MVP-6 工具调用与 MCP」和「MVP-7 RAG 知识增强」——两者各自都是重头戏，合在一版 7-10 天根本做不完，拆开后每版可独立验证；
2. **补入 MVP-8 多 Agent 协作版**——多 Agent 协作是贾维斯核心卖点，原计划中缺失，放在工具与知识就绪之后水到渠成；
3. **原 MVP-7 顺延为 MVP-9**，聚焦 Harness 基础设施 + 评测 + 自进化。

| 版本 | 主题 | 预计耗时 | 新增能力 |
|------|------|---------|---------|
| MVP-1 | 基础对话版 | 2-3 天 | 前后端跑通 + LLM 对话 |
| MVP-2 | 流式输出版 | 2-3 天 | SSE 流式响应 |
| MVP-3 | 场景识别与模型路由版 | 3-5 天 | 意图识别 + 角色/模型切换 |
| MVP-4 | 动态 UI 版 | 3-5 天 | 场景化组件 + 预测性加载 |
| MVP-5 | 记忆系统版 | 5-7 天 | 三层记忆 + 个性化 |
| MVP-6 | 工具调用与 MCP 版 | 4-6 天 | Function Calling + MCP 生态接入 |
| MVP-7 | RAG 知识增强版 | 5-7 天 | 向量检索 + 图谱增强 |
| MVP-8 | 多 Agent 协作版 | 4-6 天 | 主控+专家架构 + ReAct |
| MVP-9 | Harness 与评测版 | 5-7 天 | 基础设施 + 评测 + 自进化 |

> 原则：每个版本结束都得到一个**可运行、可演示的完整产品**，下一版只做增量。

---

### MVP-1：基础对话版（预计 2-3 天）

**版本目标：** 跑通前后端与 LLM 对话全链路，拿到第一个能聊天的贾维斯。

**技术栈：** Spring Boot 3 + Spring AI + React 18 + TypeScript + Ant Design

**实现内容：**
- Spring AI 接入 LLM（OpenAI 兼容接口，支持通义千问等国产模型）
- REST 聊天接口 `POST /api/chat`
- React 聊天界面（消息列表 + 输入框）
- 内存对话历史（`List<Message>`，进程重启即失，可接受）

**关键实现要点：**

```yaml
# application.yml —— OpenAI 兼容配置（通义千问换 base-url 即可）
spring:
  ai:
    openai:
      api-key: ${DASHSCOPE_API_KEY}
      base-url: https://dashscope.aliyuncs.com/compatible-mode
      chat:
        options:
          model: qwen-plus
          temperature: 0.7
```

```java
// AiConfig.java —— ChatClient 是 Spring AI 的核心入口
@Configuration
public class AiConfig {
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
```

```java
// ChatController.java —— 最小可用聊天接口
@RestController
@RequestMapping("/api")
public class ChatController {
    private final ChatClient chatClient;
    private final List<Message> history = new CopyOnWriteArrayList<>(); // MVP-1 内存历史

    public ChatController(ChatClient chatClient) { this.chatClient = chatClient; }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest req) {
        history.add(new UserMessage(req.message()));
        String content = chatClient.prompt()
                .messages(history)
                .user(req.message())
                .call()
                .content();
        history.add(new AssistantMessage(content));
        return new ChatResponse(content);
    }
}
```

```tsx
// ChatPage —— 前端仅做 fetch + 渲染，先不追求体验
const send = async () => {
  const res = await fetch("/api/chat", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ message: input }),
  });
  const data = await res.json();
  setMessages((prev) => [...prev, { role: "user", text: input }, { role: "assistant", text: data.content }]);
};
```

**验证标准：** 浏览器输入消息能正常往返对话，多轮上下文连贯（模型记得前文）。

**学习目标：** 掌握 Spring AI 基本用法（ChatClient / Prompt / Message），理解 LLM API 调用流程与无状态本质（上下文靠客户端回传）。

---

### MVP-2：流式输出版（预计 2-3 天）

**版本目标：** 回复逐字显示，体验对标主流 AI 产品。

**技术栈：** Spring MVC（Servlet 栈）+ `SseEmitter`（标准 SSE 帧）+ 前端逐字渲染

**实现内容：**
- SSE 流式接口 `GET /api/chat/stream`
- 前端 EventSource 接收并逐字渲染
- 打字指示器（TypingIndicator）动画
- 流结束标记与消息落库（替换 MVP-1 的 List）

**关键实现要点（MVC 下流式必须用 `SseEmitter`，不可直接返回 `Flux`——WebFlux 的 SSE 编码器在 MVC 栈不生效，详见 ChatController#sseChat 的 javadoc）：**

```java
@PostMapping(value = "/api/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public ResponseEntity<SseEmitter> stream(@RequestBody ChatRequest request) {
    SseEmitter emitter = new SseEmitter(0L);
    chatClient.prompt()
            .messages(memory.get(request.sessionId()))   // MVP-2 仍可内存存储
            .user(request.message())
            .stream()                                    // Spring AI 流式调用：任何栈下都返回 Flux<String>
            .content()
            .subscribe(
                chunk -> emitter.send(SseEmitter.event().name("delta").data(chunk)), // 逐字帧
                emitter::completeWithError,
                emitter::complete
            );
    return ResponseEntity.ok()
            .contentType(MediaType.TEXT_EVENT_STREAM)
            .body(emitter);
}
```

```tsx
// 前端逐字渲染：onmessage 追加而非覆盖
const es = new EventSource(`/api/chat/stream?message=${encodeURIComponent(input)}&sessionId=${sid}`);
es.onmessage = (e) => {
  setStreamingText((prev) => prev + e.data);   // 每次到达一个 token 片段
};
es.onerror = () => { es.close(); finalizeMessage(); };
```

**避坑：**
- `EventSource` 只支持 GET；若必须 POST，用 `fetch` + `ReadableStream` 手动解析 `data:` 行；
- SSE 经 Nginx 需关闭缓冲：`proxy_buffering off;`，否则整段一次性吐出；
- 每条 SSE 消息中的换行需转义（`\n\n` 是事件边界）。

**验证标准：** 回复逐字流畅显示；打字动画自然；断网/中断不白屏。

**学习目标：** 掌握 SSE 原理与 Spring MVC 的 Servlet 3.1 异步（`SseEmitter`）用法——推送不占请求线程；订阅回调里只做帧写入，不做阻塞重活。

---

### MVP-3：场景识别与模型路由版（预计 3-5 天）

**版本目标：** 输入"我家猫生病了"自动切到宠物场景，回复风格、系统提示词、（后续）UI 全变。

**技术栈：** 场景枚举 + 关键词匹配引擎 + 配置化（提示词/主题/模型映射）

**实现内容：**
- 场景枚举（8 个场景，含触发词、系统提示词、主题色、图标）
- 关键词匹配引擎（第一层识别，后续版本再叠向量/LLM 层）
- 动态系统提示词切换
- 轻量模型路由：场景 → 模型映射（编程场景用强推理模型，闲聊用低成本模型）

**关键实现要点：**

```java
public enum ScenarioType {
    PET(Set.of("宠物", "猫", "狗", "疫苗", "生病"),
        "你是一位有十年临床经验的宠物医生，回答专业、温和、给出可执行建议。",
        "qwen-plus", "#52c41a", "🐱"),
    CODING(Set.of("代码", "bug", "编程", "报错", "开发"),
        "你是一位资深全栈工程师，回答精确、给出可运行代码。",
        "deepseek-r1", "#1677ff", "💻"),
    // ... CAREER / EMOTION / LIFE / STUDY / ENTERTAINMENT / HEALTH 共 8 个
    ;

    private final Set<String> keywords;
    private final String systemPrompt;
    private final String model;      // 模型路由：场景级模型映射
    private final String themeColor;
    private final String icon;
}
```

```java
// ScenarioDetector —— 第一层：关键词规则（<10ms）；未命中走默认场景
public ScenarioType detect(String message) {
    return Arrays.stream(ScenarioType.values())
            .filter(s -> s.getKeywords().stream().anyMatch(message::contains))
            .findFirst()
            .orElse(ScenarioType.LIFE);
}
```

```java
// 场景化调用：注入系统提示词 + 切换模型
public Flux<String> chat(String sessionId, String message) {
    ScenarioType scenario = detector.detect(message);
    return chatClient.prompt()
            .system(scenario.getSystemPrompt())
            .messages(memory.get(sessionId))
            .user(message)
            .options(OpenAiChatOptions.builder().model(scenario.getModel()).build())
            .stream().content();
}
```

**验证标准：** "我家猫不吃东西"→ 宠物医生口吻；"这段代码为什么空指针"→ 工程师口吻；同一会话切换话题能跟随切换。

**学习目标：** 掌握意图识别最简实现与提示词工程；理解"配置驱动"设计——场景全部收敛到枚举/配置，为 MVP-4 动态 UI 打地基。

---

### MVP-4：动态 UI 版（预计 3-5 天）

**版本目标：** 场景切换时，UI 面板、主题色、组件同步变化，带过渡动画。

**技术栈：** React.lazy + Suspense + CSS 变量主题 + 场景-组件映射 + 行为序列预测预加载

**实现内容：**
- 场景 UI 配置（每场景的布局、组件清单、主题色、图标）
- 动态组件加载（`React.lazy` + `Suspense`，按需加载场景面板）
- 场景切换动画（淡入淡出 / 主题色 300ms 过渡）
- 左侧场景面板 + 右侧工具面板骨架
- 预测性预加载：记录场景转移序列，马尔可夫链预测下一场景并提前 `import()`

**关键实现要点：**

```tsx
// scenario-ui.ts —— 场景 → UI 配置映射（与后端枚举一一对应）
export const scenarioConfig = {
  PET: {
    themeColor: "#52c41a",
    icon: "🐱",
    panel: React.lazy(() => import("./scenarios/PetPanel")),   // 症状速查 + 疫苗提醒卡片
    tools: ["医院搜索", "疫苗提醒"],
  },
  CODING: { /* ... */ },
} as const;
```

```tsx
// 场景切换渲染：主题用 CSS 变量，切换只改变量，性能远优于重渲染组件树
<div className="app" style={{ "--theme": scenario.themeColor } as React.CSSProperties}>
  <Suspense fallback={<Loading />}>
    <scenario.panel />
  </Suspense>
</div>
```

```ts
// 预测性预加载：统计「当前场景 → 下一场景」频次，闲时预取 top1 组件
const transitions: Record<string, Record<string, number>> = loadFromLocalStorage();
function preloadNextScenario(current: string) {
  const next = Object.entries(transitions[current] ?? {}).sort((a, b) => b[1] - a[1])[0]?.[0];
  if (next) void import(`./scenarios/${next}Panel`);   // webpack/vite 会打包但延迟加载
}
```

**验证标准：** 场景切换时面板/主题色/图标同步变化，300ms 平滑过渡；Network 面板可见组件 chunk 按需与预加载。

**学习目标：** 掌握代码分割与动态组件加载；理解"主题即数据"（CSS 变量驱动）与预测性资源加载思路。

---

### MVP-5：记忆系统版（预计 5-7 天）

**版本目标：** 用户说"我家猫叫汤圆"之后，下次说"汤圆最近不吃饭"能正确理解；跨会话记住偏好。

**技术栈：** Redis（短期记忆）+ Neo4j（长期画像）+ Spring AI ChatMemory

**实现内容：**
- Redis 存储当前会话上下文（TTL 30 分钟，滑动续期）
- Neo4j 存储用户画像（宠物、偏好、事实性信息的实体关系图）
- 记忆提取：对话结束后用 LLM 抽取"值得长期记住"的事实
- 记忆召回：构建 prompt 时把用户画像注入系统提示词
- 对话历史持久化（PostgreSQL / Redis Hash）

**关键实现要点：**

```java
// 短期记忆：Spring AI ChatMemory（2.0 已核实 API）+ Redis 仓储
// 注意：官方内置仅 InMemory/JdbcChatMemoryRepository，Redis 版自行实现
// ChatMemoryRepository 接口即可（本质就是 List<Message> 的读写）
@Bean
public ChatMemory chatMemory(RedisChatMemoryRepository repo) {  // 自实现，~50 行
    return MessageWindowChatMemory.builder()
            .chatMemoryRepository(repo)
            .maxMessages(20)          // 滑动窗口，防上下文爆炸
            .build();
}
```

```cypher
// Neo4j 长期记忆：实体关系建模，MERGE 保证幂等
MERGE (u:User {id: $userId})
MERGE (p:Pet {name: $petName})
MERGE (u)-[:HAS_PET]->(p)
SET p.species = '英短猫', p.weight = 4.2, p.updatedAt = datetime()
```

```java
// 记忆提取：会话尾轮触发，让 LLM 判断哪些是长期事实
public void extractAndStore(String sessionId, Long userId) {
    String result = chatClient.prompt()
            .system("""
                从以下对话中抽取值得长期记住的用户事实（宠物、健康、偏好、重要日期），
                输出 JSON 数组 [{"type":"pet","content":"..."}]，无则输出 []。只输出 JSON。
                """)
            .messages(memory.get(sessionId))
            .call().content();
    profileService.saveFacts(userId, JsonUtil.parseFacts(result));  // 写 Neo4j
}
```

```java
// 记忆召回：构建 prompt 前注入画像
String profile = profileService.getProfilePrompt(userId);   // "用户养一只英短猫汤圆，4.2kg..."
chatClient.prompt().system(scenario.getSystemPrompt() + "\n用户画像：" + profile)...
```

**验证标准：** 新会话中"汤圆最近不吃饭"能被正确理解（知道汤圆是猫）；重启服务记忆不丢；画像随对话持续丰富。

**学习目标：** 掌握三层记忆架构的设计权衡（短期管上下文、长期管事实、知识管领域）；理解 Neo4j 图建模与 Cypher 基础。

---

### MVP-6：工具调用与 MCP 版（预计 4-6 天）

**版本目标：** 贾维斯能"动手"——查真实天气、搜地图、执行任务；并通过 MCP 接入开放生态。

**技术栈：** Spring AI Function Calling（`@Tool` / `ToolCallbackProvider`）+ MCP Client + MCP Inspector

#### 6.1 Function Calling：第一个工具

```java
@Component
public class WeatherTool {
    @Tool(description = "查询指定城市的实时天气，返回温度、天气现象与出行建议")
    public String getWeather(
            @ToolParam(description = "城市名称，如：北京") String city) {
        return weatherApiClient.query(city);   // 内部调真实 API
    }
}
```

```java
// 工具注册中心：聚合所有 @Tool Bean，LLM 自动感知并选择调用
@Bean
public ToolCallbackProvider toolCallbacks(WeatherTool weatherTool, HospitalTool hospitalTool) {
    return MethodToolCallbackProvider.builder()
            .toolObjects(weatherTool, hospitalTool)
            .build();
}
```

注册后 `chatClient.prompt()...call()` 即自动携带工具定义，LLM 决定何时调用、填什么参数，框架回填结果继续推理。

#### 6.2 MCP 循序渐进专题

**① 基础概念与核心原理**

MCP（Model Context Protocol）是 Anthropic 于 2024 年开源的上下文协议，解决的是工具集成的 **M×N 问题**（M 个应用 × N 个工具 = M×N 份集成代码 → 统一协议后变成 M+N），被称为"AI 应用的 USB-C 接口"。截至 2026 年已被 OpenAI、Google、Microsoft 及主流 Agent 框架广泛采纳。

三个角色：
- **Host**：承载 LLM 的应用（贾维斯后端就是 Host）
- **Client**：Host 内部与单个 Server 保持会话的连接器
- **Server**：暴露能力的外部进程/服务（如高德地图 MCP Server）

三大服务端原语：
- **Tools**：模型可调用的函数（有副作用，模型决策）—— 对应 Function Calling
- **Resources**：只读上下文数据（文件、数据库行），由 Host 附加给模型
- **Prompts**：服务端预置的提示词模板（工作流）

通信底座为 **JSON-RPC 2.0**；传输层两种：**stdio**（本地子进程，Claude Desktop 同款，零网络开销）与 **Streamable HTTP**（远程服务化，支持多租户）。客户端反向提供 Sampling（Server 请求 Host 代为调用 LLM）、Roots（文件系统边界）、Elicitation（Server 向用户征询输入）等能力。

**② Agent 集成方法（Spring AI MCP Client）**

```yaml
# application.yml —— 声明式接入 MCP Server（stdio 方式）
# 依赖：spring-ai-starter-mcp-client（2.0.x）
spring:
  ai:
    mcp:
      client:
        enabled: true
        toolcallback:
          enabled: true          # 自动将 MCP 工具包装为 ToolCallbackProvider，注入工具池
        stdio:
          connections:
            amap:                              # 高德地图 MCP Server
              command: npx
              args: ["-y", "@amap/amap-maps-mcp-server"]
              env:
                AMAP_MAPS_API_KEY: ${AMAP_KEY}
```

启动后 Spring AI 自动发现该 Server 的全部 tools，与本地 `@Tool` 一并汇入工具池，LLM 无差别调用——**本地工具与生态工具在模型视角完全同构**，这正是 MCP 的核心价值。

**③ 开发实践：自己写一个 MCP Server**

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server</artifactId>  <!-- stdio 传输 -->
</dependency>
```

```yaml
spring:
  ai:
    mcp:
      server:
        name: jarvis-life-tools
        version: 1.0.0
```

把 `@Tool` 方法注册到 `ToolCallbackProvider` 即自动暴露为 MCP 工具，可被 Claude Desktop、任意 MCP Host 复用。工具设计规范：
- 命名：`动词_对象`（`get_weather`），全小写下划线；
- description 写清"什么时候该用我"——LLM 靠它做选择，描述差 = 永远不被调用；
- 参数必须带 `@ToolParam` 描述与校验；错误以工具执行错误返回（而非协议错误），让模型能自我修正；
- 调试用官方 **MCP Inspector**：`npx @modelcontextprotocol/inspector`，可视化列出工具、手动调用验证。

**④ 进阶用法**

- **远程部署**：换 `spring-ai-starter-mcp-server-webmvc` 以 Streamable HTTP 对外提供服务，配 OAuth 2.1 鉴权，变成团队共享工具服务；
- **生态复用**：官方 MCP Registry（registry.modelcontextprotocol.io）有大量现成 Server（GitHub、数据库、浏览器、云服务），接入即用；
- **规范演进**：2025-11-25 稳定版引入 Tasks（长时任务）与 URL 授权模式；2026-07-28 修订版走向无状态核心（移除 initialize 握手、新增 `server/discover` 与 `subscriptions/listen`）。学习时以「会话 + 能力协商」主流模型为准，SDK 会屏蔽差异。

#### 6.3 本版验证标准

- 问"北京今天适合跑步吗"→ 自动调用天气工具并基于真实数据回答；
- 接入高德 MCP Server 后能查询真实地点/路线；
- 自己写的 MCP Server 能被 MCP Inspector 正确列出并调用。

**学习目标：** 掌握 Function Calling 全流程、MCP 协议核心模型与 Client/Server 双侧开发，理解"工具即生态"的接入思路。

---

### MVP-7：RAG 知识增强版（预计 5-7 天）

**版本目标：** 贾维斯能基于自有知识库回答专业问题（宠物医疗手册、面试题库），不再只靠模型通用知识。

**技术栈：** Milvus + Spring AI ETL（DocumentReader / TextSplitter / EmbeddingModel / VectorStore）+ Neo4j 图谱检索 + Rerank

**实现内容：**
- 离线流水线：文档加载（PDF/Word/TXT）→ 智能切分 → 向量化 → 入库 Milvus（按场景隔离 Collection）
- 在线检索：查询重写 → 混合检索（向量 + 关键词 + 图谱）→ 重排序 → 上下文拼接 → 空结果兜底（明确说不知道，防幻觉）

**关键实现要点：**

```java
// 离线：知识入库（一条链式流水线）
public void ingest(Resource doc, ScenarioType scenario) {
    List<Document> chunks = new TokenTextSplitter(
            TokenTextSplitterConfig.builder()
                    .withChunkSize(500).withMinChunkSizeChars(200).build())
            .apply(new TikaDocumentReader(doc).read());
    chunks.forEach(d -> d.getMetadata().put("scenario", scenario.name())); // 场景隔离
    vectorStore.write(chunks);
}
```

```java
// 在线：混合检索 + 拼接 + 兜底
public String answerWithRag(String question, ScenarioType scenario) {
    String rewritten = queryRewriter.rewrite(question);                  // LLM 查询重写
    List<Document> hits = vectorStore.similaritySearch(SearchRequest.builder()
            .query(rewritten).topK(5)
            .filterExpression("scenario == '" + scenario + "'").build()); // 元数据过滤
    List<String> graphFacts = knowledgeGraph.expand(rewritten);          // Neo4j 关系补充
    if (hits.isEmpty() && graphFacts.isEmpty()) {
        return fallbackAnswer(question);   // 兜底：声明无依据，不给编造
    }
    String context = formatContext(rerank(hits, rewritten), graphFacts);
    return chatClient.prompt()
            .system(scenario.getSystemPrompt() + "\n仅依据以下资料回答，资料中没有的信息要明说：\n" + context)
            .user(question).call().content();
}
```

**验证标准：** 问知识库内的专业问题（如"猫传腹早期症状"）回答有据可依且能给出出处片段；问知识库外的问题明确表示无资料；检索延迟 <500ms。

**学习目标：** 掌握 RAG 全流水线与混合检索设计；理解"空结果兜底"对抑制幻觉的意义。

---

### MVP-8：多 Agent 协作版（预计 4-6 天）

**版本目标：** 跨领域复杂问题由主控 Agent 分解、多个专家 Agent 协作完成。

**技术栈：** 主控-专家 Agent 架构 + ReAct 推理 + 共享记忆 + 完整模型路由

**实现内容：**
- 主控 Agent（Orchestrator）：任务分析 → 分解 → 调度专家 → 整合结果 → 冲突消解
- 专家 Agent：每场景一个，拥有独立的系统提示词、知识库路由、工具集与模型配置
- ReAct 循环：思考（选工具/选专家）→ 行动 → 观察 → 迭代
- 三种协作模式：并行咨询（跨领域）、流水线（多步骤）、辩论（正反方+仲裁）
- 模型路由完善：按任务复杂度动态选型（简单 → 低成本模型，复杂 → 强推理模型）

**关键实现要点：**

```java
// 专家 Agent 统一抽象 —— Harness 层雏形
public interface ExpertAgent {
    ScenarioType scenario();
    Flux<String> handle(String task, AgentContext ctx);   // ctx 携带共享记忆与工具
}

@Component
public class Orchestrator {
    private final Map<ScenarioType, ExpertAgent> agents;   // 注册式，MVP-9 演进为注册中心
    private final ChatClient planner;

    public Flux<String> execute(String task, AgentContext ctx) {
        // ReAct：LLM 输出结构化计划 → 逐个调度 → 结果回填继续推理
        Plan plan = Plan.parse(planner.prompt()
                .system("""
                        你是任务调度器。将用户任务分解为子任务 JSON 数组，
                        每项 {"scenario":"PET","subtask":"..."}；单领域任务只输出一项。
                        """)
                .user(task).call().content());
        return Flux.fromIterable(plan.subtasks())
                .flatMap(st -> agents.get(st.scenario()).handle(st.subtask(), ctx))
                .collectList().map(this::integrate);       // 整合多方结论、消解冲突
    }
}
```

**验证标准：** "我家猫生病了，帮我看看症状，顺便推荐下附近的宠物医院，再把这件事记到明天日程"——单次请求自动分解为宠物专家（RAG 症状）+ 生活管家（地图/日程工具）协作完成。

**学习目标：** 掌握主控-专家多 Agent 编排与 ReAct 推理循环；理解共享记忆解决 Agent 间状态同步的机制。

---

### MVP-9：Harness 与评测版（预计 5-7 天）

**版本目标：** 从"能跑"到"可运营"——统一基础设施、量化评测、反馈驱动的自进化。

**技术栈：** Harness 层（注册/调度/监控）+ Micrometer + Prometheus + Grafana + RAGAS + 反馈闭环

**实现内容：**
- **Harness 层**：统一 Agent 注册中心、调度入口、结构化日志（traceId 贯穿全链路）、指标埋点
- **场景识别准确率**：标注样本集 + 线上识别结果统计，Grafana 看板
- **RAG 检索质量**：Recall@5 / MRR，离线评测脚本
- **回答质量**：RAGAS 忠实度（faithfulness）/ 相关性 / 完整性
- **反馈闭环**：点赞/点踩/纠正 → 差评 case 归档 → 记忆更新 + 提示词/检索策略迭代 → 对比验证

**关键实现要点：**

```java
// Harness：统一调度入口 + 指标埋点（所有 Agent 收敛到这一个口）
@Service
public class AgentHarness {
    private final AgentRegistry registry;
    private final MeterRegistry metrics;

    public Flux<String> dispatch(String task, AgentContext ctx) {
        long start = System.currentTimeMillis();
        ScenarioType scenario = detector.detect(task);
        return registry.get(scenario).handle(task, ctx)
                .doFinally(sig -> metrics.timer("agent.execution",
                        "scenario", scenario.name(),
                        "status", sig.name())
                        .record(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS));
    }
}
```

```java
// 反馈闭环：点踩差评进入 case 库，驱动策略迭代
@PostMapping("/api/feedback")
public void feedback(@RequestBody Feedback fb) {
    metrics.counter("agent.feedback", "scenario", fb.scenario(), "type", fb.type()).increment();
    if (fb.isNegative()) caseLibrary.archive(fb);   // 归档 → 人工/自动分析 → 提示词与检索策略调整
}
```

**验证标准：** Grafana 可见各场景 QPS / 延迟 / token 成本 / 差评率；评测报告能量化 RAG 检索与回答质量；差评 case 能回流驱动一轮可见的优化。

**学习目标：** 掌握 Agent 工程化基础设施设计，理解"没有评测就没有优化"——用 RAGAS 等指标把玄学变成数据。

---

## 第三部分：技术栈总览

| 层次 | 技术 | 引入版本 | 学习优先级 |
|------|------|---------|-----------|
| 后端框架 | Spring Boot 4 MVC（Servlet 栈）+ SseEmitter | MVP-1 / MVP-2 | ★★★★★ |
| LLM 接入 | Spring AI（ChatClient / ChatMemory） | MVP-1 / MVP-5 | ★★★★★ |
| 前端 | React 18 + TypeScript + Ant Design | MVP-1 | ★★★★☆ |
| 流式通信 | SSE / EventSource | MVP-2 | ★★★★☆ |
| 提示词工程 | 系统提示词 / 角色扮演 / 输出约束 | MVP-3 | ★★★★★ |
| 前端工程化 | React.lazy / CSS 变量主题 / 预加载 | MVP-4 | ★★★☆☆ |
| 缓存与记忆 | Redis / Neo4j + Cypher | MVP-5 | ★★★★☆ |
| 工具调用 | Function Calling（@Tool / ToolCallback） | MVP-6 | ★★★★★ |
| 协议生态 | MCP（Client + Server + Inspector） | MVP-6 | ★★★★★ |
| 检索增强 | Milvus / Embedding / Rerank / RAG | MVP-7 | ★★★★☆ |
| 多 Agent | 主控-专家编排 / ReAct / CoT | MVP-8 | ★★★★☆ |
| 可观测与评测 | Prometheus / Grafana / RAGAS | MVP-9 | ★★★☆☆ |
| 部署 | Docker Compose / Nginx | 贯穿 | ★★★☆☆ |

---

## 第四部分：学习建议

### 4.1 学习顺序（零基础路线）

1. **先跑通再理解**：MVP-1 不求甚解先跑起来，建立"输入→LLM→输出"的直觉；
2. **每个版本只攻一个新概念**：流式（MVP-2）、提示词（MVP-3）、记忆（MVP-5）、工具（MVP-6）、检索（MVP-7）——不要并行开新坑；
3. **MVP-6 的 MCP 专题按 ①→④ 顺序读**：概念 → 集成 → 自研 Server → 进阶，读完立刻动手接一个真实 MCP Server（高德地图最佳，有真实体感）；
4. **评测（MVP-9）不要跳**：没有指标的前八版是"感觉良好"，有指标才是工程。

### 4.2 避坑指南

- **LLM 是无状态的**：上下文全靠客户端回传，别指望模型"记得"，记忆系统（MVP-5）就是在补这个洞；
- **SSE 订阅回调里别做阻塞重活**（JDBC、RestTemplate 等）：`SseEmitter` 回调只负责帧写入；耗时业务放在普通方法里同步执行（MVC 阻塞式线程天然适合，无需响应式心智）；
- **工具 description 决定调用率**：LLM 只看描述决定用不用工具，写"什么时候该用"而不是"这是什么"；
- **RAG 必须做空结果兜底**：检索为空时明确告知，否则模型自由发挥就是幻觉现场；
- **MCP stdio Server 的 stdout 是协议通道**：业务日志一律走 stderr，否则污染 JSON-RPC 流导致连接崩溃；
- **向量库不是越大越好**：topK 5~8 + Rerank 通常优于 topK 30 裸搜，上下文过长反而掉准确率；
- **密钥全部环境变量**：`spring.ai.openai.api-key` 从不进 Git。

### 4.3 资源推荐

- MCP 官方规范与文档：modelcontextprotocol.io（含 Inspector 工具与 Registry）
- Spring AI 官方文档：docs.spring.io/spring-ai（ChatClient / Tool / MCP / VectorStore 章节与本指南一一对应）
- RAGAS 文档：docs.ragas.io（忠实度/相关性指标定义）
- Milvus 快速入门：milvus.io/docs（Docker 单机模式 10 分钟起）
- Neo4j Cypher 入门：neo4j.com/docs/cypher-manual

---

*文档版本：v1.0 · 2026-09-04 · 基于 MVP 计划与能力清单整合输出*

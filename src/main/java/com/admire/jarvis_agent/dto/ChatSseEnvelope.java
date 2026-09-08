package com.admire.jarvis_agent.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @Description SSE 统一信封（每帧 data 的固定结构）
 * @Author Liu Yang
 * @Date 2026/9/6 11:54
 *
 * <p>传输层仍是标准 SSE（{@code id / event / retry / data}），由
 * {@code org.springframework.web.servlet.mvc.method.annotation.SseEmitter} 承载。
 * 本类约束的是 {@code data} 内部的结构 —— 每一帧都自描述「谁、哪个场景、哪次会话、第几帧、什么内容」，
 * 便于 Apifox / 日志 / 前端在只看单帧时也能定位上下文。
 *
 * <pre>
 * id:12
 * event:delta
 * retry:3000
 * data:{"v":1,"type":"delta","agent":"career","scenario":"career",
 *       "conversationId":"career-001","seq":12,"ts":1757130000000,
 *       "payload":{"role":"assistant","content":"大"}}
 * </pre>
 *
 * <p>各 {@link ChatEventType} 对应的 payload：
 * <ul>
 *   <li>{@code start}   —— {@code {"model":"deepseek-v4-flash-0731"}}</li>
 *   <li>{@code delta}   —— {@code {"role":"assistant","content":"..."}}</li>
 *   <li>{@code agent}   —— {@code {"agent":"health","role":"support","reason":"..."}}</li>
 *   <li>{@code handoff} —— {@code {"from":"emotion","to":"health","reason":"..."}}</li>
 *   <li>{@code artifact}—— {@code {"id":"...","kind":"exercise-prescription","title":"...","data":{...}}}</li>
 *   <li>{@code done}    —— {@code {"finishReason":"stop"}}</li>
 *   <li>{@code error}   —— {@code {"code":"UPSTREAM_ERROR","message":"..."}}</li>
 * </ul>
 *
 * <p>设计注：payload 以 {@code Object} 承载而非强类型子类，是为了让「Service 产出领域事件、
 * Controller 只做事件→帧映射」这条分层不被 DTO 膨胀拖累；未来接入 Orchestrator 时，
 * Service 改为返回 {@code Flux<ChatEvent>}，Controller 与本类均无需改动。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatSseEnvelope {

    /** 协议版本：前端据此判断是否兼容，后续破坏性变更时 +1 */
    private static final int PROTOCOL_VERSION = 1;

    /** 协议版本 */
    private final int v;

    /** 事件类型，取值见 {@link ChatEventType}（与 SSE 的 event 字段同值） */
    private final String type;

    /** 产出该帧的 Agent（jarvis / pet / code / career / emotion / life / learn / fun / health） */
    private final String agent;

    /** 业务场景：现阶段与 agent 同值；IntentAgent 接入后二者可不同（如在宠物场景下调用代码专家） */
    private final String scenario;

    /** 会话 ID：多 Agent 协作时用于把各路流归并到同一段对话 */
    private final String conversationId;

    /** 帧序号，与 SSE 的 id 同值，单调递增 */
    private final int seq;

    /** 服务端生成该帧的毫秒时间戳 */
    private final long ts;

    /** 业务载荷，结构由 {@link #type} 决定 */
    private final Object payload;

    private ChatSseEnvelope(int v, String type, String agent, String scenario,
                            String conversationId, int seq, long ts, Object payload) {
        this.v = v;
        this.type = type;
        this.agent = agent;
        this.scenario = scenario;
        this.conversationId = conversationId;
        this.seq = seq;
        this.ts = ts;
        this.payload = payload;
    }

    /**
     * 构造信封。scenario 未独立建模前与 agent 同值。
     */
    public static ChatSseEnvelope of(ChatEventType type, String agent, String conversationId,
                                     int seq, Object payload) {
        return new ChatSseEnvelope(PROTOCOL_VERSION, type == null ? null : type.getValue(),
                agent, agent, conversationId, seq, System.currentTimeMillis(), payload);
    }

    public int getV() {
        return v;
    }

    public String getType() {
        return type;
    }

    public String getAgent() {
        return agent;
    }

    public String getScenario() {
        return scenario;
    }

    public String getConversationId() {
        return conversationId;
    }

    public int getSeq() {
        return seq;
    }

    public long getTs() {
        return ts;
    }

    public Object getPayload() {
        return payload;
    }
}

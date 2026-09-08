package com.admire.jarvis_agent.dto;

/**
 * @Description SSE 事件类型（统一协议）
 * @Author Liu Yang
 * @Date 2026/9/6 11:54
 *
 * <p>每帧 SSE 的 {@code event} 与信封里的 {@code type} 取值一致，客户端只需信 {@code type}。
 *
 * <p>生命周期：{@code start → delta* → done}，异常以 {@code error} 收尾（终态，其后不再发 done）。
 * {@code agent / handoff / artifact} 为多 Agent 协作预留，由后端 Orchestrator 产出（P1）。
 */
public enum ChatEventType {

    /** 建流：带上模型等信息，客户端可据此初始化 */
    START("start"),

    /** 增量文本：payload.content 为本次到达的片段 */
    DELTA("delta"),

    /** 调度通知：主控引入了某个协作 Agent（payload: agent / role / reason） */
    AGENT("agent"),

    /** 转介中：责任从一个 Agent 交给另一个（payload: from / to / reason） */
    HANDOFF("handoff"),

    /** 跨场景产出物：如运动处方卡（payload: id / kind / title / data） */
    ARTIFACT("artifact"),

    /** 正常结束 */
    DONE("done"),

    /** 异常终止（终态） */
    ERROR("error"),

    /** 心跳保活：无业务载荷，用于穿透代理的空闲超时 */
    PING("ping");

    private final String value;

    ChatEventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

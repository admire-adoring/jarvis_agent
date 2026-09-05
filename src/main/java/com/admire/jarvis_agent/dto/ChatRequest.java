package com.admire.jarvis_agent.dto;

/**
 * @Description 对话请求（前端 ChatTransport → 后端 SSE）
 * @Author Liu Yang
 * @Date 2026/9/5 13:40
 */
public record ChatRequest(

        /**
         * 会话 ID：前端按「Agent + 会话」生成，用于隔离多轮上下文（ChatMemory 的 key）
         */
        String conversationId,

        /**
         * 目标 Agent：jarvis（主控）/ pet / code / career / emotion / life / learn / fun / health
         */
        String agent,

        /**
         * 用户输入
         */
        String message
) {

    public String conversationIdOrDefault() {
        return (conversationId == null || conversationId.isBlank()) ? "default" : conversationId;
    }

    public String agentOrDefault() {
        return (agent == null || agent.isBlank()) ? "jarvis" : agent;
    }
}

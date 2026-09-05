package com.admire.jarvis_agent.service;

import reactor.core.publisher.Flux;

/**
 * @Description 对话 Service
 * @Author Liu Yang
 * @Date 2026/9/4 10:35
 */
public interface ChatService {

    /**
     * 单轮对话（阻塞，调试用）
     *
     * @param agent          目标 Agent（jarvis / pet / code / ...）
     * @param conversationId 会话 ID，用于隔离多轮上下文
     * @param message        用户输入
     * @return 模型回复
     */
    String chat(String agent, String conversationId, String message);

    /**
     * 流式对话：逐段产出**增量文本**（SSE 帧的组装在 Controller 层）
     *
     * @param agent          目标 Agent
     * @param conversationId 会话 ID，作为 ChatMemory 的 key 隔离上下文
     * @param message        用户输入
     * @return 增量文本流
     */
    Flux<String> chatStream(String agent, String conversationId, String message);
}

package com.admire.jarvis_agent.service;

/**
 * @Description 对话 Service
 * @Author Liu Yang
 * @Date 2026/9/4 10:35
 */
public interface ChatService {

    /**
     * 单轮对话
     *
     * @param conversationId 会话 ID，用于隔离多轮上下文
     * @param message        用户输入
     * @return 模型回复
     */
    String chat(String conversationId, String message);
}

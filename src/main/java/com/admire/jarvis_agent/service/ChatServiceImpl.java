package com.admire.jarvis_agent.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @Description 对话 Service Impl
 * @Author Liu Yang
 * @Date 2026/9/4 10:35
 */
@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;

    public ChatServiceImpl(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public String chat(String conversationId, String message) {
        log.info("chat start, conversationId={}, message={}", conversationId, message);
        if (!StringUtils.hasText(message)) {
            log.warn("chat rejected, empty message, conversationId={}", conversationId);
            return "请输入内容";
        }
        String content = chatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
        String answer = content == null ? "" : content;
        log.info("chat end, conversationId={}, answerLength={}", conversationId, answer.length());
        return answer;
    }
}

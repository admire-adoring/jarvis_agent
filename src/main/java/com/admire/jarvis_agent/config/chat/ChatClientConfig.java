package com.admire.jarvis_agent.config.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description Spring AI ChatClient 配置
 * @Author Liu Yang
 * @Date 2026/9/4 10:38
 */
@Configuration
public class ChatClientConfig {

    /**
     * 窗口式对话记忆：按 conversationId 隔离上下文
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder().build();
    }

    @Bean
    public ChatClient jarvisChatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        return builder
                .defaultSystem("你是 Jarvis，一个简洁、直接的助手。回答用中文，不啰嗦。")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}

package com.admire.jarvis_agent.service;

import com.admire.jarvis_agent.config.chat.AgentPrompts;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

/**
 * @Description 对话 Service Impl
 * @Author Liu Yang
 * @Date 2026/9/4 10:35
 */
@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    private static final String EMPTY_MESSAGE = "请输入内容";

    @Resource
    private ChatClient chatClient;

    @Override
    public String chat(String agent, String conversationId, String message) {
        log.info("chat start, agent={}, conversationId={}, message={}", agent, conversationId, message);
        if (!StringUtils.hasText(message)) {
            log.warn("chat rejected, empty message, conversationId={}", conversationId);
            return EMPTY_MESSAGE;
        }
        String content = chatClient.prompt()
                .system(AgentPrompts.of(agent))
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
        String answer = content == null ? "" : content;
        log.info("chat end, agent={}, conversationId={}, answerLength={}", agent, conversationId, answer.length());
        return answer;
    }

    @Override
    public Flux<String> chatStream(String agent, String conversationId, String message) {
        log.info("chatStream start, agent={}, conversationId={}, message={}", agent, conversationId, message);
        if (!StringUtils.hasText(message)) {
            log.warn("chatStream rejected, empty message, conversationId={}", conversationId);
            return Flux.just(EMPTY_MESSAGE);
        }
        return chatClient.prompt()
                .system(AgentPrompts.of(agent))
                .user(message)
                // conversationId 作为 ChatMemory 的 key：不同 Agent / 会话的上下文互相隔离
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content()
                .doOnNext(chunk -> log.debug("chatStream chunk, conversationId={}, chunk={}", conversationId, chunk))
                .doOnComplete(() -> log.info("chatStream complete, agent={}, conversationId={}", agent, conversationId))
                .doOnError(e -> log.error("chatStream error, agent={}, conversationId={}", agent, conversationId, e));
        // 错误不在此吞成文本：让异常冒泡到 Controller，统一转 event=error 终态帧，
        // 前端才能区分「模型正常回复」与「系统出错」（否则 error 事件永远不会出现）
    }
}

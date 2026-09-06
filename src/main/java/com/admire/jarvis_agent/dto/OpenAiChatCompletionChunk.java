package com.admire.jarvis_agent.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * OpenAI 兼容的流式 Chat Completion Chunk，用于 SSE {@code delta} 事件。
 *
 * <p>字段对齐 OpenAI <code>chat.completion.chunk</code> 协议（id / object / created / model / choices），
 * 顶层额外扩展项目字段：
 * <ul>
 *   <li>{@code agent}：当前回复的专家/场景标识（jarvis / pet / code / ...）</li>
 *   <li>{@code scenario}：当前会话场景，当前与 agent 一致；后续路由脑支持动态切换后可独立</li>
 *   <li>{@code conversationId}：会话 ID，供前端按会话隔离与断点续推</li>
 * </ul>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAiChatCompletionChunk(
        /** OpenAI 风格流 ID，形如 chatcmpl-xxxx */
        String id,

        /** 对象类型，固定为 chat.completion.chunk */
        @JsonProperty("object")
        String objectType,

        /** Unix 时间戳（秒） */
        long created,

        /** 模型名，取自 spring.ai.openai.chat.model */
        String model,

        /** 当前回复 Agent（项目扩展字段） */
        String agent,

        /** 当前场景（项目扩展字段） */
        String scenario,

        /** 会话 ID（项目扩展字段） */
        String conversationId,

        /** choices[0].delta.content 为增量文本 */
        List<Choice> choices
) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Choice(
            int index,
            Delta delta,
            @JsonProperty("finish_reason")
            String finishReason,
            Object logprobs
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Delta(
            String role,
            String content
    ) {
    }
}

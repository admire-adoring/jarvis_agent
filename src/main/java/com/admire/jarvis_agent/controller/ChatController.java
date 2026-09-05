package com.admire.jarvis_agent.controller;

import com.admire.jarvis_agent.dto.ChatRequest;
import com.admire.jarvis_agent.service.ChatService;
import jakarta.annotation.Resource;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * @Description 对话
 * @Author Liu Yang
 * @Date 2026/9/4 09:07
 */
@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    /**
     * Spring Boot 4.x 内置的是 Jackson 3（包名为 tools.jackson，与 Spring AI 依赖的 Jackson 2 并存），
     * 这里自建实例而不注入容器 bean，避免受自动配置开关影响。
     */
    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    @Resource
    private ChatService chatService;

    /**
     * 单轮对话（阻塞，调试用；正式链路走 /chat/sse）
     */
    @PostMapping
    public String chat(@RequestBody ChatRequest request) {
        return chatService.chat(request.agentOrDefault(), request.conversationIdOrDefault(), request.message());
    }

    /**
     * SSE 流式对话。
     *
     * <p>事件协议（data 均为单行 JSON，前端按「累计快照」渲染）：
     * <pre>
     * event: start   data: {"agent":"pet","conversationId":"pet-xxx"}   // 立即下发，用于冲刷响应头
     * event: delta   data: {"delta":"增量文本"}                          // 0..N 条
     * event: done    data: {"agent":"pet"}                              // 正常结束
     * event: error   data: {"message":"错误信息"}                        // 序列化等极端情况
     * </pre>
     */
    @PostMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sseChat(@RequestBody ChatRequest request) {
        String agent = request.agentOrDefault();
        String conversationId = request.conversationIdOrDefault();
        log.info("sseChat start, agent={}, conversationId={}", agent, conversationId);

        Flux<String> start = Flux.just(frame("start", Map.of("agent", agent, "conversationId", conversationId)));
        Flux<String> deltas = chatService.chatStream(agent, conversationId, request.message())
                .map(chunk -> frame("delta", Map.of("delta", chunk)))
                .onErrorResume(e -> {
                    log.error("sseChat stream error, agent={}, conversationId={}", agent, conversationId, e);
                    return Flux.just(frame("error", Map.of("message", "对话出错了，请稍后再试")));
                });
        Flux<String> done = Flux.just(frame("done", Map.of("agent", agent)));

        return Flux.concat(start, deltas, done)
                .doOnCancel(() -> log.info("sseChat client disconnected, agent={}, conversationId={}", agent, conversationId))
                .doOnError(e -> log.error("sseChat error, agent={}, conversationId={}", agent, conversationId, e));
    }

    /**
     * 组装一帧 SSE：data 统一走 JSON，避免正文里的换行 / 引号破坏以空行分帧的协议
     */
    private String frame(String event, Object data) {
        try {
            return "event: " + event + "\ndata: " + MAPPER.writeValueAsString(data) + "\n\n";
        } catch (JacksonException e) {
            log.error("sse frame serialize error, event={}", event, e);
            return "event: error\ndata: {\"message\":\"服务端序列化失败\"}\n\n";
        }
    }
}

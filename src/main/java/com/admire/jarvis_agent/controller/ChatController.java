package com.admire.jarvis_agent.controller;

import com.admire.jarvis_agent.dto.ChatRequest;
import com.admire.jarvis_agent.dto.ResponseVO;
import com.admire.jarvis_agent.service.ChatService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description 对话
 * @Author Liu Yang
 * @Date 2026/9/4 09:07
 */
@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    /** 服务端建议的重连间隔（毫秒），编码为每帧 retry：客户端断线按此等待后重连 */
    private static final long RETRY_MILLIS = 3000L;

    private static final String ERROR_MESSAGE = "对话出错了，请稍后再试";

    @Resource
    private ChatService chatService;

    /**
     * 单轮对话（阻塞，调试用；正式链路走 /chat/sse）
     */
    @PostMapping
    public ResponseVO<String> chat(@RequestBody ChatRequest request) {
        try {
            String answer = chatService.chat(request.agentOrDefault(), request.conversationIdOrDefault(), request.message());
            return ResponseVO.ok(answer);
        } catch (Exception e) {
            log.error("chat error, message={}", request.message(), e);
            return ResponseVO.error(500, ERROR_MESSAGE);
        }
    }

    /**
     * SSE 流式对话（标准 SSE 协议，WebMVC + {@link SseEmitter}）。
     *
     * <p>每帧含标准字段（W3C EventSource），帧间以空行分隔：
     * <pre>
     * id:1
     * event:start
     * retry:3000
     * data:{"agent":"jarvis","conversationId":"jarvis-xxx"}
     *
     * id:2
     * event:delta
     * data:{"delta":"你好"}
     * </pre>
     * 正常结束发 {@code done} 帧后关闭；出错发 {@code error} 帧即终止（终态，不再发 done）。
     *
     * <p>字段语义：
     * <ul>
     *   <li>{@code id}：单调自增游标。客户端维护 Last-Event-ID，断线重连时随请求头带回，供断点续推；</li>
     *   <li>{@code event}：事件分类 start / delta / done / error，客户端按事件名分派；</li>
     *   <li>{@code retry}：服务端建议的重连等待毫秒数；</li>
     *   <li>{@code data}：单行 JSON（换行经序列化转义，不破坏帧边界）。</li>
     * </ul>
     *
     * <p>实现注：WebMVC 下 SseEmitter 是标准 SSE 的正统载体（基于 Servlet 3.1 异步，
     * 推送不占请求线程）。不可直接返回 {@code Flux<ServerSentEvent>}——那是 WebFlux 的
     * 编码器类型，MVC 栈不识别，元素只会被包成裸 {@code data:}，带不出 id / event / retry。
     *
     * <p>响应头：{@code text/event-stream}（SSE 必须）、{@code Cache-Control: no-cache}、
     * {@code X-Accel-Buffering: no}（关闭 Nginx 等代理缓冲，保证逐帧即时到达）。
     */
    @PostMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> sseChat(@RequestBody ChatRequest request) {
        String agent = request.agentOrDefault();
        String conversationId = request.conversationIdOrDefault();
        log.info("sseChat start, agent={}, conversationId={}", agent, conversationId);

        SseEmitter emitter = new SseEmitter(0L);
        AtomicInteger seq = new AtomicInteger();

        Flux<SseEmitter.SseEventBuilder> frames = Flux.concat(
                        // defer：让 frame() 的 id 分配发生在订阅/消费时而非 Flux 组装时，
                        // 否则 done 帧会在构造期抢先拿到小 id，造成游标乱序（实测见 done id < delta id）
                        Flux.defer(() -> Flux.just(frame(seq, "start", Map.of("agent", agent, "conversationId", conversationId)))),
                        chatService.chatStream(agent, conversationId, request.message())
                                .map(text -> frame(seq, "delta", Map.of("delta", text == null ? "" : text))),
                        Flux.defer(() -> Flux.just(frame(seq, "done", Map.of("agent", agent))))
                )
                // 流内错误统一收敛为 error 帧收尾（error 为终态，其后不再发 done）
                .onErrorResume(e -> {
                    log.error("sseChat terminal error, agent={}, conversationId={}", agent, conversationId, e);
                    return Flux.just(frame(seq, "error", Map.of("message", ERROR_MESSAGE)));
                });

        Disposable subscription = frames.subscribe(
                event -> safeSend(emitter, event),
                // 正常不可达：流内错误已被上方 onErrorResume 收敛为 error 帧
                error -> emitter.completeWithError(error),
                emitter::complete
        );

        emitter.onCompletion(subscription::dispose);
        emitter.onError(error -> {
            log.error("sseChat emitter error, agent={}, conversationId={}", agent, conversationId, error);
            subscription.dispose();
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_EVENT_STREAM);
        headers.setCacheControl(CacheControl.noCache());
        headers.add("X-Accel-Buffering", "no");
        return ResponseEntity.ok().headers(headers).body(emitter);
    }

    /** 构建标准 SSE 帧：id（自增游标）+ event（事件名）+ retry（重连间隔）+ data（业务载荷） */
    private static SseEmitter.SseEventBuilder frame(AtomicInteger seq, String event, Object data) {
        return SseEmitter.event()
                .id(String.valueOf(seq.incrementAndGet()))
                .name(event)
                .reconnectTime(RETRY_MILLIS)
                .data(data);
    }

    private void safeSend(SseEmitter emitter, SseEmitter.SseEventBuilder event) {
        try {
            emitter.send(event);
        } catch (IOException | IllegalStateException e) {
            emitter.completeWithError(e);
        }
    }
}

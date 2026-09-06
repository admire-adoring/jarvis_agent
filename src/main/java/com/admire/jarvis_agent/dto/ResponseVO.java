package com.admire.jarvis_agent.dto;

/**
 * 统一响应包装：普通 REST 接口的全站响应契约。
 *
 * <p>SSE 流式接口不使用本类型：见 {@link com.admire.jarvis_agent.controller.ChatController#sseChat}，
 * 由 {@code SseEmitter} 逐帧发送标准 SSE 事件（id / event / retry / data，载荷为轻量 Map）。
 *
 * @param <T> 业务数据类型
 */
public record ResponseVO<T>(
        /**
         * 状态码：0 成功；非 0 失败（与 HTTP 状态码解耦，业务/系统错误统一走 body）
         */
        int code,

        /**
         * 兼容字段（历史 SSE 契约遗留）：成功时恒为 null；{@link #error(int, String)} 置 "error"
         */
        String type,

        /**
         * 提示信息（失败时为错误原因）
         */
        String message,

        /**
         * 业务数据
         */
        T data
) {

    /** 普通接口成功 */
    public static <T> ResponseVO<T> ok(T data) {
        return new ResponseVO<>(0, null, null, data);
    }

    /** 失败响应：code≠0、message 为错误原因；泛型按调用处推断 */
    public static <T> ResponseVO<T> error(int code, String message) {
        return new ResponseVO<>(code, "error", message, null);
    }
}

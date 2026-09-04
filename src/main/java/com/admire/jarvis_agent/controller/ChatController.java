package com.admire.jarvis_agent.controller;

import com.admire.jarvis_agent.service.ChatService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 对话
 * @Author Liu Yang
 * @Date 2026/9/4 09:07
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public String chat(@RequestParam String message,
                       @RequestParam(required = false, defaultValue = "default") String conversationId) {
        return chatService.chat(conversationId, message);
    }
}

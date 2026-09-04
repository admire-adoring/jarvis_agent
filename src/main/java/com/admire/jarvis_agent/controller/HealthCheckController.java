package com.admire.jarvis_agent.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description
 * @Author Liu Yang
 * @Date 2026/9/4 09:01
 */
@RestController
@RequestMapping("/health")
public class HealthCheckController {

    @GetMapping
    public String healehCheck(){
        return "OK";
    }
}

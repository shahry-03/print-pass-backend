package com.printpass.gateway.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    // 🔥 Ab yeh GET, POST, PUT sab ko handle karega!
    @RequestMapping("/auth")
    public Mono<Map<String, String>> authFallback() {
        return Mono.just(Map.of("message", "Auth service is currently down. Please try later."));
    }

    @RequestMapping("/user")
    public Mono<Map<String, String>> userFallback() {
        return Mono.just(Map.of("message", "User service is currently down. Please try later."));
    }

    @RequestMapping("/job")
    public Mono<Map<String, String>> jobFallback() {
        return Mono.just(Map.of("message", "Job service is currently down. Please try later."));
    }
}
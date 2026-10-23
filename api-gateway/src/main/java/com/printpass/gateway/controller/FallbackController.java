package com.printpass.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    // 🔥 GET, POST, PUT sab handle karega
    @RequestMapping("/auth")
    public Mono<ResponseEntity<Map<String, Object>>> authFallback(ServerWebExchange exchange) {
        return buildFallbackResponse(exchange, "Authentication Service", "AUTH-SERVICE");
    }

    @RequestMapping("/user")
    public Mono<ResponseEntity<Map<String, Object>>> userFallback(ServerWebExchange exchange) {
        return buildFallbackResponse(exchange, "User Management Service", "USER-SERVICE");
    }

    @RequestMapping("/job")
    public Mono<ResponseEntity<Map<String, Object>>> jobFallback(ServerWebExchange exchange) {
        return buildFallbackResponse(exchange, "Print Job Service", "JOB-SERVICE");
    }

    /**
     * Helper Method: Consistent fallback response banata hai.
     * Isse code duplicate nahi hota (DRY Principle).
     */
    private Mono<ResponseEntity<Map<String, Object>>> buildFallbackResponse(
            ServerWebExchange exchange, String serviceName, String serviceId) {

        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();

        // 🪵 AHEM: Production logs mein yeh entry zaroori hai debugging ke liye
        log.error("🛑 CIRCUIT BREAKER TRIGGERED | Service: {} | Request: {} {} | Possible reasons: Timeout, Service Down, or Network Issue",
                serviceId, method, path);

        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("timestamp", LocalDateTime.now().toString());
        errorBody.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        errorBody.put("error", "Service Unavailable");
        errorBody.put("service", serviceId);
        errorBody.put("message", serviceName + " is currently down. Please try again in a few moments.");
        errorBody.put("path", path);
        errorBody.put("method", method);

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorBody));
    }
}
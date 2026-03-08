package com.printpass.gateway.filter;

import com.printpass.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Agar request secured raste par hai...
            if (validator.isSecured.test(request)) {

                // 1. Check karein kya header mein 'Authorization' (Token) aya hai?
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    // 🚀 UPGRADE 1: Crash hone ke bajaye polite JSON error bhejein
                    return onError(exchange, HttpStatus.UNAUTHORIZED, "Error: ID Card (Token) kahan hai? Andar jana mana hai!");
                }

                // 2. Token ko nikalen
                String authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7); // "Bearer " lafz ko hata kar sirf token pakrein
                } else {
                    return onError(exchange, HttpStatus.UNAUTHORIZED, "Error: Ghalat Token Format!");
                }

                // 3. Token verify karein aur data aage bhejein
                try {
                    jwtUtil.validateToken(authHeader);

                    // (Optional) Agar aapke JwtUtil mein userId nikalne ka method hai:
                    // String userId = jwtUtil.extractUserId(authHeader);

                    // 🚀 UPGRADE 2: Header Mutation (Downstream services ke liye chit lagana)
                    // Ab Job Service ko dobara token verify nahi karna parega!
                    request = exchange.getRequest()
                            .mutate()
                            // .header("X-User-Id", userId) // Isey un-comment kar lein jab JwtUtil mein extract method add kar lein
                            .build();

                } catch (Exception e) {
                    return onError(exchange, HttpStatus.UNAUTHORIZED, "Error: Nakli ya Expire Token! Unauthorized access.");
                }
            }

            // 4. Sab theek hai, user ko nayi (mutated) request ke sath andar jane do
            return chain.filter(exchange.mutate().request(request).build());
        });
    }

    public static class Config {
        // Yahan configuration aati hai, filhal iski zaroorat nahi
    }

    // ── 🚀 THE NEW HELPER METHOD (App ko 500 Error se bachane ke liye) ──
    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");

        // Proper JSON format mein error message banayein
        String jsonResponse = String.format("{\"error\": \"%s\"}", message);
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);

        var buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
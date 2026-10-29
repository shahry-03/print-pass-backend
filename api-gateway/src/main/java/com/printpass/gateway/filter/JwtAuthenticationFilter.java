package com.printpass.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.util.AntPathMatcher;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.error("Invalid token: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        // 🔥 NAYA CODE: CORS ki pre-flight (OPTIONS) requests ko hamesha pass hone do
        if (request.getMethod().name().equals("OPTIONS")) {
            return chain.filter(exchange);
        }
        String path = request.getURI().getPath();

        // Public endpoints — no token required
        List<String> publicPaths = List.of(
                "/api/v1/shops/register",
                "/api/v1/auth/login",
                "/api/v1/otp/verify",
                "/api/v1/otp/resend",
                "/api/v1/jobs/upload",
                "/api/v1/jobs/reprint/**",
                "/api/v1/jobs/status/**",
                "/actuator/health",
                "/v3/api-docs/**",       // Swagger JSON
                "/swagger-ui/**",        // Swagger UI
                "/swagger-ui.html",      // Swagger HTML
                "/webjars/**"            // Swagger CSS/JS
        );

        boolean isPublic = publicPaths.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
        
        if (isPublic) {
            return chain.filter(exchange);
        }

        // Extract token from Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        if (!isTokenValid(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Extract claims and forward as headers to downstream services
        Claims claims = extractAllClaims(token);
        String userId = claims.get("userId", String.class);
        String role = claims.get("role", String.class);
        String email = claims.getSubject();

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-User-Id", userId)
                .header("X-User-Role", role)
                .header("X-User-Email", email)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -1; // Run before other filters
    }
}
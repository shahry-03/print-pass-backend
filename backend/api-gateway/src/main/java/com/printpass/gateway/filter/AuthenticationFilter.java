package com.printpass.gateway.filter;

import com.printpass.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

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
            // Agar request us raste par ja rahi hai jo secured hai...
            if (validator.isSecured.test(exchange.getRequest())) {

                // 1. Check karein kya header mein 'Authorization' (Token) aya hai?
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new RuntimeException("Error: ID Card (Token) kahan hai? Andar jana mana hai!");
                }

                // 2. Token ko nikalen
                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7); // "Bearer " lafz ko hata kar sirf token pakrein
                }

                // 3. Token ko check (verify) karein
                try {
                    jwtUtil.validateToken(authHeader);
                } catch (Exception e) {
                    throw new RuntimeException("Error: Nakli ya Expire Token! Unauthorized access.");
                }
            }
            // Sab theek hai, user ko andar jane do
            return chain.filter(exchange);
        });
    }

    public static class Config {
        // Yahan configuration aati hai, filhal iski zaroorat nahi
    }
}
package com.printpass.gateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    // Yeh wo raste hain jahan bina ID Card (Token) ke ja sakte hain (Open Endpoints)
    public static final List<String> openApiEndpoints = List.of(
            "/api/users/login",
            "/api/users/register",
            "/eureka"
    );

    // Yeh function check karega ke request open raste par hai ya secured par
    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
}
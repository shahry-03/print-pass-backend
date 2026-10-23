package com.printpass.authservice.config; // (apne package ke hisaab se badlein)

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // 1. Server URL ko API Gateway par set karein
        Server gatewayServer = new Server();
        gatewayServer.setUrl("http://localhost:8080"); 
        gatewayServer.setDescription("API Gateway Server");

        // 2. JWT Token ke liye 'Authorize' button add karein
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .servers(List.of(gatewayServer))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
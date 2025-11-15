package com.thilina.WorkingTimeApplication.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Working Time Application API",
                version = "1.0",
                description = "REST API for Working Time Management System",
                contact = @Contact(
                        name = "Thilina",
                        email = "support@workingtime.com"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local Development Server"),
                @Server(url = "https://api.workingtime.com", description = "Production Server")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}

package com.healthcare.management_system.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI healthcareManagementOpenApi() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Healthcare Management System API")
                        .description("API documentation for the final-year hospital management and universal EHR platform.")
                        .version("v1")
                        .contact(new Contact()
                                .name("Healthcare Management System")
                                .email("support@healthcare.local"))
                        .license(new License()
                                .name("Academic Use Only")))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Local development server")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the JWT access token received from /api/auth/login")));
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("auth")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi hospitalOperationsApi() {
        return GroupedOpenApi.builder()
                .group("hospital-operations")
                .pathsToMatch(
                        "/api/admin/**",
                        "/api/doctors/**",
                        "/api/patients/**",
                        "/api/appointments/**",
                        "/api/bills/**",
                        "/api/departments/**",
                        "/api/medical-records/**",
                        "/api/prescriptions/**"
                )
                .build();
    }

    @Bean
    public GroupedOpenApi platformAdministrationApi() {
        return GroupedOpenApi.builder()
                .group("platform-administration")
                .pathsToMatch("/api/superadmin/**")
                .build();
    }
}

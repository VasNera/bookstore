package com.neratzis.bookstore.core;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration


@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)

public class OpenApiConfig{

    @Bean
    public OpenAPI customOpenAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("Bookstore API")
                        .version("1.0.0")
                        .description("""
                REST API for an online store selling books, music, toys and stationery.
                Two roles with JWT-based authentication:
                - **Customer** — registers, browses and filters products, manages a
                  server-side cart, places and cancels orders, writes reviews and keeps favourites
                - **Admin** — manages categories and products, and processes orders
                  (order status and payment status)

                Key features: capability-based authorization, product catalogue with
                JPA inheritance (Book, Music, Toy, Stationery), dynamic filtering with
                Spring Data Specifications, and optimistic locking on stock to prevent
                overselling at checkout.
                """));
    }

    @Bean
    public OperationCustomizer globalSecurityResponses(){
        return (operation, handlerMethod) -> {
            boolean isSecured = handlerMethod.hasMethodAnnotation(SecurityRequirement.class)
                    || handlerMethod.getBeanType().isAnnotationPresent(SecurityRequirement.class);


            if (isSecured){
                operation.getResponses()
                        .addApiResponse("401", new ApiResponse()
                                .description("Unauthorized. Jwt token is missing or invalid"))
                        .addApiResponse("403", new ApiResponse()
                                .description("Forbidden. You don't have permission to access this resource"));
            }
            return operation;

        };
    }

}

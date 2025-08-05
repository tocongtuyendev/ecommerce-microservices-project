package com.yourcompany.ecommerce.gateway.security;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.Arrays; // Import thư viện Arrays
import java.util.List;
import java.util.function.Predicate;

@Component
public class RouterValidator {

        // Sử dụng Arrays.asList() thay vì List.of() để tương thích với Java 8
        public static final List<String> openApiEndpoints = Arrays.asList(
                        "/api/auth/signup",
                        "/api/auth/signin");

        public Predicate<ServerHttpRequest> isSecured = request -> openApiEndpoints
                        .stream()
                        .noneMatch(uri -> request.getURI().getPath().contains(uri));
}
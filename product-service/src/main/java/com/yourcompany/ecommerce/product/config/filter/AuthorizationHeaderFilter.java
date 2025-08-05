package com.yourcompany.ecommerce.product.config.filter;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AuthorizationHeaderFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String username = request.getHeader("X-Username");
        String rolesHeader = request.getHeader("X-User-Roles");

        if (username != null && rolesHeader != null && !rolesHeader.isEmpty()) {
            // Logic xử lý chuỗi mới, mạnh mẽ hơn
            // Input: ""
            // 1. Bỏ dấu ngoặc: "ROLE_ADMIN, ROLE_USER"
            String cleanedRoles = rolesHeader.substring(1, rolesHeader.length() - 1);

            // 2. Tách chuỗi và loại bỏ khoảng trắng ở mỗi phần tử
            List<SimpleGrantedAuthority> authorities = Arrays.stream(cleanedRoles.split(","))
                    .map(String::trim) // Loại bỏ khoảng trắng thừa
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    username, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

}

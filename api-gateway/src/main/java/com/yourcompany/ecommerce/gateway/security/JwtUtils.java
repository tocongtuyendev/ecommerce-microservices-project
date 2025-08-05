package com.yourcompany.ecommerce.gateway.security;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    public void validateJwtToken(String authToken) {
        // Chúng ta sẽ để phương thức này ném ra ngoại lệ nếu token không hợp lệ
        // và bộ lọc sẽ bắt lại
        Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
    }
}
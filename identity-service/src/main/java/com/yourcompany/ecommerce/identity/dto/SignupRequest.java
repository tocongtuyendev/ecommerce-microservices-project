package com.yourcompany.ecommerce.identity.dto;

import lombok.Data;
import java.util.Set;

@Data
public class SignupRequest {
    private String username;
    private String password;
    private Set<String> roles;
}

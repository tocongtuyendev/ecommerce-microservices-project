package com.yourcompany.ecommerce.identity.dto;

import lombok.Data;
import java.util.Set;
import javax.validation.constraints.NotBlank;

@Data
public class SignupRequest {
    @NotBlank(message = "username is required")
    private String username;

    @NotBlank(message = "password is required")
    private String password;

    private Set<String> roles;
}

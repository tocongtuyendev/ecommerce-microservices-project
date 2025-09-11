package com.yourcompany.ecommerce.identity.controller;

import com.yourcompany.ecommerce.common.response.ApiResponse;
import com.yourcompany.ecommerce.identity.config.RabbitMQConfig;
import com.yourcompany.ecommerce.identity.dto.JwtResponse;
import com.yourcompany.ecommerce.identity.dto.LoginRequest;
import com.yourcompany.ecommerce.identity.dto.SignupRequest;
import com.yourcompany.ecommerce.identity.model.ERole;
import com.yourcompany.ecommerce.identity.model.Role;
import com.yourcompany.ecommerce.identity.model.User;
import com.yourcompany.ecommerce.identity.repository.RoleRepository;
import com.yourcompany.ecommerce.identity.repository.UserRepository;
import com.yourcompany.ecommerce.identity.security.jwt.JwtUtils;
import com.yourcompany.ecommerce.identity.security.services.UserDetailsImpl;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.yourcompany.ecommerce.common.event.SellerProfileCreateEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<JwtResponse>> authenticateUser(@RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());
        JwtResponse jwtResponse = new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                roles);
        return ResponseEntity.ok(ApiResponse.success("Login success", jwtResponse));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Object>> registerUser(@RequestBody SignupRequest signUpRequest) {
        if (userRepository.findByUsername(signUpRequest.getUsername()).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error(400, "Error: Username is already taken!"));
        }

        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                        break;
                    case "seller": // <-- Thêm case mới cho "seller"
                        Role sellerRole = roleRepository.findByName(ERole.ROLE_SELLER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(sellerRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("User registered successfully!", null));
    }
    @PostMapping("/register-seller")
    @PreAuthorize("hasRole('USER')") // Chỉ người dùng có vai trò USER mới được đăng ký
    public ResponseEntity<ApiResponse<Object>> registerAsSeller(Authentication authentication) {
        // 1. Lấy thông tin người dùng hiện tại từ token
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        // 2. Lấy vai trò SELLER từ database
        Role sellerRole = roleRepository.findByName(ERole.ROLE_SELLER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));

        // 3. Thêm vai trò SELLER cho người dùng
        user.getRoles().add(sellerRole);
        userRepository.save(user);

        // 4. Tạo và phát sự kiện để thông báo cho các service khác
        SellerProfileCreateEvent event = new SellerProfileCreateEvent(user.getId(), user.getUsername());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_SELLER_REGISTERED, event);

        return ResponseEntity.ok(ApiResponse.success("Seller registration request received. Your profile is being created.", null));
    }
}
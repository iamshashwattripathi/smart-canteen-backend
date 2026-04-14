package com.smartcanteen.dto;

import lombok.*;
import java.util.List;

// ── Auth DTOs ────────────────────────────────────────────────

@Data @NoArgsConstructor @AllArgsConstructor
class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String phone;
}

@Data @NoArgsConstructor @AllArgsConstructor
class LoginRequest {
    private String email;
    private String password;
}

@Data @Builder
class AuthResponse {
    private String token;
    private String name;
    private String email;
    private String role;
    private double walletBalance;
}

package com.inkloom.controller;

import com.inkloom.dto.request.LoginRequest;
import com.inkloom.dto.request.RegisterRequest;
import com.inkloom.dto.response.ApiResponse;
import com.inkloom.dto.response.AuthResponse;
import com.inkloom.dto.response.UserResponse;
import com.inkloom.exception.UnauthorizedAccessException;
import com.inkloom.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Authentication successful", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {

        if (authHeader != null && !authHeader.isBlank()) {
            UserResponse user = authService.getUserByToken(authHeader);
            return ResponseEntity.ok(ApiResponse.ok(user));
        } else if (userIdHeader != null && !userIdHeader.isBlank()) {
            UserResponse user = authService.getUserById(userIdHeader);
            return ResponseEntity.ok(ApiResponse.ok(user));
        }

        throw new UnauthorizedAccessException("Missing authentication token or user header");
    }
}

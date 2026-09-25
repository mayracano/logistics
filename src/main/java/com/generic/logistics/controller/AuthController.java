package com.generic.logistics.controller;

import com.generic.logistics.dto.AuthResponse;
import com.generic.logistics.dto.LoginRequest;
import com.generic.logistics.model.User;
import com.generic.logistics.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Perimeter access endpoints and user registration in the logistics system.")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Start Session", description = "Verifies credentials and returns a dynamic role-based JWT Bearer Token.")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("REST hit: Login attempt for email {}", request.email());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Public endpoint for registering profiles for Customers, Drivers, or Administrators.")
    public ResponseEntity<User> register(@Valid @RequestBody User user) {
        log.info("REST hit: Registration request for email {}", user.getEmail());
        User registeredUser = authService.register(user);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }
}

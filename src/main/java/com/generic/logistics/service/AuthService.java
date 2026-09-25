package com.generic.logistics.service;

import com.generic.logistics.dto.AuthResponse;
import com.generic.logistics.dto.LoginRequest;
import com.generic.logistics.model.User;
import com.generic.logistics.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Processing authentication logic for user: {}", request.email());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + request.email()));

        Map<String, Object> extraClaims = Map.of("role", user.getRole().name());

        String jwtToken = jwtService.generateToken(extraClaims, user.getEmail());

        log.info("Token successfully generated for user {} with role {}", user.getEmail(), user.getRole());
        return new AuthResponse(jwtToken);
    }

    @Transactional
    public User register(User user) {
        log.info("Registering new user profile in database: {}", user.getEmail());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }
}

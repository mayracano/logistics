package com.generic.logistics.service;

import com.generic.logistics.dto.AuthResponse;
import com.generic.logistics.dto.LoginRequest;
import com.generic.logistics.model.Role;
import com.generic.logistics.model.User;
import com.generic.logistics.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private AuthService authService;

    @Test
    void shouldLoginSuccessfullyAndReturnToken() {
        LoginRequest request = new LoginRequest("admin@logistics.com", "password123");
        User mockUser = new User();
        mockUser.setEmail("admin@logistics.com");
        mockUser.setRole(Role.ADMIN);

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(mockUser));
        when(jwtService.generateToken(anyMap(), anyString())).thenReturn("mocked.jwt.token");

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("mocked.jwt.token");
        assertThat(response.tokenType()).isEqualTo("Bearer");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundDuringLogin() {
        LoginRequest request = new LoginRequest("unknown@logistics.com", "password123");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found: unknown@logistics.com");

        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    void shouldRegisterNewUserWithEncodedPassword() {
        User rawUser = new User();
        rawUser.setEmail("driver@logistics.com");
        rawUser.setPassword("rawPassword");

        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userRepository.save(rawUser)).thenReturn(rawUser);

        User savedUser = authService.register(rawUser);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
        verify(userRepository).save(rawUser);
    }
}

package com.generic.logistics.config;

import com.generic.logistics.model.Role;
import com.generic.logistics.model.User;
import com.generic.logistics.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SecurityConfig securityConfig;

    @Test
    void shouldBuildSecurityFilterChainSuccessfully() throws Exception {
        HttpSecurity httpSecurity = mock(HttpSecurity.class);
        DefaultSecurityFilterChain mockChain = new DefaultSecurityFilterChain(
                mock(org.springframework.security.web.util.matcher.RequestMatcher.class),
                new java.util.ArrayList<>()
        );

        var csrfConfigurer = mock(CsrfConfigurer.class, RETURNS_DEEP_STUBS);
        var authConfigurer = mock(AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry.class, org.mockito.Answers.RETURNS_DEEP_STUBS);
        var sessionConfigurer = mock(SessionManagementConfigurer.class, RETURNS_DEEP_STUBS);

        doAnswer(inv -> {
            Customizer customizer = inv.getArgument(0);
            customizer.customize(csrfConfigurer);
            return httpSecurity;
        }).when(httpSecurity).csrf(any());

        doAnswer(inv -> {
            Customizer customizer = inv.getArgument(0);
            customizer.customize(authConfigurer);
            return httpSecurity;
        }).when(httpSecurity).authorizeHttpRequests(any());

        doAnswer(inv -> {
            Customizer customizer = inv.getArgument(0);
            customizer.customize(sessionConfigurer);
            return httpSecurity;
        }).when(httpSecurity).sessionManagement(any());

        when(httpSecurity.authenticationProvider(any(AuthenticationProvider.class))).thenReturn(httpSecurity);
        when(httpSecurity.addFilterBefore(any(), any())).thenReturn(httpSecurity);
        when(httpSecurity.build()).thenReturn(mockChain);

        SecurityFilterChain filterChain = securityConfig.securityFilterChain(httpSecurity);

        assertThat(filterChain).isNotNull();
    }


    @Test
    void shouldLoadUserByUsernameSuccessfully() {
        UserDetailsService userDetailsService = securityConfig.userDetailsService(userRepository);

        User mockUser = new User();
        mockUser.setEmail("test@logistics.com");
        mockUser.setPassword("hashedPassword");
        mockUser.setRole(Role.DRIVER);

        when(userRepository.findByEmail("test@logistics.com")).thenReturn(Optional.of(mockUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("test@logistics.com");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("test@logistics.com");
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_DRIVER");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        UserDetailsService userDetailsService = securityConfig.userDetailsService(userRepository);
        when(userRepository.findByEmail("empty@logistics.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("empty@logistics.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found in database: empty@logistics.com");
    }

    @Test
    void shouldCreateAuthenticationProvider() {
        UserDetailsService userDetailsService = mock(UserDetailsService.class);
        AuthenticationProvider provider = securityConfig.authenticationProvider(userDetailsService);
        assertThat(provider).isNotNull();
    }

    @Test
    void shouldCreatePasswordEncoder() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        assertThat(encoder).isNotNull();
    }

    @Test
    void shouldCreateAuthenticationManager() {
        AuthenticationProvider provider = mock(AuthenticationProvider.class);
        AuthenticationManager manager = securityConfig.authenticationManager(provider);
        assertThat(manager).isNotNull();
    }
}
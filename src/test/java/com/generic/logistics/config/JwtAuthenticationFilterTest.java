package com.generic.logistics.config;

import com.generic.logistics.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtService jwtService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldFullyExecuteFilterAndAuthenticateUser() throws Exception {
        String mockToken = "valid.jwt.token";
        String userEmail = "driver@logistics.com";

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + mockToken);
        when(jwtService.extractUsername(mockToken)).thenReturn(userEmail);
        when(jwtService.isTokenValid(mockToken, userEmail)).thenReturn(true);
        when(jwtService.extractRole(mockToken)).thenReturn("DRIVER");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo(userEmail);
        assertThat(authentication.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_DRIVER");

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldReturnImmediatelyWhenHeaderIsNull() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldReturnImmediatelyWhenHeaderDoesNotStartWithBearer() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic dXNlcjpwYXNz");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldNotAuthenticateWhenExtractedUsernameIsNull() throws Exception {
        String mockToken = "malformed.token";

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + mockToken);
        when(jwtService.extractUsername(mockToken)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldSkipAuthenticationIfUserIsAlreadyAuthenticatedInContext() throws Exception {
        String mockToken = "valid.token";
        String userEmail = "admin@logistics.com";

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + mockToken);
        when(jwtService.extractUsername(mockToken)).thenReturn(userEmail);

        var existingAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "previous-user", null, Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService, never()).isTokenValid(anyString(), anyString());
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("previous-user");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldLogWarningWhenTokenIsInvalid() throws Exception {
        String mockToken = "invalid.token";
        String userEmail = "driver@logistics.com";

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + mockToken);
        when(jwtService.extractUsername(mockToken)).thenReturn(userEmail);
        when(jwtService.isTokenValid(mockToken, userEmail)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldCatchAndLogExceptionWhenTokenProcessingFails() throws Exception {
        String mockToken = "corrupted.token";

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + mockToken);
        when(jwtService.extractUsername(mockToken)).thenThrow(new RuntimeException("Cryptographic signature failure"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldReturnTrueWhenRequestIsForSwaggerDocumentationPaths() throws Exception {
        when(request.getRequestURI()).thenReturn("/v3/api-docs/swagger-config");
        boolean shouldSkipFirst = jwtAuthenticationFilter.shouldNotFilter(request);

        when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");
        boolean shouldSkipSecond = jwtAuthenticationFilter.shouldNotFilter(request);

        when(request.getRequestURI()).thenReturn("/swagger-ui.html");
        boolean shouldSkipThird = jwtAuthenticationFilter.shouldNotFilter(request);

        assertThat(shouldSkipFirst).isTrue();
        assertThat(shouldSkipSecond).isTrue();
        assertThat(shouldSkipThird).isTrue();
    }

    @Test
    void shouldReturnFalseWhenRequestIsForBusinessEndpoints() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/shipments");
        boolean shouldSkip = jwtAuthenticationFilter.shouldNotFilter(request);

        assertThat(shouldSkip).isFalse();
    }
}

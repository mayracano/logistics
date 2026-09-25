package com.generic.logistics.exception;

import com.generic.logistics.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void shouldHandleValidationErrorsSuccessfully() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError error1 = new FieldError("object", "field1", "Field1 is required");
        FieldError error2 = new FieldError("object", "field2", "Field2 must be positive");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(error1, error2));
        when(request.getRequestURI()).thenReturn("/api/shipments");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationErrors(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
        assertThat(response.getBody().message()).contains("Field1 is required", "Field2 must be positive");
        assertThat(response.getBody().path()).isEqualTo("/api/shipments");
    }

    @Test
    void shouldHandleBadCredentialsSuccessfully() {
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBadCredentials(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(401);
        assertThat(response.getBody().error()).isEqualTo("Unauthorized");
        assertThat(response.getBody().message()).isEqualTo("Invalid email or password");
        assertThat(response.getBody().path()).isEqualTo("/api/auth/login");
    }

    @Test
    void shouldHandleUserNotFoundSuccessfully() {
        UsernameNotFoundException exception = new UsernameNotFoundException("User not found with email: test@email.com");
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleUserNotFound(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().error()).isEqualTo("Not Found");
        assertThat(response.getBody().message()).isEqualTo("User not found with email: test@email.com");
        assertThat(response.getBody().path()).isEqualTo("/api/auth/login");
    }

    @Test
    void shouldHandleDuplicateResourceException() {
        DuplicateResourceException exception = new DuplicateResourceException("User with email unknown@logistics.com already exists.");
        when(request.getRequestURI()).thenReturn("/api/users");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleDuplicateResourceException(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertEquals(HttpStatus.CONFLICT.getReasonPhrase(), response.getBody().error());
        assertEquals("User with email unknown@logistics.com already exists.", response.getBody().message());
        assertEquals("/api/users", response.getBody().path());
    }
}

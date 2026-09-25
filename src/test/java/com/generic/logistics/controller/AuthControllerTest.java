package com.generic.logistics.controller;


import com.generic.logistics.dto.AuthResponse;
import com.generic.logistics.dto.LoginRequest;
import com.generic.logistics.model.Role;
import com.generic.logistics.model.User;
import com.generic.logistics.service.AuthService;
import com.generic.logistics.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void shouldLoginSuccessfullyAndReturnToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest("admin@logistics.com", "password123");
        AuthResponse mockResponse = new AuthResponse("mocked.jwt.token");

        when(authService.login(any(LoginRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked.jwt.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void shouldRegisterNewUserSuccessfully() throws Exception {
        User newUser = new User();
        newUser.setFirstName("John");
        newUser.setLastName("Doe");
        newUser.setEmail("john.doe@logistics.com");
        newUser.setPassword("rawPassword123");
        newUser.setPhone("1234567890");
        newUser.setRole(Role.CUSTOMER);

        User mockSavedUser = new User();
        mockSavedUser.setId(1L);
        mockSavedUser.setFirstName("John");
        mockSavedUser.setLastName("Doe");
        mockSavedUser.setEmail("john.doe@logistics.com");
        mockSavedUser.setPhone("1234567890");
        mockSavedUser.setRole(Role.CUSTOMER);

        when(authService.register(any(User.class))).thenReturn(mockSavedUser);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john.doe@logistics.com"))
                .andExpect(jsonPath("$.role").value(Role.CUSTOMER.name()));
    }
}

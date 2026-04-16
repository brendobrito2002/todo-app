package com.myapp.todoapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myapp.todoapp.config.security.JwtAuthenticationFilter;
import com.myapp.todoapp.config.security.JwtUtil;
import com.myapp.todoapp.dto.LoginRequest;
import com.myapp.todoapp.dto.LoginResponse;
import com.myapp.todoapp.dto.RegisterRequest;
import com.myapp.todoapp.exception.InvalidCredentialsException;
import com.myapp.todoapp.exception.UserAlreadyExistsException;
import com.myapp.todoapp.service.AuthService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = AuthController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    )
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("register: deve retornar 200 e mensagem de sucesso")
    void register_success() throws Exception {
        RegisterRequest request = new RegisterRequest("João Silva", "joao@email.com", "senha123");

        doNothing().when(authService).register(any());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Usuário registrado com sucesso!"));
    }

    @Test
    @DisplayName("register: deve retornar 400 quando email já está em uso")
    void register_emailAlreadyInUse() throws Exception {
        RegisterRequest request = new RegisterRequest("João Silva", "joao@email.com", "senha123");

        doThrow(new UserAlreadyExistsException("Email já está em uso"))
                .when(authService).register(any());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email já está em uso"));
    }

    @Test
    @DisplayName("register: deve retornar 400 quando nome está em branco")
    void register_blankName() throws Exception {
        RegisterRequest request = new RegisterRequest("", "joao@email.com", "senha123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.name").exists());
    }

    @Test
    @DisplayName("register: deve retornar 400 quando email é inválido")
    void register_invalidEmail() throws Exception {
        RegisterRequest request = new RegisterRequest("João Silva", "email-invalido", "senha123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.email").exists());
    }

    @Test
    @DisplayName("register: deve retornar 400 quando senha tem menos de 6 caracteres")
    void register_shortPassword() throws Exception {
        RegisterRequest request = new RegisterRequest("João Silva", "joao@email.com", "123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.password").exists());
    }

    @Test
    @DisplayName("login: deve retornar 200 com token ao autenticar com sucesso")
    void login_success() throws Exception {
        LoginRequest request = new LoginRequest("joao@email.com", "senha123");
        LoginResponse loginResponse = new LoginResponse("token_gerado", "Bearer");

        when(authService.login(any())).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login realizado com sucesso"))
                .andExpect(jsonPath("$.data.accessToken").value("token_gerado"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("login: deve retornar 401 quando credenciais são inválidas")
    void login_invalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("joao@email.com", "senha_errada");

        doThrow(new InvalidCredentialsException("Email ou senha inválidos"))
                .when(authService).login(any());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email ou senha inválidos"));
    }

    @Test
    @DisplayName("login: deve retornar 400 quando email está em branco")
    void login_blankEmail() throws Exception {
        LoginRequest request = new LoginRequest("", "senha123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.email").exists());
    }

    @Test
    @DisplayName("login: deve retornar 400 quando senha está em branco")
    void login_blankPassword() throws Exception {
        LoginRequest request = new LoginRequest("joao@email.com", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.password").exists());
    }
}
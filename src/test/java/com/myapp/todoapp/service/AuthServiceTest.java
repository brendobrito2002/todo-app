package com.myapp.todoapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.myapp.todoapp.config.security.JwtUtil;
import com.myapp.todoapp.dto.LoginRequest;
import com.myapp.todoapp.dto.LoginResponse;
import com.myapp.todoapp.dto.RegisterRequest;
import com.myapp.todoapp.exception.InvalidCredentialsException;
import com.myapp.todoapp.exception.UserAlreadyExistsException;
import com.myapp.todoapp.model.entity.User;
import com.myapp.todoapp.model.enums.Role;
import com.myapp.todoapp.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("João Silva", "joao@email.com", "senha123");
        loginRequest = new LoginRequest("joao@email.com", "senha123");
        user = User.builder()
                .id(1L)
                .name("João Silva")
                .email("joao@email.com")
                .password("senha_encoded")
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("register: deve registrar usuário com sucesso")
    void register_success() {
        when(userRepository.findByEmail(registerRequest.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("senha_encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);

        assertThatNoException().isThrownBy(() -> authService.register(registerRequest));

        verify(userRepository).findByEmail(registerRequest.email());
        verify(passwordEncoder).encode(registerRequest.password());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register: deve lançar exceção quando email já está em uso")
    void register_emailAlreadyInUse() {
        when(userRepository.findByEmail(registerRequest.email())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Email já está em uso");

        verify(userRepository).findByEmail(registerRequest.email());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register: deve salvar senha encodada, nunca a senha em texto puro")
    void register_passwordMustBeEncoded() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha123")).thenReturn("senha_encoded");

        authService.register(registerRequest);

        verify(userRepository).save(argThat(savedUser ->
                savedUser.getPassword().equals("senha_encoded")
        ));
    }

    @Test
    @DisplayName("register: deve salvar usuário com role USER por padrão")
    void register_defaultRoleMustBeUser() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("senha_encoded");

        authService.register(registerRequest);

        verify(userRepository).save(argThat(savedUser ->
                savedUser.getRole() == Role.USER
        ));
    }

    @Test
    @DisplayName("login: deve retornar LoginResponse com token ao autenticar com sucesso")
    void login_success() {
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken(user)).thenReturn("token_gerado");

        LoginResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("token_gerado");
        assertThat(response.tokenType()).isEqualTo("Bearer");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateAccessToken(user);
    }

    @Test
    @DisplayName("login: deve lançar exceção quando credenciais são inválidas")
    void login_invalidCredentials() {
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Email ou senha inválidos");

        verify(jwtUtil, never()).generateAccessToken(any(User.class));
    }

    @Test
    @DisplayName("login: deve autenticar com o email e senha corretos")
    void login_mustAuthenticateWithCorrectCredentials() {
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken(user)).thenReturn("token_gerado");

        authService.login(loginRequest);

        verify(authenticationManager).authenticate(
                argThat(auth ->
                        auth instanceof UsernamePasswordAuthenticationToken token &&
                        token.getPrincipal().equals("joao@email.com") &&
                        token.getCredentials().equals("senha123")
                )
        );
    }
}
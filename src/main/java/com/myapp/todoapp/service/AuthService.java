package com.myapp.todoapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.myapp.todoapp.config.security.JwtUtil;
import com.myapp.todoapp.dto.LoginRequest;
import com.myapp.todoapp.dto.LoginResponse;
import com.myapp.todoapp.dto.RegisterRequest;
import com.myapp.todoapp.exception.InvalidCredentialsException;
import com.myapp.todoapp.exception.UserAlreadyExistsException;
import com.myapp.todoapp.model.entity.User;
import com.myapp.todoapp.model.enums.Role;
import com.myapp.todoapp.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    public void register(RegisterRequest request) {
        log.info("\nTentativa de registro para email: {}", request.email());

        if (userRepository.findByEmail(request.email()).isPresent()) {
            log.warn("\nRegistro falhou - email já em uso: {}", request.email());
            throw new UserAlreadyExistsException("Email já está em uso");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        userRepository.save(user);

        log.info("\nUsuário registrado com sucesso: {}", request.email());
    }

    public LoginResponse login(LoginRequest request) {
        log.info("\nTentativa de login: {}", request.email());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException e) {
            log.warn("\nLogin falhou: {}", request.email());
            throw new InvalidCredentialsException("Email ou senha inválidos");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Email ou senha inválidos"));

        String accessToken = jwtUtil.generateAccessToken(user);

        log.info("\nLogin bem-sucedido: {}", request.email());

        return new LoginResponse(accessToken, "Bearer");
    }
}

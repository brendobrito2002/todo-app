package com.myapp.todoapp.service;

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
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new UserAlreadyExistsException("Email já está em uso");
        }
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();
        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Email ou senha inválidos");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Email ou senha inválidos"));

        String accessToken = jwtUtil.generateAccessToken(user);

        return new LoginResponse(accessToken, "Bearer");
    }
}
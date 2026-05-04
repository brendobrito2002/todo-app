package com.myapp.todoapp.controller;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.myapp.todoapp.dto.ApiResponse;
import com.myapp.todoapp.dto.LoginRequest;
import com.myapp.todoapp.dto.LoginResponse;
import com.myapp.todoapp.dto.RegisterRequest;
import com.myapp.todoapp.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Usuário registrado com sucesso!", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse data = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login realizado com sucesso", data));
    }
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> me(Authentication authentication) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", authentication.getName());
        data.put("authorities", authentication.getAuthorities());
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}

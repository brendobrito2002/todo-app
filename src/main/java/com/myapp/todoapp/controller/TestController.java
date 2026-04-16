package com.myapp.todoapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/protected")
    public ResponseEntity<Map<String, Object>> protectedEndpoint(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Você acessou um endpoint protegido com sucesso!");
        response.put("email", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        response.put("authenticated", true);

        return ResponseEntity.ok(response);
    }
}
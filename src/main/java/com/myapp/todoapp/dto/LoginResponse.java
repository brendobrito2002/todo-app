package com.myapp.todoapp.dto;

public record LoginResponse(
        String accessToken,
        String tokenType
) {}
package com.myapp.todoapp.dto;

import jakarta.validation.constraints.*;

public record LoginRequest(
		@NotBlank(message = "O email é obrigatório")
		String email,
		
		@NotBlank(message = "A senha é obrigatório")
		String password
) {}
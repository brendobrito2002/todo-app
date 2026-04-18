package com.myapp.todoapp.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
		@NotBlank(message = "Nome é obrigatório")
		String name,
		
		String description
) {}

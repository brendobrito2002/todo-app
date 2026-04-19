package com.myapp.todoapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
		@NotBlank(message = "Nome é obrigatório")
		@Size(min = 5, max = 20, message = "O nome deve ter tamanho minimo de 5 e maximo de 20")
		String name,
		
		@Size(min = 5, max = 100, message = "A descrição deve ter tamanho minimo de 5 e maximo de 100")
		String description
) {}

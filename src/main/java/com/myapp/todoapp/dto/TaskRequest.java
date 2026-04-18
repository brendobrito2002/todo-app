package com.myapp.todoapp.dto;

import java.time.LocalDate;

import com.myapp.todoapp.model.enums.Priority;
import com.myapp.todoapp.model.enums.Status;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TaskRequest(
		@NotBlank(message = "Titulo é obrigatório")
		@Size(min = 1, max = 20, message = "O titulo deve ter tamanho minimo de 1 e maximo de 20")
		String title,
		
		String description,
		
		@NotNull(message = "Prazo é obrigatório")
		@FutureOrPresent(message = "O prazo deve ser definido como presente ou futuro")
		LocalDate dueDate,
		
		Status status,
		
		Priority priority
) {}

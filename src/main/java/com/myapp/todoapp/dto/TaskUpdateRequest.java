package com.myapp.todoapp.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.myapp.todoapp.model.enums.Priority;
import com.myapp.todoapp.model.enums.Status;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;

public record TaskUpdateRequest(
		@Size(min = 1, max = 20, message = "O titulo deve ter tamanho minimo de 1 e maximo de 20")
		String title,
		
		@Size(min = 5, max = 100, message = "A descrição deve ter tamanho minimo de 5 e maximo de 100")
		String description,
		
		@FutureOrPresent(message = "O prazo deve ser hoje ou no futuro")
		@JsonFormat(pattern = "dd/MM/yyyy")
		LocalDate dueDate,
		
		Status status,
		Priority priority,
		Long categoryId
) {}

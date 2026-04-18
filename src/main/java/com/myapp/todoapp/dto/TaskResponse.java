package com.myapp.todoapp.dto;

import java.time.LocalDate;

import com.myapp.todoapp.model.enums.Priority;
import com.myapp.todoapp.model.enums.Status;

public record TaskResponse(
		String title,
		String description,
		LocalDate dueDate,
		Status status,
		Priority priority
) {}

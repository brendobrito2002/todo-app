package com.myapp.todoapp.dto;

public record CategoryUpdateRequest(
		String name,
		String description
) {}

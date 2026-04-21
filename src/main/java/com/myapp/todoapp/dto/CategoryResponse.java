package com.myapp.todoapp.dto;

import com.myapp.todoapp.model.entity.Category;

public record CategoryResponse(
		Long id,
        String name,
        String description
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
        		category.getId(),
                category.getName(),
                category.getDescription()
        );
    }
}

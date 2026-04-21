package com.myapp.todoapp.dto;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.myapp.todoapp.model.entity.Task;
import com.myapp.todoapp.model.enums.Priority;
import com.myapp.todoapp.model.enums.Status;

public record TaskResponse(
		Long id,
        String title,
        String description,
        
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate dueDate,
        
        Status status,
        Priority priority,
        Long categoryId
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
        		task.getId(),
        		task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                task.getStatus(),
                task.getPriority(),
                task.getCategory() != null ? task.getCategory().getId() : null
        );
    }
}

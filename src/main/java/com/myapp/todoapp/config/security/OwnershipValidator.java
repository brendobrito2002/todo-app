package com.myapp.todoapp.config.security;

import com.myapp.todoapp.exception.AccessDeniedException;
import com.myapp.todoapp.exception.CategoryNotFoundException;
import com.myapp.todoapp.exception.TaskNotFoundException;
import com.myapp.todoapp.model.entity.Category;
import com.myapp.todoapp.model.entity.Task;
import com.myapp.todoapp.repository.CategoryRepository;
import com.myapp.todoapp.repository.TaskRepository;
import org.springframework.stereotype.Component;

@Component
public class OwnershipValidator {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;

    public OwnershipValidator(TaskRepository taskRepository,
                               CategoryRepository categoryRepository) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
    }

    public Task validateTaskOwnership(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
        		.orElseThrow(() -> new TaskNotFoundException("Tarefa não encontrada"));
        
        if (!task.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Acesso negado à tarefa");
        }  
        return task;
    }

    public Category validateCategoryOwnership(Long categoryId, Long userId) {
        Category category = categoryRepository.findById(categoryId)
        		.orElseThrow(() -> new CategoryNotFoundException("Categoria não encontrada"));
        
        if (!category.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Acesso negado à categoria");
        }
        return category;
    }
}
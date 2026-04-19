package com.myapp.todoapp.config.security;

import com.myapp.todoapp.exception.AccessDeniedException;
import com.myapp.todoapp.exception.CategoryNotFoundException;
import com.myapp.todoapp.exception.TaskNotFoundException;
import com.myapp.todoapp.model.entity.Category;
import com.myapp.todoapp.model.entity.Task;
import com.myapp.todoapp.repository.CategoryRepository;
import com.myapp.todoapp.repository.TaskRepository;
import org.springframework.security.core.context.SecurityContextHolder;
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
        if (isAdmin() || task.getUser().getId().equals(userId)) {
            return task;
        }
        throw new AccessDeniedException("Acesso negado à tarefa");
    }

    public Category validateCategoryOwnership(Long categoryId, Long userId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Categoria não encontrada"));
        if (isAdmin() || category.getUser().getId().equals(userId)) {
            return category;
        }
        throw new AccessDeniedException("Acesso negado à categoria");
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}

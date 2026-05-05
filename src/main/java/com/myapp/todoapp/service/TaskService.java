package com.myapp.todoapp.service;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.myapp.todoapp.config.security.AuthenticatedUserResolver;
import com.myapp.todoapp.config.security.OwnershipValidator;
import com.myapp.todoapp.dto.TaskRequest;
import com.myapp.todoapp.dto.TaskResponse;
import com.myapp.todoapp.dto.TaskUpdateRequest;
import com.myapp.todoapp.exception.BusinessException;
import com.myapp.todoapp.model.entity.Category;
import com.myapp.todoapp.model.entity.Task;
import com.myapp.todoapp.model.entity.User;
import com.myapp.todoapp.model.enums.Priority;
import com.myapp.todoapp.model.enums.Status;
import com.myapp.todoapp.repository.TaskRepository;
import com.myapp.todoapp.specification.TaskSpecification;

@Service
public class TaskService {
	private final TaskRepository taskRepository;
	private final AuthenticatedUserResolver authResolver;
	private final OwnershipValidator ownershipValidator;
	
	public TaskService (TaskRepository taskRepository, 
						AuthenticatedUserResolver authenticatedUserResolver, 
						OwnershipValidator ownershipValidator) {
		this.taskRepository = taskRepository;
		this.authResolver = authenticatedUserResolver;
		this.ownershipValidator = ownershipValidator;
	}
	
    public TaskResponse create(TaskRequest request) {
        User user = authResolver.getAuthenticatedUser();
        Category category = request.categoryId() != null ? ownershipValidator.validateCategoryOwnership(request.categoryId(), user.getId()) : null;
        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .dueDate(request.dueDate())
                .status(request.status() != null ? request.status() : Status.TODO)
                .priority(request.priority() != null ? request.priority() : Priority.MEDIUM)
                .user(user)
                .category(category)
                .build();

        return TaskResponse.from(taskRepository.save(task));
    }
    
    public Page<TaskResponse> findAll(Pageable pageable) {
        User user = authResolver.getAuthenticatedUser();
        return taskRepository.findByUserId(user.getId(), pageable)
                .map(TaskResponse::from);
    }
    
    public TaskResponse findById(Long taskId) {
    	User user = authResolver.getAuthenticatedUser();
    	return TaskResponse.from(ownershipValidator.validateTaskOwnership(taskId, user.getId()));
    }
    
    public TaskResponse update(Long taskId, TaskUpdateRequest request) {
        User user = authResolver.getAuthenticatedUser();
        Task task = ownershipValidator.validateTaskOwnership(taskId, user.getId());

        if (task.getStatus() == Status.DONE) {
            throw new BusinessException("Tarefa concluída não pode ser alterada");
        }

        if (request.title() != null) task.setTitle(request.title());
        if (request.description() != null) task.setDescription(request.description());
        if (request.dueDate() != null) task.setDueDate(request.dueDate());
        if (request.status() != null) task.setStatus(request.status());
        if (request.priority() != null) task.setPriority(request.priority());
        if (request.categoryId() != null) {
            Category category = ownershipValidator.validateCategoryOwnership(request.categoryId(),user.getId());
            task.setCategory(category);
        }

        return TaskResponse.from(taskRepository.save(task));
    }
    
    public void delete(Long taskId) {
        User user = authResolver.getAuthenticatedUser();
        Task task = ownershipValidator.validateTaskOwnership(taskId, user.getId());
        taskRepository.delete(task);
    }
    
    public Page<TaskResponse> findAllFiltered(
            Status status,
            Priority priority,
            LocalDate dueDate,
            Pageable pageable
    ) {
        User user = authResolver.getAuthenticatedUser();
        Specification<Task> spec = TaskSpecification.filter(
                user.getId(),
                status,
                priority,
                dueDate
        );

        return taskRepository.findAll(spec, pageable)
                .map(TaskResponse::from);
    }
}

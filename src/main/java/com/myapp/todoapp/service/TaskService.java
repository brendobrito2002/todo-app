package com.myapp.todoapp.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.myapp.todoapp.config.security.AuthenticatedUserResolver;
import com.myapp.todoapp.config.security.OwnershipValidator;
import com.myapp.todoapp.dto.TaskRequest;
import com.myapp.todoapp.dto.TaskResponse;
import com.myapp.todoapp.dto.TaskUpdateRequest;
import com.myapp.todoapp.model.entity.Category;
import com.myapp.todoapp.model.entity.Task;
import com.myapp.todoapp.model.entity.User;
import com.myapp.todoapp.model.enums.Priority;
import com.myapp.todoapp.model.enums.Status;
import com.myapp.todoapp.repository.TaskRepository;

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
    
    public List<TaskResponse> findAll(){
    	User user = authResolver.getAuthenticatedUser();
    	return taskRepository.findByUserId(user.getId())
    			.stream()
    			.map(TaskResponse::from)
    			.toList();
    }
    
    public TaskResponse findById(Long taskId) {
    	User user = authResolver.getAuthenticatedUser();
    	return TaskResponse.from(ownershipValidator.validateTaskOwnership(taskId, user.getId()));
    }
    
    public TaskResponse update(Long taskId, TaskUpdateRequest request) {
        User user = authResolver.getAuthenticatedUser();
        Task task = ownershipValidator.validateTaskOwnership(taskId, user.getId());
        
        if(request.title() != null) task.setTitle(request.title());
        if(request.description() != null) task.setDescription(request.description());
        if(request.dueDate() != null) task.setDueDate(request.dueDate());
        if(request.status() != null) task.setStatus(request.status());
        if(request.priority() != null) task.setPriority(request.priority());
        if(request.categoryId() != null) {
        	Category category = ownershipValidator.validateCategoryOwnership(request.categoryId(), user.getId());
        	task.setCategory(category);
        }
        
        return TaskResponse.from(taskRepository.save(task));
    }
    
    public void delete(Long taskId) {
        User user = authResolver.getAuthenticatedUser();
        Task task = ownershipValidator.validateTaskOwnership(taskId, user.getId());
        taskRepository.delete(task);
    }
    
    public List<TaskResponse> findByUserIdAndDueDate(LocalDate dueDate){
        User user = authResolver.getAuthenticatedUser();
        return taskRepository.findByUserIdAndDueDate(user.getId(), dueDate)
        		.stream()
        		.map(TaskResponse::from)
        		.toList();
    }
    
    public List<TaskResponse> findByUserIdAndStatus(Status status){
    	User user = authResolver.getAuthenticatedUser();
    	return taskRepository.findByUserIdAndStatus(user.getId(), status)
    			.stream()
    			.map(TaskResponse::from)
    			.toList();
    }
    
    public List<TaskResponse> findByUserIdAndPriority(Priority priority){
    	User user = authResolver.getAuthenticatedUser();
    	return taskRepository.findByUserIdAndPriority(user.getId(), priority)
    			.stream()
    			.map(TaskResponse::from)
    			.toList();
    }
}

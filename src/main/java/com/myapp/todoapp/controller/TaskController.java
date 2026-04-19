package com.myapp.todoapp.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.myapp.todoapp.dto.ApiResponse;
import com.myapp.todoapp.dto.TaskRequest;
import com.myapp.todoapp.dto.TaskResponse;
import com.myapp.todoapp.dto.TaskUpdateRequest;
import com.myapp.todoapp.model.enums.Priority;
import com.myapp.todoapp.model.enums.Status;
import com.myapp.todoapp.service.TaskService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

	private final TaskService taskService;
	
	public TaskController(TaskService taskService) {
		this.taskService = taskService;
	}
	
	@PostMapping
	public ResponseEntity<ApiResponse<TaskResponse>> create(@Valid @RequestBody TaskRequest request){
		TaskResponse data = taskService.create(request);
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Tarefa criada com sucesso", data));
	}
	
	@GetMapping
	public ResponseEntity<ApiResponse<List<TaskResponse>>> findAll(){
		List<TaskResponse> data = taskService.findAll();
		return ResponseEntity.ok(ApiResponse.success(data));
	}
	
	@GetMapping("/{taskId}")
	public ResponseEntity<ApiResponse<TaskResponse>> findById(@PathVariable Long taskId){
		TaskResponse data = taskService.findById(taskId);
		return ResponseEntity.ok(ApiResponse.success(data));
	}
	
	@PatchMapping("/{taskId}")
	public ResponseEntity<ApiResponse<TaskResponse>> update(@PathVariable Long taskId, @Valid @RequestBody TaskUpdateRequest request){
		TaskResponse data = taskService.update(taskId, request);
		return ResponseEntity.ok(ApiResponse.success("Task atualizada com sucesso",data));
	}
	
	@DeleteMapping("/{taskId}")
	public ResponseEntity<ApiResponse<TaskResponse>> delete(@PathVariable Long taskId){
		taskService.delete(taskId);
		return ResponseEntity.ok(ApiResponse.success("Task deletada com sucesso", null));
	}
	
	@GetMapping("/filter/date")
	public ResponseEntity<ApiResponse<List<TaskResponse>>> findByDueDate(@RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate dueDate) {
	    List<TaskResponse> data = taskService.findByUserIdAndDueDate(dueDate);
	    return ResponseEntity.ok(ApiResponse.success(data));
	}

    @GetMapping("/filter/status")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> findByStatus(@RequestParam Status status) {
        List<TaskResponse> data = taskService.findByUserIdAndStatus(status);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/filter/priority")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> findByPriority(@RequestParam Priority priority) {
        List<TaskResponse> data = taskService.findByUserIdAndPriority(priority);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}

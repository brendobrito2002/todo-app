package com.myapp.todoapp.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.myapp.todoapp.model.entity.Task;
import com.myapp.todoapp.model.enums.Priority;
import com.myapp.todoapp.model.enums.Status;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
	List<Task> findByUserId(Long userId);
	
	List<Task> findByUserIdAndDueDate(Long userId, LocalDate dueDate);
	
	List<Task> findByUserIdAndStatus(Long userId, Status status);
	
	List<Task> findByUserIdAndPriority(Long userId, Priority priority);
}

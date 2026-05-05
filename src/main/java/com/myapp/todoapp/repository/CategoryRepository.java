package com.myapp.todoapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.myapp.todoapp.model.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>{
	Page<Category> findByUserId(Long userId, Pageable pageable);
}

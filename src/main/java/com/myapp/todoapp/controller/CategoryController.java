package com.myapp.todoapp.controller;

import com.myapp.todoapp.dto.ApiResponse;
import com.myapp.todoapp.dto.CategoryRequest;
import com.myapp.todoapp.dto.CategoryResponse;
import com.myapp.todoapp.dto.CategoryUpdateRequest;
import com.myapp.todoapp.dto.PageResponse;
import com.myapp.todoapp.service.CategoryService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse data = categoryService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Categoria criada com sucesso", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CategoryResponse>>> findAll(Pageable pageable){
        PageResponse<CategoryResponse> data = PageResponse.from(categoryService.findAll(pageable));
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> findById(@PathVariable Long categoryId) {
        CategoryResponse data = categoryService.findById(categoryId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PatchMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(@PathVariable Long categoryId, @Valid @RequestBody CategoryUpdateRequest request) {
        CategoryResponse data = categoryService.update(categoryId, request);
        return ResponseEntity.ok(ApiResponse.success("Categoria atualizada com sucesso", data));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long categoryId) {
        categoryService.delete(categoryId);
        return ResponseEntity.ok(ApiResponse.success("Categoria removida com sucesso", null));
    }
}

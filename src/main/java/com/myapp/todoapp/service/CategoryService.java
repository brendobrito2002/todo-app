package com.myapp.todoapp.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.myapp.todoapp.config.security.AuthenticatedUserResolver;
import com.myapp.todoapp.config.security.OwnershipValidator;
import com.myapp.todoapp.dto.CategoryRequest;
import com.myapp.todoapp.dto.CategoryResponse;
import com.myapp.todoapp.dto.CategoryUpdateRequest;
import com.myapp.todoapp.model.entity.Category;
import com.myapp.todoapp.model.entity.User;
import com.myapp.todoapp.repository.CategoryRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final OwnershipValidator ownershipValidator;
    private final AuthenticatedUserResolver authResolver;

    public CategoryService(CategoryRepository categoryRepository,
                           OwnershipValidator ownershipValidator,
                           AuthenticatedUserResolver authResolver) {
        this.categoryRepository = categoryRepository;
        this.ownershipValidator = ownershipValidator;
        this.authResolver = authResolver;
    }

    public CategoryResponse create(CategoryRequest request) {
        User user = authResolver.getAuthenticatedUser();

        Category category = Category.builder()
                .name(request.name())
                .description(request.description())
                .user(user)
                .build();

        return CategoryResponse.from(categoryRepository.save(category));
    }

    public List<CategoryResponse> findAll() {
        User user = authResolver.getAuthenticatedUser();
        return categoryRepository.findByUserId(user.getId())
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public CategoryResponse findById(Long categoryId) {
        User user = authResolver.getAuthenticatedUser();
        return CategoryResponse.from(ownershipValidator.validateCategoryOwnership(categoryId, user.getId()));
    }

    public CategoryResponse update(Long categoryId, CategoryUpdateRequest request) {
        User user = authResolver.getAuthenticatedUser();
        Category category = ownershipValidator.validateCategoryOwnership(categoryId, user.getId());

        if (request.name() != null) category.setName(request.name());
        if (request.description() != null) category.setDescription(request.description());

        return CategoryResponse.from(categoryRepository.save(category));
    }

    public void delete(Long categoryId) {
        User user = authResolver.getAuthenticatedUser();
        Category category = ownershipValidator.validateCategoryOwnership(categoryId, user.getId());
        categoryRepository.delete(category);
    }
}

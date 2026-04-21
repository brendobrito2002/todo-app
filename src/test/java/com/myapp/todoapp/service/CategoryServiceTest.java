package com.myapp.todoapp.service;

import com.myapp.todoapp.config.security.AuthenticatedUserResolver;
import com.myapp.todoapp.config.security.OwnershipValidator;
import com.myapp.todoapp.dto.CategoryRequest;
import com.myapp.todoapp.dto.CategoryResponse;
import com.myapp.todoapp.dto.CategoryUpdateRequest;
import com.myapp.todoapp.model.entity.Category;
import com.myapp.todoapp.model.entity.User;
import com.myapp.todoapp.model.enums.Role;
import com.myapp.todoapp.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private OwnershipValidator ownershipValidator;

    @Mock
    private AuthenticatedUserResolver authResolver;

    @InjectMocks
    private CategoryService categoryService;

    private User user;
    private Category category;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("João Silva")
                .email("joao@email.com")
                .password("senha_encoded")
                .role(Role.USER)
                .build();

        category = Category.builder()
                .id(1L)
                .name("Trabalho")
                .description("Tarefas de trabalho")
                .user(user)
                .build();
    }

    // create

    @Test
    @DisplayName("create: deve criar categoria com sucesso")
    void create_success() {
        CategoryRequest request = new CategoryRequest("Trabalho", "Tarefas de trabalho");

        when(authResolver.getAuthenticatedUser()).thenReturn(user);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponse response = categoryService.create(request);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Trabalho");
        assertThat(response.description()).isEqualTo("Tarefas de trabalho");

        verify(authResolver).getAuthenticatedUser();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("create: deve salvar categoria vinculada ao usuário autenticado")
    void create_mustLinkToAuthenticatedUser() {
        CategoryRequest request = new CategoryRequest("Trabalho", "Tarefas de trabalho");

        when(authResolver.getAuthenticatedUser()).thenReturn(user);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        categoryService.create(request);

        verify(categoryRepository).save(argThat(saved ->
                saved.getUser().getId().equals(user.getId())
        ));
    }

    // findAll

    @Test
    @DisplayName("findAll: deve retornar todas as categorias do usuário autenticado")
    void findAll_success() {
        when(authResolver.getAuthenticatedUser()).thenReturn(user);
        when(categoryRepository.findByUserId(user.getId())).thenReturn(List.of(category));

        List<CategoryResponse> response = categoryService.findAll();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).name()).isEqualTo("Trabalho");

        verify(categoryRepository).findByUserId(user.getId());
    }

    // findById

    @Test
    @DisplayName("findById: deve retornar categoria quando pertence ao usuário")
    void findById_success() {
        when(authResolver.getAuthenticatedUser()).thenReturn(user);
        when(ownershipValidator.validateCategoryOwnership(1L, user.getId())).thenReturn(category);

        CategoryResponse response = categoryService.findById(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Trabalho");

        verify(ownershipValidator).validateCategoryOwnership(1L, user.getId());
    }

    // update

    @Test
    @DisplayName("update: deve atualizar apenas os campos enviados")
    void update_partialUpdate() {
        CategoryUpdateRequest request = new CategoryUpdateRequest("Pessoal", null);

        when(authResolver.getAuthenticatedUser()).thenReturn(user);
        when(ownershipValidator.validateCategoryOwnership(1L, user.getId())).thenReturn(category);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        categoryService.update(1L, request);

        assertThat(category.getName()).isEqualTo("Pessoal");
        assertThat(category.getDescription()).isEqualTo("Tarefas de trabalho");

        verify(categoryRepository).save(category);
    }

    // delete

    @Test
    @DisplayName("delete: deve deletar categoria quando pertence ao usuário")
    void delete_success() {
        when(authResolver.getAuthenticatedUser()).thenReturn(user);
        when(ownershipValidator.validateCategoryOwnership(1L, user.getId())).thenReturn(category);

        assertThatNoException().isThrownBy(() -> categoryService.delete(1L));

        verify(categoryRepository).delete(category);
    }

    @Test
    @DisplayName("delete: não deve chamar repository quando ownership falha")
    void delete_ownershipFails() {
        when(authResolver.getAuthenticatedUser()).thenReturn(user);
        when(ownershipValidator.validateCategoryOwnership(1L, user.getId()))
                .thenThrow(new com.myapp.todoapp.exception.AccessDeniedException("Acesso negado à categoria"));

        assertThatThrownBy(() -> categoryService.delete(1L))
                .isInstanceOf(com.myapp.todoapp.exception.AccessDeniedException.class)
                .hasMessage("Acesso negado à categoria");

        verify(categoryRepository, never()).delete(any());
    }
}
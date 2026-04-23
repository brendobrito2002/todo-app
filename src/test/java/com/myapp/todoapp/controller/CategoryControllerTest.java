package com.myapp.todoapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myapp.todoapp.config.security.JwtAuthenticationFilter;
import com.myapp.todoapp.config.security.JwtUtil;
import com.myapp.todoapp.dto.CategoryRequest;
import com.myapp.todoapp.dto.CategoryResponse;
import com.myapp.todoapp.dto.CategoryUpdateRequest;
import com.myapp.todoapp.exception.AccessDeniedException;
import com.myapp.todoapp.exception.CategoryNotFoundException;
import com.myapp.todoapp.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = CategoryController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("removal")
	@MockBean
    private CategoryService categoryService;

    @SuppressWarnings("removal")
	@MockBean
    private JwtUtil jwtUtil;

    // POST /api/categories

    @Test
    @DisplayName("create: deve retornar 201 ao criar categoria com sucesso")
    @WithMockUser
    void create_success() throws Exception {
        CategoryRequest request = new CategoryRequest("Trabalho", "Tarefas de trabalho");
        CategoryResponse response = new CategoryResponse(1L, "Trabalho", "Tarefas de trabalho");

        when(categoryService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Categoria criada com sucesso"))
                .andExpect(jsonPath("$.data.name").value("Trabalho"));
    }

    @Test
    @DisplayName("create: deve retornar 400 quando nome está em branco")
    @WithMockUser
    void create_blankName() throws Exception {
        CategoryRequest request = new CategoryRequest("", "Tarefas de trabalho");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.name").exists());
    }
    
    // GET /api/categories

    @Test
    @DisplayName("findAll: deve retornar 200 com lista de categorias")
    @WithMockUser
    void findAll_success() throws Exception {
        List<CategoryResponse> response = List.of(
                new CategoryResponse(1L, "Trabalho", "Tarefas de trabalho"),
                new CategoryResponse(2L, "Pessoal", "Tarefas pessoais")
        );

        when(categoryService.findAll()).thenReturn(response);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("findAll: deve retornar lista vazia quando usuário não tem categorias")
    @WithMockUser
    void findAll_empty() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    // GET /api/categories/{id}

    @Test
    @DisplayName("findById: deve retornar 200 com a categoria")
    @WithMockUser
    void findById_success() throws Exception {
        CategoryResponse response = new CategoryResponse(1L, "Trabalho", "Tarefas de trabalho");

        when(categoryService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Trabalho"));
    }

    @Test
    @DisplayName("findById: deve retornar 404 quando categoria não existe")
    @WithMockUser
    void findById_notFound() throws Exception {
        when(categoryService.findById(99L))
                .thenThrow(new CategoryNotFoundException("Categoria não encontrada"));

        mockMvc.perform(get("/api/categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Categoria não encontrada"));
    }

    @Test
    @DisplayName("findById: deve retornar 403 quando categoria não pertence ao usuário")
    @WithMockUser
    void findById_accessDenied() throws Exception {
        when(categoryService.findById(1L))
                .thenThrow(new AccessDeniedException("Acesso negado à categoria"));

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acesso negado à categoria"));
    }

    // PATCH /api/categories/{id}

    @Test
    @DisplayName("update: deve retornar 200 ao atualizar categoria com sucesso")
    @WithMockUser
    void update_success() throws Exception {
        CategoryUpdateRequest request = new CategoryUpdateRequest("Pessoal", null);
        CategoryResponse response = new CategoryResponse(1L, "Pessoal", "Tarefas de trabalho");

        when(categoryService.update(eq(1L), any())).thenReturn(response);

        mockMvc.perform(patch("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Categoria atualizada com sucesso"))
                .andExpect(jsonPath("$.data.name").value("Pessoal"));
    }

    @Test
    @DisplayName("update: deve retornar 403 quando categoria não pertence ao usuário")
    @WithMockUser
    void update_accessDenied() throws Exception {
        CategoryUpdateRequest request = new CategoryUpdateRequest("Pessoal", null);

        when(categoryService.update(eq(1L), any()))
                .thenThrow(new AccessDeniedException("Acesso negado à categoria"));

        mockMvc.perform(patch("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    // DELETE /api/categories/{id}

    @Test
    @DisplayName("delete: deve retornar 200 ao deletar categoria com sucesso")
    @WithMockUser
    void delete_success() throws Exception {
        doNothing().when(categoryService).delete(1L);

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Categoria removida com sucesso"));
    }

    @Test
    @DisplayName("delete: deve retornar 403 quando categoria não pertence ao usuário")
    @WithMockUser
    void delete_accessDenied() throws Exception {
        doThrow(new AccessDeniedException("Acesso negado à categoria"))
                .when(categoryService).delete(1L);

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("delete: deve retornar 404 quando categoria não existe")
    @WithMockUser
    void delete_notFound() throws Exception {
        doThrow(new CategoryNotFoundException("Categoria não encontrada"))
                .when(categoryService).delete(99L);

        mockMvc.perform(delete("/api/categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Categoria não encontrada"));
    }
}
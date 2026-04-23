package com.myapp.todoapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.myapp.todoapp.config.security.JwtAuthenticationFilter;
import com.myapp.todoapp.config.security.JwtUtil;
import com.myapp.todoapp.dto.TaskRequest;
import com.myapp.todoapp.dto.TaskResponse;
import com.myapp.todoapp.dto.TaskUpdateRequest;
import com.myapp.todoapp.exception.AccessDeniedException;
import com.myapp.todoapp.exception.TaskNotFoundException;
import com.myapp.todoapp.model.enums.Priority;
import com.myapp.todoapp.model.enums.Status;
import com.myapp.todoapp.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = TaskController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @SuppressWarnings("removal")
	@MockBean
    private TaskService taskService;

    @SuppressWarnings("removal")
	@MockBean
    private JwtUtil jwtUtil;

    private TaskResponse taskResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        taskResponse = new TaskResponse(
        		1L,
                "Tarefa 1",
                "Descrição da tarefa",
                LocalDate.now().plusDays(3),
                Status.TODO,
                Priority.MEDIUM,
                1L
        );
    }

    // POST /api/tasks

    @Test
    @DisplayName("create: deve retornar 201 ao criar task com sucesso")
    @WithMockUser
    void create_success() throws Exception {
        TaskRequest request = new TaskRequest(
                "Tarefa 1", "Descrição da tarefa",
                LocalDate.now().plusDays(3),
                Status.TODO, Priority.MEDIUM, null
        );

        when(taskService.create(any())).thenReturn(taskResponse);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tarefa criada com sucesso"))
                .andExpect(jsonPath("$.data.title").value("Tarefa 1"));
    }

    @Test
    @DisplayName("create: deve retornar 400 quando título está em branco")
    @WithMockUser
    void create_blankTitle() throws Exception {
        TaskRequest request = new TaskRequest(
                "", "Descrição da tarefa",
                LocalDate.now().plusDays(3),
                null, null, null
        );

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.title").exists());
    }

    // GET /api/tasks

    @Test
    @DisplayName("findAll: deve retornar 200 com lista de tasks")
    @WithMockUser
    void findAll_success() throws Exception {
        when(taskService.findAll()).thenReturn(List.of(taskResponse));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("findAll: deve retornar lista vazia quando usuário não tem tasks")
    @WithMockUser
    void findAll_empty() throws Exception {
        when(taskService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
    
    // GET /api/tasks/{id}

    @Test
    @DisplayName("findById: deve retornar 200 com a task")
    @WithMockUser
    void findById_success() throws Exception {
        when(taskService.findById(1L)).thenReturn(taskResponse);

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Tarefa 1"));
    }

    @Test
    @DisplayName("findById: deve retornar 404 quando task não existe")
    @WithMockUser
    void findById_notFound() throws Exception {
        when(taskService.findById(99L))
                .thenThrow(new TaskNotFoundException("Tarefa não encontrada"));

        mockMvc.perform(get("/api/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Tarefa não encontrada"));
    }

    @Test
    @DisplayName("findById: deve retornar 403 quando task não pertence ao usuário")
    @WithMockUser
    void findById_accessDenied() throws Exception {
        when(taskService.findById(1L))
                .thenThrow(new AccessDeniedException("Acesso negado à tarefa"));

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acesso negado à tarefa"));
    }
    
    // GET /api/tasks/filter/dueDate
    
    @Test
    @DisplayName("findByDueDate: deve retornar 200 com tasks filtradas pela data")
    @WithMockUser
    void findByDueDate_success() throws Exception {
        LocalDate date = LocalDate.now().plusDays(3);

        when(taskService.findByUserIdAndDueDate(date)).thenReturn(List.of(taskResponse));

        mockMvc.perform(get("/api/tasks/filter/date")
                        .param("dueDate", date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Tarefa 1"));
    }

    @Test
    @DisplayName("findByDueDate: deve retornar lista vazia quando não há tasks na data")
    @WithMockUser
    void findByDueDate_empty() throws Exception {
        LocalDate date = LocalDate.now().plusDays(10);

        when(taskService.findByUserIdAndDueDate(date)).thenReturn(List.of());

        mockMvc.perform(get("/api/tasks/filter/date")
                        .param("dueDate", date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("findByDueDate: deve retornar 400 quando formato da data é inválido")
    @WithMockUser
    void findByDueDate_invalidFormat() throws Exception {
        mockMvc.perform(get("/api/tasks/filter/date")
                        .param("dueDate", "data-invalida"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // GET /api/tasks/filter/status

    @Test
    @DisplayName("findByStatus: deve retornar 200 com tasks filtradas pelo status")
    @WithMockUser
    void findByStatus_success() throws Exception {
        when(taskService.findByUserIdAndStatus(Status.TODO)).thenReturn(List.of(taskResponse));

        mockMvc.perform(get("/api/tasks/filter/status")
                        .param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].status").value("TODO"));
    }

    @Test
    @DisplayName("findByStatus: deve retornar 400 quando status é inválido")
    @WithMockUser
    void findByStatus_invalidStatus() throws Exception {
        mockMvc.perform(get("/api/tasks/filter/status")
                        .param("status", "INVALIDO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // GET /api/tasks/filter/priority

    @Test
    @DisplayName("findByPriority: deve retornar 200 com tasks filtradas pela prioridade")
    @WithMockUser
    void findByPriority_success() throws Exception {
        when(taskService.findByUserIdAndPriority(Priority.MEDIUM)).thenReturn(List.of(taskResponse));

        mockMvc.perform(get("/api/tasks/filter/priority")
                        .param("priority", "MEDIUM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].priority").value("MEDIUM"));
    }

    @Test
    @DisplayName("findByPriority: deve retornar 400 quando priority é inválida")
    @WithMockUser
    void findByPriority_invalidPriority() throws Exception {
        mockMvc.perform(get("/api/tasks/filter/priority")
                        .param("priority", "INVALIDO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // PATCH /api/tasks/{id}

    @Test
    @DisplayName("update: deve retornar 200 ao atualizar task com sucesso")
    @WithMockUser
    void update_success() throws Exception {
        TaskUpdateRequest request = new TaskUpdateRequest(
                "Novo título", null, null, null, null, null
        );

        when(taskService.update(eq(1L), any())).thenReturn(taskResponse);

        mockMvc.perform(patch("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tarefa atualizada com sucesso"));
    }

    @Test
    @DisplayName("update: deve retornar 403 quando task não pertence ao usuário")
    @WithMockUser
    void update_accessDenied() throws Exception {
        TaskUpdateRequest request = new TaskUpdateRequest(
                "Novo título", null, null, null, null, null
        );

        when(taskService.update(eq(1L), any()))
                .thenThrow(new AccessDeniedException("Acesso negado à tarefa"));

        mockMvc.perform(patch("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    // DELETE /api/tasks/{id}

    @Test
    @DisplayName("delete: deve retornar 200 ao deletar task com sucesso")
    @WithMockUser
    void delete_success() throws Exception {
        doNothing().when(taskService).delete(1L);

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tarefa deletada com sucesso"));
    }

    @Test
    @DisplayName("delete: deve retornar 404 quando task não existe")
    @WithMockUser
    void delete_notFound() throws Exception {
        doThrow(new TaskNotFoundException("Tarefa não encontrada"))
                .when(taskService).delete(99L);

        mockMvc.perform(delete("/api/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Tarefa não encontrada"));
    }

    @Test
    @DisplayName("delete: deve retornar 403 quando task não pertence ao usuário")
    @WithMockUser
    void delete_accessDenied() throws Exception {
        doThrow(new AccessDeniedException("Acesso negado à tarefa"))
                .when(taskService).delete(1L);

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }
}
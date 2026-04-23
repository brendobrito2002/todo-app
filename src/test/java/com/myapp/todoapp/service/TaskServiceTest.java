package com.myapp.todoapp.service;

import com.myapp.todoapp.config.security.AuthenticatedUserResolver;
import com.myapp.todoapp.config.security.OwnershipValidator;
import com.myapp.todoapp.dto.TaskRequest;
import com.myapp.todoapp.dto.TaskResponse;
import com.myapp.todoapp.dto.TaskUpdateRequest;
import com.myapp.todoapp.exception.AccessDeniedException;
import com.myapp.todoapp.model.entity.Category;
import com.myapp.todoapp.model.entity.Task;
import com.myapp.todoapp.model.entity.User;
import com.myapp.todoapp.model.enums.Priority;
import com.myapp.todoapp.model.enums.Role;
import com.myapp.todoapp.model.enums.Status;
import com.myapp.todoapp.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private OwnershipValidator ownershipValidator;

    @Mock
    private AuthenticatedUserResolver authResolver;

    @InjectMocks
    private TaskService taskService;

    private User user;
    private Category category;
    private Task task;

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

        task = Task.builder()
                .id(1L)
                .title("Tarefa 1")
                .description("Descrição da tarefa")
                .dueDate(LocalDate.now().plusDays(3))
                .status(Status.TODO)
                .priority(Priority.MEDIUM)
                .user(user)
                .category(category)
                .build();
    }

    // create

    @Test
    @DisplayName("create: deve criar task com sucesso sem categoria")
    void create_successWithoutCategory() {
        TaskRequest request = new TaskRequest(
                "Tarefa 1", "Descrição da tarefa",
                LocalDate.now().plusDays(3),
                null, null, null
        );

        when(authResolver.getAuthenticatedUser()).thenReturn(user);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponse response = taskService.create(request);

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("Tarefa 1");
        assertThat(response.status()).isEqualTo(Status.TODO);
        assertThat(response.priority()).isEqualTo(Priority.MEDIUM);

        verify(ownershipValidator, never()).validateCategoryOwnership(any(), any());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("create: deve criar task com sucesso com categoria válida")
    void create_successWithCategory() {
        TaskRequest request = new TaskRequest(
                "Tarefa 1", "Descrição da tarefa",
                LocalDate.now().plusDays(3),
                null, null, 1L
        );

        when(authResolver.getAuthenticatedUser()).thenReturn(user);
        when(ownershipValidator.validateCategoryOwnership(1L, user.getId())).thenReturn(category);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponse response = taskService.create(request);

        assertThat(response).isNotNull();
        verify(ownershipValidator).validateCategoryOwnership(1L, user.getId());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("create: deve aplicar defaults quando status e priority são nulos")
    void create_defaultStatusAndPriority() {
        TaskRequest request = new TaskRequest(
                "Tarefa 1", "Descrição da tarefa",
                LocalDate.now().plusDays(3),
                null, null, null
        );

        when(authResolver.getAuthenticatedUser()).thenReturn(user);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        taskService.create(request);

        verify(taskRepository).save(argThat(saved ->
                saved.getStatus() == Status.TODO &&
                saved.getPriority() == Priority.MEDIUM
        ));
    }

    @Test
    @DisplayName("create: deve lançar exceção quando categoria não pertence ao usuário")
    void create_categoryAccessDenied() {
        TaskRequest request = new TaskRequest(
                "Tarefa 1", "Descrição da tarefa",
                LocalDate.now().plusDays(3),
                null, null, 99L
        );

        when(authResolver.getAuthenticatedUser()).thenReturn(user);
        when(ownershipValidator.validateCategoryOwnership(99L, user.getId()))
                .thenThrow(new AccessDeniedException("Acesso negado à categoria"));

        assertThatThrownBy(() -> taskService.create(request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Acesso negado à categoria");

        verify(taskRepository, never()).save(any());
    }

    // findAll

    @Test
    @DisplayName("findAll: deve retornar todas as tasks do usuário autenticado")
    void findAll_success() {
        when(authResolver.getAuthenticatedUser()).thenReturn(user);
        when(taskRepository.findByUserId(user.getId())).thenReturn(List.of(task));

        List<TaskResponse> response = taskService.findAll();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).title()).isEqualTo("Tarefa 1");

        verify(taskRepository).findByUserId(user.getId());
    }

    @Test
    @DisplayName("findAll: deve retornar lista vazia quando usuário não tem tasks")
    void findAll_empty() {
        when(authResolver.getAuthenticatedUser()).thenReturn(user);
        when(taskRepository.findByUserId(user.getId())).thenReturn(List.of());

        List<TaskResponse> response = taskService.findAll();

        assertThat(response).isEmpty();
        verify(taskRepository).findByUserId(user.getId());
    }

    // findById

    @Test
    @DisplayName("findById: deve retornar task quando pertence ao usuário")
    void findById_success() {
        when(authResolver.getAuthenticatedUser()).thenReturn(user);
        when(ownershipValidator.validateTaskOwnership(1L, user.getId())).thenReturn(task);

        TaskResponse response = taskService.findById(1L);

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("Tarefa 1");

        verify(ownershipValidator).validateTaskOwnership(1L, user.getId());
    }

    @Test
    @DisplayName("findById: deve lançar exceção quando task não pertence ao usuário")
    void findById_accessDenied() {
        when(authResolver.getAuthenticatedUser()).thenReturn(user);
        when(ownershipValidator.validateTaskOwnership(1L, user.getId()))
                .thenThrow(new AccessDeniedException("Acesso negado à tarefa"));

        assertThatThrownBy(() -> taskService.findById(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Acesso negado à tarefa");
    }

    // findByDueDate

    @Test
    @DisplayName("findByDueDate: deve retornar tasks filtradas pela data")
    void findByDueDate_success() {
        LocalDate date = LocalDate.now().plusDays(3);

        when(authResolver.getAuthenticatedUser()).thenReturn(user);
        when(taskRepository.findByUserIdAndDueDate(user.getId(), date)).thenReturn(List.of(task));

        List<TaskResponse> response = taskService.findByUserIdAndDueDate(date);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).dueDate()).isEqualTo(date);
    }

    @Test
    @DisplayName("findByDueDate: deve retornar lista vazia quando não há tasks na data")
    void findByDueDate_empty() {
        LocalDate date = LocalDate.now().plusDays(10);

        when(authResolver.getAuthenticatedUser()).thenReturn(user);
        when(taskRepository.findByUserIdAndDueDate(user.getId(), date)).thenReturn(List.of());

        List<TaskResponse> response = taskService.findByUserIdAndDueDate(date);

        assertThat(response).isEmpty();
    }

    // findByStatus

    @Test
    @DisplayName("findByStatus: deve retornar tasks filtradas pelo status")
    void findByStatus_success() {
        when(authResolver.getAuthenticatedUser()).thenReturn(user);
        when(taskRepository.findByUserIdAndStatus(user.getId(), Status.TODO)).thenReturn(List.of(task));

        List<TaskResponse> response = taskService.findByUserIdAndStatus(Status.TODO);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).status()).isEqualTo(Status.TODO);
    }

    @Test
    @DisplayName("findByStatus: deve retornar lista vazia quando não há tasks com o status")
    void findByStatus_empty() {
        when(authResolver.getAuthenticatedUser()).thenReturn(user);
        when(taskRepository.findByUserIdAndStatus(user.getId(), Status.DONE)).thenReturn(List.of());

        List<TaskResponse> response = taskService.findByUserIdAndStatus(Status.DONE);

        assertThat(response).isEmpty();
    }

    // findByPriority

    @Test
    @DisplayName("findByPriority: deve retornar tasks filtradas pela prioridade")
    void findByPriority_success() {
        when(authResolver.getAuthenticatedUser()).thenReturn(user);
        when(taskRepository.findByUserIdAndPriority(user.getId(), Priority.MEDIUM)).thenReturn(List.of(task));

        List<TaskResponse> response = taskService.findByUserIdAndPriority(Priority.MEDIUM);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).priority()).isEqualTo(Priority.MEDIUM);
    }

    @Test
    @DisplayName("findByPriority: deve retornar lista vazia quando não há tasks com a prioridade")
    void findByPriority_empty() {
        when(authResolver.getAuthenticatedUser()).thenReturn(user);
        when(taskRepository.findByUserIdAndPriority(user.getId(), Priority.HIGH)).thenReturn(List.of());

        List<TaskResponse> response = taskService.findByUserIdAndPriority(Priority.HIGH);

        assertThat(response).isEmpty();
    }

    // update

    @Test
    @DisplayName("update: deve atualizar apenas os campos enviados mantendo os demais")
    void update_partialUpdate() {
        TaskUpdateRequest request = new TaskUpdateRequest(
                "Novo título", null, null, null, null, null
        );

        when(authResolver.getAuthenticatedUser()).thenReturn(user);
        when(ownershipValidator.validateTaskOwnership(1L, user.getId())).thenReturn(task);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        taskService.update(1L, request);

        assertThat(task.getTitle()).isEqualTo("Novo título");
        assertThat(task.getDescription()).isEqualTo("Descrição da tarefa");
        assertThat(task.getStatus()).isEqualTo(Status.TODO);
        assertThat(task.getPriority()).isEqualTo(Priority.MEDIUM);

        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("update: deve atualizar categoria quando categoryId é enviado")
    void update_withCategory() {
        Category novaCategoria = Category.builder()
                .id(2L)
                .name("Pessoal")
                .user(user)
                .build();

        TaskUpdateRequest request = new TaskUpdateRequest(
                null, null, null, null, null, 2L
        );

        when(authResolver.getAuthenticatedUser()).thenReturn(user);
        when(ownershipValidator.validateTaskOwnership(1L, user.getId())).thenReturn(task);
        when(ownershipValidator.validateCategoryOwnership(2L, user.getId())).thenReturn(novaCategoria);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        taskService.update(1L, request);

        assertThat(task.getCategory().getId()).isEqualTo(2L);
        verify(ownershipValidator).validateCategoryOwnership(2L, user.getId());
    }

    @Test
    @DisplayName("update: não deve alterar categoria quando categoryId é nulo")
    void update_withoutCategory() {
        TaskUpdateRequest request = new TaskUpdateRequest(
                "Novo título", null, null, null, null, null
        );

        when(authResolver.getAuthenticatedUser()).thenReturn(user);
        when(ownershipValidator.validateTaskOwnership(1L, user.getId())).thenReturn(task);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        taskService.update(1L, request);

        verify(ownershipValidator, never()).validateCategoryOwnership(any(), any());
        assertThat(task.getCategory().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("update: deve lançar exceção quando task não pertence ao usuário")
    void update_accessDenied() {
        TaskUpdateRequest request = new TaskUpdateRequest(
                "Novo título", null, null, null, null, null
        );

        when(authResolver.getAuthenticatedUser()).thenReturn(user);
        when(ownershipValidator.validateTaskOwnership(1L, user.getId()))
                .thenThrow(new AccessDeniedException("Acesso negado à tarefa"));

        assertThatThrownBy(() -> taskService.update(1L, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Acesso negado à tarefa");

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("update: deve lançar exceção quando categoria não pertence ao usuário")
    void update_categoryAccessDenied() {
        TaskUpdateRequest request = new TaskUpdateRequest(
                null, null, null, null, null, 99L
        );

        when(authResolver.getAuthenticatedUser()).thenReturn(user);
        when(ownershipValidator.validateTaskOwnership(1L, user.getId())).thenReturn(task);
        when(ownershipValidator.validateCategoryOwnership(99L, user.getId()))
                .thenThrow(new AccessDeniedException("Acesso negado à categoria"));

        assertThatThrownBy(() -> taskService.update(1L, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Acesso negado à categoria");

        verify(taskRepository, never()).save(any());
    }

    // delete

    @Test
    @DisplayName("delete: deve deletar task com sucesso")
    void delete_success() {
        when(authResolver.getAuthenticatedUser()).thenReturn(user);
        when(ownershipValidator.validateTaskOwnership(1L, user.getId())).thenReturn(task);

        assertThatNoException().isThrownBy(() -> taskService.delete(1L));

        verify(taskRepository).delete(task);
    }

    @Test
    @DisplayName("delete: não deve chamar repository quando ownership falha")
    void delete_ownershipFails() {
        when(authResolver.getAuthenticatedUser()).thenReturn(user);
        when(ownershipValidator.validateTaskOwnership(1L, user.getId()))
                .thenThrow(new AccessDeniedException("Acesso negado à tarefa"));

        assertThatThrownBy(() -> taskService.delete(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Acesso negado à tarefa");

        verify(taskRepository, never()).delete(any());
    }
}
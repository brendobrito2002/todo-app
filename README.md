# Todo App

Gerenciador de tarefas desenvolvido como projeto de prática, construído sobre o [Auth System](https://github.com/brendobrito2002/auth-system) como base de autenticação. Permite criar e organizar tarefas por categorias, com controle de status, prioridade e prazo.

## Tecnologias

- Java 21
- Spring Boot 3.4.4
- Spring Security
- Spring Data JPA
- PostgreSQL
- Lombok
- SpringDoc OpenAPI (Swagger)
- JUnit 5 + Mockito

## Funcionalidades

- Cadastro e autenticação de usuários com JWT
- Criação e gerenciamento de tarefas com status, prioridade e prazo
- Organização de tarefas por categorias
- Filtros de tarefas por status, prioridade e data
- Controle de acesso por ownership — cada usuário acessa apenas seus próprios dados
- Perfil de administrador com acesso total ao sistema
- Documentação interativa via Swagger UI
- Tratamento de erros padronizado

## Configuração

Antes de executar, crie o arquivo `src/main/resources/application.properties` com o seguinte conteúdo:

```properties
spring.application.name=todo-app
server.port=8080

spring.datasource.url=jdbc:postgresql://localhost:5432/todoapp_db
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

jwt.secret=seu_secret_aqui_com_no_minimo_32_caracteres_e_muito_seguro
jwt.access-token-expiration=3600000

springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs
```

> **Atenção:** o valor de `jwt.secret` deve ter no mínimo 32 caracteres para o algoritmo HMAC funcionar corretamente. O banco de dados PostgreSQL deve estar criado antes de executar a aplicação. Um usuário administrador é criado automaticamente na primeira execução.

## Como executar

**Pré-requisitos:** Java 21, Maven e PostgreSQL instalados.

```bash
git clone https://github.com/brendobrito2002/todo-app.git
cd todo-app
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

A documentação interativa fica disponível em `http://localhost:8080/swagger-ui.html`.

## Endpoints

### Autenticação

```http
POST /api/auth/register
POST /api/auth/login
```

### Categorias (requer token)

```http
POST    /api/categories
GET     /api/categories
GET     /api/categories/{id}
PATCH   /api/categories/{id}
DELETE  /api/categories/{id}
```

### Tarefas (requer token)

```http
POST    /api/tasks
GET     /api/tasks
GET     /api/tasks/{id}
GET     /api/tasks/filter/status?status=TODO
GET     /api/tasks/filter/priority?priority=HIGH
GET     /api/tasks/filter/date?dueDate=01/08/2026
PATCH   /api/tasks/{id}
DELETE  /api/tasks/{id}
```

> Para endpoints protegidos, inclua o header `Authorization: Bearer {token}` ou utilize o botão **Authorize** no Swagger UI.

## Estrutura do projeto

```
src/main/java/com/myapp/todoapp/
├── config/
│   ├── security/      # Filtro JWT, validação de ownership, configuração do Spring Security
│   ├── DataInitializer.java
│   └── SwaggerConfig.java
├── controller/        # Endpoints da API
├── dto/               # Objetos de entrada e saída
├── exception/         # Exceções customizadas e handler global
├── model/
│   ├── entity/        # Entidades User, Task e Category
│   └── enums/         # Role, Status e Priority
├── repository/        # Acesso ao banco de dados
└── service/           # Regras de negócio
```

## Testes

```bash
./mvnw test
```
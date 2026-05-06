# Todo App

Gerenciador de tarefas desenvolvido como projeto de prática, construído sobre o [Auth System](https://github.com/brendobrito2002/auth-system) como base de autenticação. Permite criar e organizar tarefas por categorias, com controle de status, prioridade e prazo.

## Tecnologias

- Java 21
- Spring Boot 3.4.4
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT (jjwt 0.12.6)
- Flyway
- Lombok
- SpringDoc OpenAPI (Swagger)
- JUnit 5 + Mockito

## Funcionalidades

- Cadastro e autenticação de usuários com JWT
- Criação e gerenciamento de tarefas com status, prioridade e prazo
- Organização de tarefas por categorias
- Filtros de tarefas por status, prioridade e data
- Controle de acesso por ownership onde cada usuário acessa apenas seus próprios dados
- Perfil de administrador com acesso total ao sistema
- Versionamento do schema do banco com Flyway
- Documentação interativa via Swagger UI
- Tratamento de erros padronizado

## Perfis de execução

O projeto pode ser executado em dois modos:

- **Docker**: utilizando containers para banco e aplicação
- **Local**: utilizando PostgreSQL instalado na máquina

---

## Configuração

### Com Docker

Renomeie o arquivo `.env.example` para `.env` e preencha com os valores desejados:

```env
DB_NAME=todoapp_db
DB_USER=todoapp
DB_PASSWORD=todoapp
JWT_SECRET=seu_secret_aqui_com_no_minimo_32_caracteres_e_muito_seguro
JWT_EXPIRATION=3600000
```

---

### Sem Docker

Crie o arquivo `src/main/resources/application.properties` com o seguinte conteúdo:

```properties
spring.application.name=todo-app
server.port=8080

spring.datasource.url=jdbc:postgresql://localhost:5432/todoapp_db
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true

spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

jwt.secret=seu_secret_aqui_com_no_minimo_32_caracteres_e_muito_seguro
jwt.access-token-expiration=3600000

springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs
```

> **Atenção:** o valor de `jwt.secret` deve ter no mínimo 32 caracteres para o algoritmo HMAC funcionar corretamente.
> Certifique-se também de que o PostgreSQL local esteja rodando na porta `5432`.

---

## Como executar

### Com Docker (recomendado)

**Pré-requisitos:** Docker e Docker Compose instalados.

```bash
git clone https://github.com/brendobrito2002/todo-app.git
cd todo-app
```

Renomeie o arquivo `.env.example` para `.env` e preencha os valores, depois execute:

```bash
docker compose up --build
```

---

### Acessando o banco via pgAdmin (Docker)

Para conectar no banco que está rodando no Docker:

- **Host:** localhost
- **Port:** 5434
- **Database:** todoapp_db
- **User:** todoapp
- **Password:** (definido no `.env`)

---

A aplicação sobe em `http://localhost:8080`.

A documentação interativa fica disponível em `http://localhost:8080/swagger-ui.html`.

Na primeira execução:

- O Flyway cria automaticamente as tabelas do sistema
- Um usuário administrador é criado automaticamente com as seguintes credenciais:
```
Email: admin@todoapp.com
Senha: admin123
```

---

Para encerrar:

```bash
docker compose down
```

Para encerrar e remover os dados do banco:

```bash
docker compose down -v
```

---

### Sem Docker

**Pré-requisitos:** Java 21, Maven e PostgreSQL instalados.

Crie o banco de dados:

```sql
CREATE DATABASE todoapp_db;
```

Configure o `application.properties` conforme a seção acima, ajustando usuário e senha, e execute:

```bash
git clone https://github.com/brendobrito2002/todo-app.git
cd todo-app
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

---

## Observação importante sobre banco de dados

Ao utilizar Docker e PostgreSQL local ao mesmo tempo, você terá **dois bancos diferentes**:

- Docker → roda na porta **5434**
- PostgreSQL local → normalmente roda na porta **5432**

Eles **não compartilham dados automaticamente**.

---

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
PATCH   /api/tasks/{id}
DELETE  /api/tasks/{id}
```

> O endpoint `GET /api/tasks` suporta filtros e paginação combinados. Todos os parâmetros são opcionais.

> Para endpoints protegidos, inclua o header `Authorization: Bearer {token}` ou utilize o botão **Authorize** no Swagger UI.

---

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
├── service/           # Regras de negócio
└── specification/     # Filtros dinâmicos com JPA Specifications
```

## Testes

```bash
./mvnw test
```
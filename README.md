# DevQuote Backend

API REST para gestão de tarefas e entregas para desenvolvedores freelancers.

---

## 🚀 Stack

### Core
- Java 21 + Spring Boot 3.5.4
- Spring Data JPA + PostgreSQL 17
- Maven 3.8+

### Segurança
- OAuth2 Authorization Server
- JWT com refresh tokens
- Spring Security (RBAC)

### Recursos
- SpringDoc OpenAPI / Swagger
- JavaMailSender (notificações)
- AWS S3 (anexos)
- Docker

---

## 📦 Arquitetura

```
src/main/java/br/com/devquote/
├── adapter/              # Conversão Entity ↔ DTO
├── configuration/        # Spring configs (Security, OpenAPI)
├── controller/           # REST Controllers + docs
├── dto/                  # Request/Response DTOs
├── entity/               # Entidades JPA
├── repository/           # JPA Repositories
├── service/              # Lógica de negócio
│   └── impl/
└── security/             # @RequiresPermission, @RequiresProfile
```

---

## 🔧 Quick Start

### Requisitos
- Java 21+
- PostgreSQL 17
- Maven 3.8+

### Desenvolvimento Local

```bash
# Compilar
./mvnw clean compile

# Executar
./mvnw spring-boot:run

# Build produção
./mvnw clean package -DskipTests
```

### Docker

```bash
# Desenvolvimento
docker-compose up -d

# Produção
docker build -t devquote-backend .
docker run -p 8080:8080 devquote-backend
```

---

## 📚 Documentação API

- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

### Principais Endpoints

#### Autenticação
- `POST /api/auth/login` - Login
- `POST /api/auth/register` - Cadastro
- `POST /api/auth/refresh` - Renovar token
- `GET /api/auth/me` - Usuário autenticado

#### Recursos
- `/api/projects` - Projetos
- `/api/tasks` - Tarefas
- `/api/subtasks` - Subtarefas
- `/api/deliveries` - Entregas
- `/api/delivery-items` - Itens de entrega
- `/api/requesters` - Solicitantes
- `/api/billing-periods` - Faturamento
- `/api/dashboard` - Estatísticas

#### Administração
- `/api/users` - Usuários
- `/api/profiles` - Perfis
- `/api/permissions` - Permissões

---

## 🔒 Segurança

### Autenticação OAuth2
- Authorization Server integrado
- Tokens JWT + refresh token
- Client Credentials e Password Grant

### Autorização (RBAC)
- **Perfis:** Admin, User, Custom
- **Recursos:** BILLING, TASKS, PROJECTS, DELIVERIES, USERS, REPORTS, SETTINGS
- **Operações:** CREATE, READ, UPDATE, DELETE
- **Controle granular** por campo

---

## 📊 Funcionalidades

### Módulos
- Dashboard com métricas
- Gestão de projetos e tarefas
- Sistema de entregas
- Faturamento mensal
- Notificações por email

### Recursos Técnicos
- Paginação e ordenação dinâmica
- Filtros avançados
- Soft delete
- Auditoria (timestamps)
- Tratamento global de exceções
- Cache de consultas
- Templates de email HTML

---

## 📈 Monitoramento

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Métricas
```bash
curl http://localhost:8080/actuator/metrics
```

---

## 🧪 Testes

```bash
# Executar testes
./mvnw test

# Cobertura
./mvnw test jacoco:report
```

---

## 🤝 Contribuindo

### Padrão de Commits
- `feat:` Nova funcionalidade
- `fix:` Correção de bug
- `docs:` Documentação
- `refactor:` Refatoração
- `test:` Testes

---

## 📄 Licença

Projeto privado e proprietário. Todos os direitos reservados.

# DevQuote Backend

Sistema empresarial completo de gestão de orçamentos, projetos, tarefas e entregas para desenvolvedores freelancers.  
Construído com **Java 21** e **Spring Boot 3.5.4**, oferecendo uma API REST robusta com autenticação OAuth2 Authorization Server integrado e controle granular de permissões.

## 🚀 Tecnologias

### Core
- **Java 21** - Linguagem principal
- **Spring Boot 3.5.4** - Framework base
- **Spring Security** - Autenticação e autorização
- **Spring Data JPA** - Persistência de dados
- **PostgreSQL 15+** - Banco de dados relacional

### Segurança
- **OAuth2 Authorization Server** - Servidor de autorização integrado
- **JWT** - Tokens de autenticação stateless
- **Spring Security** - Framework de segurança
- **CORS** - Configuração para cross-origin

### Documentação e Build
- **OpenAPI 3.0 / Swagger** - Documentação interativa da API
- **Maven 3.8+** - Gerenciamento de dependências e build
- **Docker** - Containerização
- **Lombok** - Redução de boilerplate

## 📦 Arquitetura do Projeto

```
src/main/java/br/com/devquote/
├── adapter/              # Conversão Entity ↔ DTO
├── configuration/        # Configurações do Spring
│   ├── openapi/         # Configuração Swagger/OpenAPI
│   └── security/        # OAuth2, JWT, CORS
├── controller/          # REST Controllers
│   └── doc/            # Interfaces de documentação OpenAPI
├── dto/                # Data Transfer Objects
│   ├── request/        # DTOs de entrada
│   └── response/       # DTOs de saída  
├── entity/             # Entidades JPA
├── enums/              # Enumerações (ProfileType, ResourceType, etc)
├── error/              # Tratamento global de erros
├── repository/         # Interfaces JPA Repository
├── security/           # Aspectos e anotações customizadas
│   ├── @RequiresPermission  # Controle por recurso
│   └── @RequiresProfile      # Controle por perfil
├── service/            # Interfaces de serviço
│   └── impl/          # Implementações da lógica de negócio
└── utils/             # Classes utilitárias
```

## 🔧 Configuração do Ambiente

### Requisitos
- Java 21+
- Maven 3.8+
- PostgreSQL 17
- Docker e Docker Compose (opcional)

### Variáveis de Ambiente

```properties
# Banco de Dados
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5434/devquote
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=root

# Segurança OAuth2/JWT
APP_JWTSECRET=sua_chave_secreta_256bits
SECURITY_ISSUER=http://localhost:8080

# CORS
APP_FRONTEND_URL=http://localhost:5173

# JPA
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false

# Servidor
PORT=8080
```

## 🐳 Docker

### Docker Compose (Desenvolvimento)
```bash
# Iniciar todos os serviços
docker-compose up -d

# Rebuild e restart
docker-compose up --build

# Verificar logs
docker-compose logs -f devquote-backend

# Parar serviços
docker-compose down
```

### Docker Build (Produção)
```bash
# Build da imagem
docker build -t devquote-backend .

# Executar container
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/devquote \
  -e APP_JWTSECRET=secret \
  devquote-backend
```

## 💻 Desenvolvimento Local

```bash
# Instalar dependências
./mvnw clean install

# Executar aplicação
./mvnw spring-boot:run

# Executar com perfil específico
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Build para produção
./mvnw clean package -DskipTests

# Executar JAR
java -jar target/devquote-backend-0.0.1-SNAPSHOT.jar
```

## 📚 Documentação da API

Após iniciar a aplicação, acesse:
- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

### Principais Endpoints

#### Autenticação
- `POST /api/auth/login` - Login com usuário/senha
- `POST /api/auth/register` - Cadastro de novo usuário
- `POST /api/auth/refresh` - Renovar token JWT
- `GET /api/auth/me` - Dados do usuário autenticado

#### Recursos Principais
- `/api/quotes` - Gestão de orçamentos
- `/api/projects` - Gestão de projetos
- `/api/tasks` - Gestão de tarefas
- `/api/deliveries` - Gestão de entregas
- `/api/requesters` - Gestão de solicitantes
- `/api/billing-months` - Faturamento mensal
- `/api/dashboard` - Estatísticas e métricas

#### Administração
- `/api/users` - Gestão de usuários
- `/api/profiles` - Gestão de perfis
- `/api/permissions` - Gestão de permissões

## 🧪 Testes

```bash
# Executar todos os testes
./mvnw test

# Testes com relatório de cobertura
./mvnw test jacoco:report

# Testes de integração
./mvnw test -Dtest=*IntegrationTest

# Teste específico
./mvnw test -Dtest=QuoteServiceTest
```

## 🔒 Sistema de Segurança

### Autenticação OAuth2
- Authorization Server integrado
- Suporte a Client Credentials e Password Grant
- Tokens JWT com refresh token
- Configuração de issuer customizável

### Autorização (RBAC)
- **Perfis:** Admin, User, Custom
- **Recursos:** Quote, Project, Task, Delivery, etc
- **Operações:** CREATE, READ, UPDATE, DELETE
- **Permissões de Campo:** Controle granular por campo

### Anotações de Segurança
```java
// Requer perfil específico
@RequiresProfile(ProfileType.ADMIN)

// Requer permissão em recurso
@RequiresPermission(resource = ResourceType.QUOTE, operation = OperationType.UPDATE)

// Combinação de permissões
@PreAuthorize("hasRole('ADMIN') or @permissionService.hasPermission(#id, 'QUOTE', 'READ')")
```

## 📊 Funcionalidades Implementadas

### Módulos de Negócio
- ✅ **Dashboard** - Estatísticas e métricas consolidadas
- ✅ **Orçamentos** - CRUD completo com versionamento
- ✅ **Projetos** - Gestão hierárquica com tarefas
- ✅ **Tarefas/Subtarefas** - Organização e tracking
- ✅ **Entregas** - Controle de entregas agrupadas
- ✅ **Faturamento** - Controle mensal de cobranças
- ✅ **Solicitantes** - Gestão de clientes/solicitantes

### Recursos Técnicos
- ✅ Paginação e ordenação dinâmica
- ✅ Filtros avançados por query params
- ✅ Soft delete em entidades críticas
- ✅ Auditoria com created/updated timestamps
- ✅ Correlation ID para rastreamento
- ✅ Tratamento global de exceções
- ✅ Validação em múltiplas camadas
- ✅ Cache de consultas frequentes

## 🏗️ Padrões de Desenvolvimento

### Fluxo de Dados
```
Controller → Service → Repository → Database
     ↓           ↓           ↓
    DTO      Entity      Entity
     ↓           ↓           ↓
  Response   Business    Persistence
            Logic        Layer
```

### Convenções
- **DTOs:** Separação entre Request e Response
- **Adapters:** Conversão centralizada Entity ↔ DTO
- **Services:** Interface + Implementação
- **Validação:** Bean Validation + Custom Validators
- **Erros:** ProblemDetails (RFC 7807)

## 📈 Monitoramento

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Métricas
```bash
curl http://localhost:8080/actuator/metrics
```

### Logs
- Configuração via Logback
- Níveis: ERROR, WARN, INFO, DEBUG, TRACE
- Correlation ID em todas as requisições
- Arquivo: `logs/devquote.log`

## 🚀 Deploy

### Render (Produção)
```yaml
# render.yaml
services:
  - type: web
    name: devquote-backend
    env: docker
    dockerfilePath: ./Dockerfile
    envVars:
      - key: SPRING_DATASOURCE_URL
        fromDatabase:
          name: devquote-db
          property: connectionString
```

### Heroku
```bash
heroku create devquote-backend
heroku addons:create heroku-postgresql:hobby-dev
git push heroku main
```

## 🤝 Contribuindo

1. Fork o projeto
2. Crie sua feature branch (`git checkout -b feature/NovaFuncionalidade`)
3. Commit suas mudanças (`git commit -m 'feat: adiciona nova funcionalidade'`)
4. Push para a branch (`git push origin feature/NovaFuncionalidade`)
5. Abra um Pull Request

### Padrão de Commits
- `feat:` Nova funcionalidade
- `fix:` Correção de bug
- `docs:` Documentação
- `style:` Formatação
- `refactor:` Refatoração
- `test:` Testes
- `chore:` Manutenção

## 📄 Licença

Este projeto é privado e proprietário. Todos os direitos reservados.

## 👥 Equipe

Desenvolvido com ❤️ para a comunidade de desenvolvedores freelancers.

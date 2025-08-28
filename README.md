# DevQuote Backend

Sistema de gestão de orçamentos, tarefas e entregas para desenvolvedores freelancers.  
Construído com **Java 21** e **Spring Boot 3.5.4**, oferecendo uma API REST robusta com autenticação OAuth2 e controle granular de permissões.

## 🚀 Tecnologias

- **Java 21** + **Spring Boot 3.5.4**
- **Spring Security** com OAuth2 e JWT
- **Spring Data JPA** com PostgreSQL
- **OpenAPI/Swagger** para documentação
- **Docker** para containerização
- **Maven** para gerenciamento de dependências

## 📦 Estrutura do Projeto

```
src/main/java/br/com/devquote/
├── adapter/          # Adaptadores para conversão de DTOs
├── configuration/    # Configurações (Security, OpenAPI, CORS)
├── controller/       # Controllers REST e documentação
├── dto/             # DTOs de request/response
├── entity/          # Entidades JPA
├── enums/           # Enumerações
├── error/           # Tratamento de erros
├── repository/      # Repositórios JPA
├── security/        # Aspectos e anotações de segurança
├── service/         # Lógica de negócio
└── utils/           # Utilitários
```

## 🔧 Configuração do Ambiente

### Requisitos
- Java 21+
- Maven 3.8+
- PostgreSQL 15+
- Docker (opcional)

### Variáveis de Ambiente

```properties
# Banco de Dados
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/devquote
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=sua_senha

# Segurança
APP_JWTSECRET=seu_secret_jwt_256bits
SECURITY_ISSUER=http://localhost:8080

# CORS
DEVQUOTE_CORS_ALLOWED_ORIGINS=http://localhost:5173

# Servidor
PORT=8080
```

## 🐳 Executando com Docker

```bash
# Build e execução
docker-compose up --build

# Execução em background
docker-compose up -d

# Parar containers
docker-compose down
```

## 💻 Executando Localmente

```bash
# Instalar dependências
mvn clean install

# Executar aplicação
mvn spring-boot:run

# Executar com perfil específico
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## 📚 Documentação API

Após iniciar a aplicação, acesse:
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## 🧪 Testes

```bash
# Executar todos os testes
mvn test

# Executar testes com coverage
mvn test jacoco:report
```

## 🏗️ Build para Produção

```bash
# Gerar JAR
mvn clean package -DskipTests

# Executar JAR
java -jar target/devquote-backend-*.jar
```

## 📊 Monitoramento

Endpoints do Actuator disponíveis:
- `GET /actuator/health` - Status da aplicação
- `GET /actuator/info` - Informações da build
- `GET /actuator/metrics` - Métricas da aplicação

## 🔒 Segurança

O sistema implementa:
- Autenticação OAuth2 com JWT
- Controle de acesso baseado em perfis (RBAC)
- Permissões granulares por recurso e campo
- Proteção CORS configurável

## 📝 Funcionalidades Principais

- **Gestão de Projetos**: Criação e acompanhamento de projetos
- **Tarefas e Subtarefas**: Organização hierárquica de atividades
- **Orçamentos (Quotes)**: Geração e controle de orçamentos
- **Faturamento Mensal**: Controle de cobranças mensais
- **Entregas**: Registro de entregas aos clientes
- **Dashboard**: Métricas e estatísticas do sistema
- **Gestão de Usuários**: Controle de acesso multi-tenant

## 🤝 Contribuindo

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto é privado e proprietário.
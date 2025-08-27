# DevQuote — Backend (Java 21, Spring Boot 3)

API REST do **DevQuote**, um sistema de gestão de orçamentos e projetos para desenvolvedores.  
Construída com **Java 21** e **Spring Boot 3**, integrando com **PostgreSQL** e seguindo boas práticas de arquitetura, segurança e deploy.

---

## 📌 Sumário
- [Arquitetura e Tecnologias](#arquitetura-e-tecnologias)
- [Requisitos](#requisitos)
- [Perfis e Configurações](#perfis-e-configurações)
- [Como rodar](#como-rodar)
  - [Local (IDE / Maven)](#local-ide--maven)
  - [Docker (dev local)](#docker-dev-local)
  - [Produção (Render)](#produção-render)
- [Variáveis de Ambiente](#variáveis-de-ambiente)
- [Banco de Dados](#banco-de-dados)
- [Observabilidade (Actuator)](#observabilidade-actuator)
- [Comandos Úteis](#comandos-úteis)
- [Troubleshooting](#troubleshooting)
- [Boas Práticas de Segurança](#boas-práticas-de-segurança)
- [Licença](#licença)

---

## 🏗 Arquitetura e Tecnologias
- **Java 21** + **Spring Boot 3**
- **PostgreSQL** 15+
- **Maven**
- **Docker / Docker Compose**
- **Spring Security** com JWT
- **Spring Actuator** para healthchecks e métricas
- **Perfis de configuração** (`default`, `docker`, `prod`)

Estrutura simplificada de pacotes:
```
src/main/java/br/com/devquote/
├── api/            # Controllers REST
├── configuration/  # Configurações (security, cors, profiles)
├── domain/         # Entidades de negócio
├── dto/            # DTOs de entrada/saída
├── repository/     # Repositórios JPA
├── service/        # Regras de negócio
└── util/           # Utilitários comuns
```

---

## ⚙ Requisitos
- Java 21+
- Maven 3.9+
- Docker 24+ (opcional)
- PostgreSQL 15+ (se rodar localmente sem Docker)

---

## 🌐 Perfis e Configurações
- **default** → desenvolvimento local na IDE
- **docker** → execução com Docker Compose
- **prod** → deploy em ambiente de produção (ex.: Render)

Perfis definidos via:
```bash
export SPRING_PROFILES_ACTIVE=docker
```
ou:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

---

## 🚀 Como rodar

### Local (IDE / Maven)
1. Suba um PostgreSQL local (`localhost:5432`), crie o banco `devquote` e configure `application.yml`.
2. Rode:
```bash
mvn spring-boot:run
```
ou:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=default
```

---

### Docker (dev local)
O projeto possui `Dockerfile` e `docker-compose.yml`.

Subir app + banco:
```bash
docker-compose up --build
```
Em segundo plano:
```bash
docker-compose up -d --build
```
Parar:
```bash
docker-compose down
```

Padrões esperados:
- App: `http://localhost:8080`
- Postgres (host): `5434` → container `5432`

---

### Produção (Render)
Variáveis mínimas:
```dotenv
SPRING_PROFILES_ACTIVE=prod
APP_JWTSECRET=<segredo-forte-256bits>
SECURITY_ISSUER=https://<sua-app>.onrender.com
DEVQUOTE_CORS_ALLOWED_ORIGINS=https://seu-frontend.com
PORT=8080
SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/devquote?sslmode=require
SPRING_DATASOURCE_USERNAME=<usuario>
SPRING_DATASOURCE_PASSWORD=<senha>
```

Build:
```bash
mvn clean package -DskipTests
```
Rodar:
```bash
java -jar target/devquote-*.jar
```

---

## 🔑 Variáveis de Ambiente

| Variável | Descrição | Exemplo |
|----------|-----------|---------|
| `SPRING_PROFILES_ACTIVE` | Perfil ativo | `default` \| `docker` \| `prod` |
| `SPRING_DATASOURCE_URL` | JDBC URL | `jdbc:postgresql://postgres:5432/devquote?sslmode=disable` |
| `SPRING_DATASOURCE_USERNAME` | Usuário do banco | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Senha do banco | `root` |
| `APP_JWTSECRET` | Segredo JWT (HS256) | `base64:...` |
| `SECURITY_ISSUER` | Issuer esperado no token | `http://localhost:8080` |
| `DEVQUOTE_CORS_ALLOWED_ORIGINS` | Origens CORS permitidas | `http://localhost:3000` |
| `PORT` | Porta HTTP | `8080` |

---

## 🗄 Banco de Dados
- Migração de schema recomendada com **Flyway** ou **Liquibase**.
- Para dev com Docker Compose:
  - Host: `postgres`
  - Porta container: `5432`
  - Porta host: `5434`
  - Database: `devquote`
  - Usuário: `postgres`
  - Senha: `root`

---

## 📊 Observabilidade (Actuator)
Endpoints úteis:
- `GET /actuator/health` — status da aplicação
- `GET /actuator/info` — informações de build
- `GET /actuator/env` — variáveis de ambiente (**restrito!**)
- `GET /actuator/metrics` — métricas gerais

> Em produção, exponha apenas `health`, `info` e `prometheus` e proteja-os.

---

## 🛠 Comandos Úteis
```bash
# Rodar testes
mvn test

# Limpar e buildar sem testes
mvn clean package -DskipTests

# Subir containers
docker-compose up -d --build

# Derrubar containers
docker-compose down
```

---

## 🐞 Troubleshooting
- **Erro de conexão no Docker**  
  Verifique se a `SPRING_DATASOURCE_URL` aponta para `postgres:5432` dentro do container.
- **CORS bloqueando requisição**  
  Ajuste `DEVQUOTE_CORS_ALLOWED_ORIGINS` para incluir o domínio da requisição.

---

## 🔒 Boas Práticas de Segurança
- Nunca commitar segredos ou senhas no repositório.
- Usar segredo JWT com pelo menos **32 bytes** (256 bits) e trocar periodicamente.
- Restringir CORS para origens confiáveis.
- Proteger endpoints sensíveis do Actuator.

---

## 📄 Licença
Distribuído sob a licença MIT. Consulte `LICENSE` para mais detalhes.


## 📄 Comandos úteis
M-01
cd "/mnt/c/Users/Wesley Eduardo/Documents/projetos-git/devquote/devquote-backend"
docker compose -f "/mnt/c/Users/Wesley Eduardo/Documents/projetos-git/devquote-backend/docker-compose.yml" up -d

M-02
cd "/mnt/c/Users/wesle/OneDrive/Documentos/projetos-git/devquote-backend"
docker compose -f "/mnt/c/Users/wesle/OneDrive/Documentos/projetos-git/devquote-backend/docker-compose.yml" up -d



## 📄 Swagger
http://localhost:8080/swagger-ui/index.html





## 📄 Atualização da imagem do Deploy Opção A — Enviar somente a tag latest

docker login -u SEU_USUARIO

docker build -t wesleyeduardodev/devquote-backend:latest .

docker push wesleyeduardodev/devquote-backend:latest


## 📄 Atualização da imagem do Deploy Opção B — Enviar somente a TAG  de Data

🚀 Build e Push da Imagem Docker para o Docker Hub
1. Login no Docker Hub
   docker login -u SEU_USUARIO


Digite sua senha quando solicitado (não aparece na tela).
Se tudo der certo, vai aparecer Login Succeeded.

2. Definir variáveis (imagem e tag com data/hora/segundo)
   IMAGE=wesleyeduardodev/devquote-backend
   TAG=$(date +%d-%m-%Y-%H-%M-%S)
   echo "Usando tag: $TAG"

3. Build da imagem
   docker build -t $IMAGE:$TAG -t $IMAGE:latest .

4. Push da imagem com tag única
   docker push $IMAGE:$TAG

5. Push da imagem latest
   docker push $IMAGE:latest

6. Mostrar a URL final para usar no Render
   echo "Image URL para o Render:"
   echo "docker.io/$IMAGE:$TAG"

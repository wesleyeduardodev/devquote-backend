# DevQuote Backend - Configurações por Ambiente

Este projeto está configurado para rodar em três ambientes diferentes, cada um com suas próprias configurações.

## 📁 Estrutura de Configuração

```
src/main/resources/
├── application.yml              # Configuração padrão (IDE/Local)
├── application-docker.yml       # Configuração para Docker local
└── application-prod.yml         # Configuração para produção (Render)
```

## 🔧 Ambientes de Execução

### 1. 🖥️ **Desenvolvimento Local (IDE)**
**Arquivo:** `application.yml`
**Profile:** `default` (padrão)

**Pré-requisitos:**
- PostgreSQL instalado localmente na porta 5432
- Banco `devquote` criado
- Usuário `postgres` com senha `root`

**Como executar:**
```bash
# Via IDE (IntelliJ/VSCode)
mvn spring-boot:run

# Via Maven
mvn spring-boot:run -Dspring-boot.run.profiles=default
```

**Características:**
- Logs detalhados para debug
- Show SQL habilitado
- Conecta no PostgreSQL local

---

### 2. 🐳 **Docker Local**
**Arquivo:** `application-docker.yml`
**Profile:** `docker`

**Como executar:**
```bash
# Construir e executar com Docker Compose
docker-compose up --build

# Executar em background
docker-compose up -d --build

# Parar os containers
docker-compose down
```

**Características:**
- Usa PostgreSQL em container
- Porta do banco: 5434 (host) → 5432 (container)
- Logs otimizados
- Profile automático via `SPRING_PROFILES_ACTIVE=docker`

---

### 3. 🚀 **Produção (Render)**
**Arquivo:** `application-prod.yml`
**Profile:** `prod`

**Configuração no Render:**

1. **Variáveis de Ambiente Obrigatórias:**
```bash
SPRING_PROFILES_ACTIVE=prod
APP_JWTSECRET=SeuSecretSuperForteAqui256Bits
SECURITY_ISSUER=https://sua-app.render.com
DEVQUOTE_CORS_ALLOWED_ORIGINS=https://seu-frontend.com
PORT=8080
```

2. **Build Command:**
```bash
mvn clean package -DskipTests
```

3. **Start Command:**
```bash
java -jar target/devquote-0.0.1-SNAPSHOT.jar
```

**Características:**
- Conecta no PostgreSQL do Render
- SSL obrigatório (`sslmode=require`)
- Logs minimizados
- Variáveis de ambiente para segurança

---

## 🔐 Configuração de Segurança

### JWT Secret
- **Desenvolvimento:** Usa valor padrão (não seguro)
- **Produção:** **OBRIGATÓRIO** definir `APP_JWTSECRET` forte

### Gerar um JWT Secret seguro:
```bash
# Opção 1: OpenSSL
openssl rand -base64 32

# Opção 2: Java
java -cp "target/classes" -c "System.out.println(java.util.Base64.getEncoder().encodeToString(java.security.SecureRandom.getInstanceStrong().generateSeed(32)))"
```

---

## 🌐 CORS Configuration

### Desenvolvimento:
```yaml
devquote:
  cors:
    allowed-origins: http://localhost:3000,http://localhost:4200,http://localhost:8080
```

### Produção:
```bash
DEVQUOTE_CORS_ALLOWED_ORIGINS=https://meu-frontend.vercel.app,https://admin.meu-app.com
```

---

## 🗄️ Configuração do Banco

### Local (IDE):
```yaml
url: jdbc:postgresql://localhost:5432/devquote?sslmode=disable
username: postgres
password: root
```

### Docker:
```yaml
url: jdbc:postgresql://postgres:5432/devquote?sslmode=disable
username: postgres
password: root
```

### Produção (Render):
```yaml
url: postgresql://devquote:senha@host.render.com/devquote?sslmode=require
username: devquote
password: khfOpZYqxTP60DvDA2dPJCxGehBvqlra
```

---

## 📝 Comandos Úteis

```bash
# Executar com profile específico
mvn spring-boot:run -Dspring-boot.run.profiles=docker

# Verificar qual profile está ativo
curl http://localhost:8080/actuator/env | grep "spring.profiles.active"

# Logs em tempo real (Docker)
docker-compose logs -f devquote-backend

# Conectar no banco PostgreSQL local
psql -h localhost -p 5432 -U postgres -d devquote

# Conectar no banco Render
PGPASSWORD=khfOpZYqxTP60DvDA2dPJCxGehBvqlra psql -h dpg-d2dqbi0dl3ps73b5lmp0-a.oregon-postgres.render.com -U devquote devquote
```

---

## ⚠️ Checklist de Deploy

### Antes de fazer deploy em produção:

- [ ] Definir `APP_JWTSECRET` forte
- [ ] Configurar `SECURITY_ISSUER` com URL real
- [ ] Ajustar `DEVQUOTE_CORS_ALLOWED_ORIGINS`
- [ ] Verificar URL do banco de produção
- [ ] Testar conexão com o banco
- [ ] Configurar logs apropriados
- [ ] Verificar variável `PORT` do Render

---

## 🆘 Troubleshooting

### Problema: Erro de conexão com banco
**Solução:** Verificar se o banco está rodando e as credenciais estão corretas

### Problema: CORS Error
**Solução:** Ajustar `DEVQUOTE_CORS_ALLOWED_ORIGINS` com as URLs corretas

### Problema: JWT Error
**Solução:** Verificar se `APP_JWTSECRET` está definido e tem tamanho adequado

### Problema: Profile não carrega
**Solução:** Verificar `SPRING_PROFILES_ACTIVE` nas variáveis de ambiente


### Comandos Docker
docker stop $(docker ps -aq) 2>/dev/null
docker rm -vf $(docker ps -aq) 2>/dev/null
docker rmi -f $(docker images -aq) 2>/dev/null
docker volume rm $(docker volume ls -q) 2>/dev/null
docker system prune -af --volumes

docker logs --follow devquote-backend
docker logs --follow devquote-postgres


# Testar se a imagem funciona
docker run -p 8080:8080 \
-e SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/devquote?sslmode=disable" \
-e SPRING_DATASOURCE_USERNAME="postgres" \
-e SPRING_DATASOURCE_PASSWORD="root" \
devquote-backend
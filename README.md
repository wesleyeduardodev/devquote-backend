# DevQuote Backend

## 🎯 Propósito
API REST em Spring Boot para gestão completa de tarefas, entregas e faturamento de projetos de desenvolvimento de software. Sistema multi-perfil com controle granular de permissões.

## 🛠️ Stack Tecnológica
- **Java 21** + **Spring Boot 3.5.4**
- **PostgreSQL** (produção) + **H2** (desenvolvimento)
- **Spring Security** + **JWT** + **OAuth2**
- **AWS S3** (armazenamento de arquivos)
- **Redis** (cache distribuído)
- **Prometheus** (métricas e monitoramento)
- **SpringDoc OpenAPI** (documentação Swagger)
- **Apache POI** (exportação Excel)
- **Thymeleaf** (templates de email)

## 📁 Estrutura do Projeto
```
src/main/java/br/com/devquote/
├── entity/              # 25 entidades JPA (User, Task, Delivery, BillingPeriod, etc)
├── repository/          # Repositórios Spring Data JPA
├── service/             # Interfaces + impl/ (lógica de negócio)
├── controller/          # ~20 controllers REST + doc/ (OpenAPI)
├── dto/                 # request/ + response/ (DTOs separados)
├── adapter/             # Conversão Entity <-> DTO
├── configuration/       # Configs Spring + security/ (JWT, OAuth2, CORS)
├── enums/               # DeliveryStatus, FlowType, ProfileType, etc
├── error/               # ApiExceptionHandler (tratamento global)
└── utils/               # Utilitários
```

## 🔑 Funcionalidades Principais

### Autenticação & Autorização
- Login JWT (validade 24h) + refresh token
- Sistema de perfis: **ADMIN** > **MANAGER** > **USER**
- Permissões granulares por recurso e campo
- OAuth2 Authorization Server

### Gestão de Tarefas
- CRUD completo com filtros avançados e paginação
- Subtarefas com valores individuais
- Anexos (upload S3, download com URL pré-assinada)
- Fluxos: **DESENVOLVIMENTO** e **OPERACIONAL**
- Tarefas desvinculadas (sem faturamento/entrega)
- Exportação Excel + relatórios completos
- Envio de emails (financeiro e notificação)

### Sistema de Entregas
- Status: PENDING → DEVELOPMENT → DELIVERED → HOMOLOGATION → APPROVED/REJECTED → PRODUCTION
- **Itens de Desenvolvimento**: vinculados a projetos, branches, PRs
- **Itens Operacionais**: tarefas operacionais independentes
- Cálculo automático de status baseado nos itens
- Anexos por entrega e por item
- Exportação e relatórios

### Períodos de Faturamento
- Criação por mês/ano (constraint único)
- Vinculação de múltiplas tarefas ao período
- Totalizadores automáticos (soma de valores)
- Filtros por flowType (DESENVOLVIMENTO/OPERACIONAL)
- Anexos (notas fiscais, comprovantes)
- Exportação Excel + email de resumo

### Dashboard
- Estatísticas gerais (usuários, receita, tarefas, taxa conclusão)
- Estatísticas por módulo (tarefas, entregas, projetos, solicitantes)
- Gráficos de tarefas por período e entregas por status
- Atividades recentes

### Outros Módulos
- **Projetos**: gestão de repositórios
- **Solicitantes**: clientes/stakeholders
- **Usuários**: gerenciamento completo (ADMIN only)
- **Notificações**: configurações por tipo e canal (email, telefone)

## 🔒 Segurança
- **Password**: BCrypt (força 10)
- **Token**: JJWT (HS256) com secret base64
- **CORS**: origens permitidas configuráveis
- **SQL Injection**: JPA/Hibernate (prepared statements)
- **Endpoints públicos**: apenas `/api/auth/login` e `/api/auth/register`

## 🗄️ Banco de Dados
**25 entidades principais**:
- **Core**: User, Profile, Permission, ResourcePermission, FieldPermission
- **Negócio**: Task, SubTask, TaskAttachment, Requester, Project
- **Entregas**: Delivery, DeliveryItem, DeliveryOperationalItem + Attachments
- **Faturamento**: BillingPeriod, BillingPeriodTask, BillingPeriodAttachment
- **Configurações**: NotificationConfig

**Relacionamentos-chave**:
- User ←→ UserProfile ←→ Profile (many-to-many)
- Task → SubTask[] (one-to-many)
- Task ↔ Delivery (one-to-one)
- Delivery → DeliveryItem[] + DeliveryOperationalItem[] (one-to-many)
- BillingPeriod → BillingPeriodTask[] → Task (many-to-many)

## 🚀 Configuração
Variáveis de ambiente necessárias (`.env.example`):
```bash
APP_JWTSECRET=<base64-secret>
AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY / AWS_S3_BUCKET_NAME / AWS_S3_REGION
MAIL_HOST / MAIL_PORT / MAIL_USERNAME / MAIL_PASSWORD
DEVQUOTE_EMAIL_ENABLED / DEVQUOTE_EMAIL_FROM
```

Banco: `jdbc:postgresql://localhost:5434/devquote` (user: postgres, pass: root)
Redis: `localhost:6379`

## 📊 Status Atual

### ✅ Completo e Funcional
- Todos os módulos de negócio (100%)
- Autenticação e autorização (100%)
- Integrações externas (S3, Redis, Email, Prometheus)
- Exportações e relatórios
- Sistema de permissões granular

### ⚠️ Pontos de Atenção
- **Dashboard**: alguns dados estatísticos usam mock (TODOs identificados)
  - Contagens mensais (tarefas, requesters)
  - Gráficos de atividades (dados simulados)
  - Sistema de auditoria não implementado
- **Testes**: ausência total de testes automatizados
- **Migrations**: usando `ddl-auto=update` (recomendado Flyway/Liquibase para prod)

### 📝 TODOs Pendentes
1. Implementar contagens mensais reais no dashboard
2. Substituir dados mock de gráficos por dados reais
3. Criar sistema de auditoria/logs para atividades recentes
4. Implementar testes unitários e de integração

## 🔍 Endpoints Importantes
- Swagger UI: `/swagger-ui.html`
- Actuator: `/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`
- Total: **~100+ endpoints REST**

## 💡 Contexto de Uso
Sistema usado para gerenciar demandas de desenvolvimento, desde a solicitação até a entrega e faturamento. Suporta dois fluxos:
1. **DESENVOLVIMENTO**: tarefas técnicas com itens vinculados a projetos/branches/PRs
2. **OPERACIONAL**: tarefas operacionais sem vinculação técnica

Permite rastreamento completo: Tarefa → Subtarefas → Entrega → Itens de Entrega → Período de Faturamento

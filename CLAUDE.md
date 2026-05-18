# CLAUDE.md — devquote-backend

Instruções para o agente. Overview e funcionalidades estão no `README.md`. Regras do monorepo no `../CLAUDE.md`.

## Stack
Java 17 · Spring Boot 3.5.4 · Spring Security + JWT (jjwt) · PostgreSQL 17 · Caffeine cache · AWS S3 · SMTP + Thymeleaf · Micrometer/OTEL · Apache POI + JasperReports.

## Comandos

```powershell
./mvnw spring-boot:run                     # dev (porta 8080, Swagger em /swagger-ui.html)
./mvnw -DskipTests package                 # JAR em target/
./mvnw test                                # ⚠️ praticamente sem testes hoje
# Infra local (Postgres + Redis) sobe pelo compose da RAIZ do monorepo:
cd ../ && docker compose up -d
```

Postgres local: `localhost:5435/devquote_wesley`, user `devquote_wesley_user`, senha `devquote_wesley` (estrutura espelha produção; ver `../docker-compose.yml`).
Vars para IntelliJ: `../varaiveis_intelij_local.txt` (local) ou `../varaiveis_intelij_prod.txt` (apontando p/ banco de prod).

## Estrutura `src/main/java/br/com/devquote/`

`entity/`, `repository/`, `service/` + `service/impl/`, `controller/` (+ `doc/` p/ OpenAPI), `dto/request/` + `dto/response/`, `adapter/`, `configuration/` (+ `security/`), `enums/`, `error/`, `client/` (`clickup/`, `git/`), `job/`, `helper/`, `utils/`.

## Convenções (seguir ao adicionar feature)

- **DTOs separados por direção:** `dto/request/` e `dto/response/`. Nunca reutilizar.
- **Adapter Pattern para conversão Entity↔DTO** em `adapter/`. Controller não monta DTO.
- **Lombok liberado:** `@Data`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`.
- **Exceções de domínio:** lançar `BusinessException` / `ResourceNotFoundException` no service. `ApiExceptionHandler` traduz para `ProblemDetail` (RFC 7807). Não tratar no controller.
- **Paginação:** Spring Data `Page<T>` + `PageAdapter` antes de devolver.
- **`@Transactional`** em service que escreve em ≥2 entidades.
- **OpenAPI:** endpoint novo público → criar classe `*Doc` em `controller/doc/`.
- **Configs dinâmicas** (tokens, SMTP, JWT, etc) vivem em `system_parameter` (tabela). Acesso via `*Helper` em `helper/`. **Não hardcode** em `application.yml`.

## Gotchas

- **`spring.jpa.hibernate.ddl-auto=update`** em prod, sem Flyway/Liquibase. Mudanças aditivas (nova coluna opcional, nova entidade) aplicam sozinhas no próximo restart. **Drops/renames precisam de SQL manual** em cada tenant — avisar o dono antes.
- **Permissões granulares (`ResourcePermission`, `FieldPermission`) NÃO são aplicadas no backend.** Só perfil (`ROLE_ADMIN/MANAGER/USER`) é checado. Granular só roda no frontend. Endpoint sensível novo → no mínimo exigir `ROLE_ADMIN`.
- **Sem refresh token.** JWT 24h fixo. Não criar feature que assuma sessão > 24h sem alinhar.
- **Cache em uso é Caffeine**, não Redis (Redis está comentado em `application.yml`). Cluster com >1 réplica seria problemático.
- **Sync ClickUp/GitHub sem retry/circuit breaker.** Falha → log `[ERRO]` e segue.
- **Logs de sync têm formato fixo** — manter prefixos `=== INICIO ===`, `[SUCESSO]`, `[ERRO]`, `[PROCESSANDO]`, `[PULADO]`, `=== FIM ===`. Queries Loki/Grafana dependem disso. Ver `README.md` seção "Visualização de Logs".

## Configuração

Sem profiles `dev/prod` separados. Tudo via env var com defaults para localhost. Variáveis críticas: `APP_JWTSECRET`, `AWS_*`, `MAIL_*`, `DEVQUOTE_EMAIL_*`, `SPRING_DATASOURCE_*`. Em prod, a maioria já está em `system_parameter` (banco); só Postgres + AWS continuam em env do pod (ver `devquote-infra/SECRETS.md`).

## Jobs agendados (`@Scheduled`, fuso America/Sao_Paulo)

| Job | Cron | Endpoint manual |
|---|---|---|
| `GitPullRequestSyncJob` | `0 0 6 * * ?` (6h) | `POST /api/git-sync` |
| `ClickUpSyncJob` | `0 0 7 * * ?` (7h) | `POST /api/clickup-sync` |

## Deploy

`git push` master → GitHub Actions builda e push `wesleyeduardodev/devquote-backend:sha-<short>` no Docker Hub → workflow commita nova tag em `devquote-infra/tenants/{wesley,joao}/backend/deployment.yaml` → Argo CD aplica em ~3min. **Nunca `docker push` manual ou `kubectl set image`** — quebra GitOps.

## Antes de mexer

| Vou… | Olhar primeiro |
|---|---|
| Adicionar endpoint | `controller/` + `dto/request/` + `dto/response/` + `service/` + `adapter/` + `controller/doc/` |
| Mudar regra de negócio | `service/impl/` (controller é fino) |
| Alterar entidade | `entity/` + lembrar de ddl-auto (ver gotcha acima) |
| Nova integração externa | `client/` + parâmetros via `*Helper` (banco), nunca hardcoded |
| Job agendado novo | `job/` + `@Scheduled` + manter formato de logs de sync |
| Permissão granular | Frontend (`ScreenGuard`/`ResourceGuard`/`FieldGuard`). Backend só valida perfil. |
| Template de email | `src/main/resources/templates/email/*.html` (Thymeleaf) |

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
- **Permissões granulares (`ResourcePermission`, `FieldPermission`) NÃO são aplicadas no backend.** Só perfil (`ROLE_ADMIN/MANAGER/USER`) é checado. Endpoint sensível novo → no mínimo exigir `ROLE_ADMIN`.
- **Escrita em Task/Delivery/Billing é ADMIN-only** (desde 2026-05): create/update/delete/bulk de `TaskController`, `DeliveryController`, `BillingPeriodController` e `BillingPeriodTaskController` (vincular/desvincular) usam `@PreAuthorize("hasRole('ADMIN')")`; e-mail de tarefa idem. **GETs/listas permanecem mais abertos** (USER lista tarefas/entregas; MANAGER lista billing) — só a escrita é restrita. Ao criar endpoint de escrita nessas áreas, usar `hasRole('ADMIN')`.
- **Valores monetários são filtrados por perfil no backend.** Use `SecurityUtils.canViewMonetaryValues()` (true para ADMIN ou MANAGER; false para USER). Aplicado em: `TaskController` (anula `amount`/subtask amounts + `total-amount`=0 p/ USER), `DeliveryController` (anula `taskValue` + `total-amount`=0), relatórios PDF (task e delivery recebem `showValues`) e exports Excel (derivam `canViewAmounts` do perfil, ignorando flag do cliente). **Ao criar endpoint/relatório que exponha valor, gatear por `SecurityUtils`.** ⚠️ **Em PDFs Jasper, gatear cada elemento que mostra valor — INCLUSIVE em subreports.** O parâmetro `showValues` precisa ser **consumido** em cada `<rectangle>`/`<textField>`/`<staticText>` (`<printWhenExpression><![CDATA[Boolean.TRUE.equals($P{showValues})]]></printWhenExpression>` dentro do `<reportElement>`). Não basta o report principal gatear: o `task_subtasks_report.jrxml` esqueceu a faixa final do `<summary>` e vazou "VALOR TOTAL DA TAREFA" pra USER no PDF de tarefas com subtarefas (corrigido 2026-05-25). Padrões safe: `delivery_report.jrxml` usa `taskAmount=null` + `printWhenExpression="$P{taskAmount} != null"` na banda inteira; `operational_report` é bloqueado por role no controller.
- **`BillingPeriod.status` é String PT-BR** (`PENDENTE/FATURADO/PAGO/ATRASADO/CANCELADO`), não enum. Filtros e agregações usam esses literais.
- **Módulo & Servidor são cadastros (FK na Task), desde 2026-05.** Entidades `SystemModule` (tabela `module`) e `Server` (tabela `server`, com `link`); CRUD em `/api/modules` e `/api/servers` (molde Project). A `Task` referencia por `@ManyToOne` (`module_id`/`server_id`). **As colunas antigas `task.system_module`/`task.server_origin` foram removidas da entidade mas continuam ÓRFÃS no banco** (ddl-auto não derruba) — não reusar. `TaskResponse`/`DeliveryResponse` expõem `moduleName/serverName/serverLink`. Filtro por `moduleId`/`serverId` em `/api/tasks` e `/api/deliveries`. **Relatórios:** PDF/Excel alimentam Módulo/Servidor pelo nome do cadastro (no Excel de tarefa via subquery `(SELECT name FROM module/server WHERE id = t.module_id/server_id)`; o PDF de entrega exigiu editar `delivery_report.jrxml` + **regenerar `delivery_report.jasper`**).
- **`@RequestParam List<String> sort` quebra** com formato `campo,direção` (o Spring faz split por vírgula → trata "asc" como campo). Ler sort via `MultiValueMap<String,String> params` + `params.get("sort")` (padrão de Task/Delivery/Profile controllers).
- **Sem refresh token.** JWT 24h fixo. Não criar feature que assuma sessão > 24h sem alinhar.
- **Cache em uso é Caffeine**, não Redis (Redis está comentado em `application.yml`). Cluster com >1 réplica seria problemático.
- **Sync ClickUp/GitHub sem retry/circuit breaker.** Falha → log `[ERRO]` e segue.
- **Logs de sync têm formato fixo** — manter prefixos `=== INICIO ===`, `[SUCESSO]`, `[ERRO]`, `[PROCESSANDO]`, `[PULADO]`, `=== FIM ===`. Queries Loki/Grafana dependem disso. Ver `README.md` seção "Visualização de Logs".
- **Relatórios Jasper carregam o `.jasper` pré-compilado** (em `resources/reports/`), e só caem no `.jrxml` se o `.jasper` não existir (ver `ReportServiceImpl.load*JasperReport`). **Editar o `.jrxml` NÃO tem efeito até regenerar o `.jasper`.** Regenerar: `JasperCompileManager.compileReportToFile(jrxml, jasper)` rodando com o classpath do projeto (classe temporária `CompileJasper` + `mvnw dependency:build-classpath` p/ montar o cp; precisa de `target/classes` no cp quando o jrxml importa classe do projeto, ex.: `OperationalReportRow`; JAVA_HOME=`C:/jdk17.0.16_8`). **Copiar o `.jasper` novo para `src/main/resources/reports/` E `target/classes/reports/`.** Mudar só o VALOR de um parâmetro existente não precisa recompilar; adicionar parâmetro/campo no layout, sim. **Atalho** (quando não pode rodar `./mvnw`): apagar o `.jasper` em ambos os diretórios — o loader cai em `JasperCompileManager.compileReport(jrxml)` em runtime; JasperReports 6.21.2 já traz compilador Groovy embutido, basta restart do backend.
- **Design unificado dos PDFs (desde 2026-05):** `task_report`, `delivery_report` e `operational_report` (Estatísticas) foram redesenhados com a mesma linguagem limpa — barra accent fina `#4F5EE6` no topo (não bloco colorido cheio), títulos de seção uppercase accent + linha sublinhada, grid de 2 colunas (label cinza `#6B7280` em cima / valor `#111827` embaixo), valores monetários à direita, card de valor suave `#F3F4FF`, rodapé com linha accent. No `operational_report` os valores eram **cortados** → corrigido empilhando quantidade (cima) e `R$` (baixo) em colunas de 87px somando 555. Fontes via `DejaVu Sans`/`DejaVu Sans Mono` (família vem do artefato `jasperreports-fonts`).
- **Descrição/observações rich-text nos PDFs** passam por `HtmlImageExtractor.parseHtmlToBlocks` → `cleanHtmlForPdf` (vira `ContentBlock`s renderizados pelo `content_blocks_subreport` com `markup="html"`). **Blocos de código (`<pre>`)** são protegidos antes da limpeza e renderizados monoespaçados (`DejaVu Sans Mono`) preservando indentação (`&nbsp;`) e quebras (`<br/>`). ⚠️ **Gotcha:** no `markup="html"` do Jasper o `<font size="N">` usa a **escala HTML 1–7** (7 = enorme), não pontos — para mudar o tamanho do código herdar o do campo (não setar `size`) ou usar CSS `style="font-size:..."`.
- **Allowlist de tipos de anexo fica em 5 lugares idênticos:** `TaskAttachmentServiceImpl`, `SubTaskAttachmentServiceImpl`, `DeliveryAttachmentServiceImpl`, `DeliveryItemAttachmentServiceImpl`, `DeliveryOperationalAttachmentServiceImpl` (método `validateFile` → `isAllowedContentType`/`isAllowedByExtension`; `.sql`/`.json` e afins caem no fallback por **extensão** pois o content-type não é reconhecido). **Adicionar um tipo = mexer nos 5** (+ no front). Faturamento (`BillingPeriodAttachmentServiceImpl`) tem allowlist própria, à parte.

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
| Permissão de acesso | Backend valida só perfil (`@PreAuthorize`). |
| Expor/ocultar valor monetário | `SecurityUtils.canViewMonetaryValues()` (ADMIN/MANAGER) no controller/relatório |
| Template de email | `src/main/resources/templates/email/*.html` (Thymeleaf) |
| Dashboard | `DashboardServiceImpl` só retorna `recentActivities`; métricas/valores vêm de `/tasks/stats`, `/deliveries/stats`, `/billing-periods` (o frontend agrega) |

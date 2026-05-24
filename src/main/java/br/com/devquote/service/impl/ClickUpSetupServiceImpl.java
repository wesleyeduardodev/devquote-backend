package br.com.devquote.service.impl;

import br.com.devquote.dto.request.ClickUpSetupSaveRequest;
import br.com.devquote.dto.request.SystemParameterRequest;
import br.com.devquote.dto.response.ClickUpSetupFieldsResponse;
import br.com.devquote.dto.response.ClickUpSetupItemResponse;
import br.com.devquote.dto.response.ClickUpSetupUserResponse;
import br.com.devquote.entity.SystemParameter;
import br.com.devquote.repository.SystemParameterRepository;
import br.com.devquote.service.ClickUpSetupService;
import br.com.devquote.service.PriorityBoardService;
import br.com.devquote.service.SystemParameterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickUpSetupServiceImpl implements ClickUpSetupService {

    private static final String API_BASE = "https://api.clickup.com/api/v2";

    // Aliases pra auto-detect de fields (case-insensitive)
    private static final List<String> DEV_FIELD_ALIASES = List.of(
            "desenvolvedor", "developer", "dev", "responsável dev", "responsavel dev"
    );
    private static final List<String> ORDER_FIELD_ALIASES = List.of(
            "ordem", "order", "priority", "prioridade"
    );

    private final RestTemplate clickUpRestTemplate;
    private final SystemParameterService systemParameterService;
    private final SystemParameterRepository systemParameterRepository;
    private final PriorityBoardService priorityBoardService;

    // ========================================================================
    // Endpoints do wizard
    // ========================================================================

    @Override
    @SuppressWarnings("unchecked")
    public ClickUpSetupUserResponse validateToken(String token) {
        Map<String, Object> body = doGet(token, "/user");
        Object userObj = body.get("user");
        if (!(userObj instanceof Map)) {
            throw new ResponseStatusException(BAD_REQUEST, "Resposta inesperada do ClickUp");
        }
        Map<String, Object> user = (Map<String, Object>) userObj;
        return ClickUpSetupUserResponse.builder()
                .id(asString(user.get("id")))
                .username(asString(user.get("username")))
                .email(asString(user.get("email")))
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ClickUpSetupItemResponse> listTeams(String token) {
        Map<String, Object> body = doGet(token, "/team");
        List<Map<String, Object>> teams = (List<Map<String, Object>>) body.getOrDefault("teams", List.of());
        List<ClickUpSetupItemResponse> result = new ArrayList<>(teams.size());
        for (Map<String, Object> t : teams) {
            result.add(ClickUpSetupItemResponse.builder()
                    .id(asString(t.get("id")))
                    .name(asString(t.get("name")))
                    .build());
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ClickUpSetupItemResponse> listSpaces(String token, String teamId) {
        Map<String, Object> body = doGet(token, "/team/" + teamId + "/space?archived=false");
        List<Map<String, Object>> spaces = (List<Map<String, Object>>) body.getOrDefault("spaces", List.of());
        List<ClickUpSetupItemResponse> result = new ArrayList<>(spaces.size());
        for (Map<String, Object> s : spaces) {
            result.add(ClickUpSetupItemResponse.builder()
                    .id(asString(s.get("id")))
                    .name(asString(s.get("name")))
                    .build());
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ClickUpSetupItemResponse> listLists(String token, String spaceId) {
        List<ClickUpSetupItemResponse> result = new ArrayList<>();

        // 1) Folderless lists
        Map<String, Object> folderless = doGet(token, "/space/" + spaceId + "/list?archived=false");
        List<Map<String, Object>> fl = (List<Map<String, Object>>) folderless.getOrDefault("lists", List.of());
        for (Map<String, Object> l : fl) {
            result.add(ClickUpSetupItemResponse.builder()
                    .id(asString(l.get("id")))
                    .name(asString(l.get("name")))
                    .parent(null)
                    .build());
        }

        // 2) Folders → lists dentro de cada folder
        Map<String, Object> foldersBody = doGet(token, "/space/" + spaceId + "/folder?archived=false");
        List<Map<String, Object>> folders = (List<Map<String, Object>>) foldersBody.getOrDefault("folders", List.of());
        for (Map<String, Object> folder : folders) {
            String folderName = asString(folder.get("name"));
            List<Map<String, Object>> lists = (List<Map<String, Object>>) folder.getOrDefault("lists", List.of());
            for (Map<String, Object> l : lists) {
                result.add(ClickUpSetupItemResponse.builder()
                        .id(asString(l.get("id")))
                        .name(asString(l.get("name")))
                        .parent(folderName)
                        .build());
            }
        }

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ClickUpSetupItemResponse> listSharedLists(String token, String teamId) {
        Map<String, Object> body = doGet(token, "/team/" + teamId + "/shared");
        List<ClickUpSetupItemResponse> result = new ArrayList<>();
        Object sharedObj = body.get("shared");
        if (!(sharedObj instanceof Map)) return result;

        Map<String, Object> shared = (Map<String, Object>) sharedObj;

        // Lists compartilhadas diretamente
        List<Map<String, Object>> lists = (List<Map<String, Object>>) shared.getOrDefault("lists", List.of());
        for (Map<String, Object> l : lists) {
            result.add(ClickUpSetupItemResponse.builder()
                    .id(asString(l.get("id")))
                    .name(asString(l.get("name")))
                    .parent("Compartilhada")
                    .build());
        }

        // Folders compartilhados — pra cada folder, suas lists
        List<Map<String, Object>> folders = (List<Map<String, Object>>) shared.getOrDefault("folders", List.of());
        for (Map<String, Object> folder : folders) {
            String folderName = asString(folder.get("name"));
            List<Map<String, Object>> folderLists = (List<Map<String, Object>>) folder.getOrDefault("lists", List.of());
            for (Map<String, Object> l : folderLists) {
                result.add(ClickUpSetupItemResponse.builder()
                        .id(asString(l.get("id")))
                        .name(asString(l.get("name")))
                        .parent("Compartilhada · " + folderName)
                        .build());
            }
        }

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ClickUpSetupFieldsResponse listFields(String token, String listId) {
        Map<String, Object> body = doGet(token, "/list/" + listId + "/field");
        List<Map<String, Object>> raw = (List<Map<String, Object>>) body.getOrDefault("fields", List.of());

        List<ClickUpSetupFieldsResponse.Field> all = new ArrayList<>(raw.size());
        for (Map<String, Object> f : raw) {
            String type = asString(f.get("type"));
            List<ClickUpSetupFieldsResponse.Option> options = null;
            if ("drop_down".equalsIgnoreCase(type)) {
                Object cfg = f.get("type_config");
                if (cfg instanceof Map) {
                    Object optsObj = ((Map<String, Object>) cfg).get("options");
                    if (optsObj instanceof List) {
                        options = new ArrayList<>();
                        for (Object o : (List<Object>) optsObj) {
                            if (o instanceof Map) {
                                Map<String, Object> om = (Map<String, Object>) o;
                                options.add(ClickUpSetupFieldsResponse.Option.builder()
                                        .id(asString(om.get("id")))
                                        .name(asString(om.get("name")))
                                        .build());
                            }
                        }
                    }
                }
            }
            all.add(ClickUpSetupFieldsResponse.Field.builder()
                    .id(asString(f.get("id")))
                    .name(asString(f.get("name")))
                    .type(type)
                    .options(options)
                    .build());
        }

        // Auto-detect dos fields esperados
        ClickUpSetupFieldsResponse.Field developerField = findByNameAlias(all, DEV_FIELD_ALIASES);
        ClickUpSetupFieldsResponse.Field orderField = findByNameAlias(all, ORDER_FIELD_ALIASES);

        // Auto-detect da opção do user dentro do dev field, baseado no username
        String developerOptionId = null;
        if (developerField != null && developerField.getOptions() != null) {
            String username = currentUsername(token);
            if (username != null && !username.isBlank()) {
                String unameLower = username.toLowerCase();
                for (ClickUpSetupFieldsResponse.Option opt : developerField.getOptions()) {
                    String optName = opt.getName() == null ? "" : opt.getName().toLowerCase();
                    if (optName.isBlank()) continue;
                    // Match: opção contida no username OU username contido no nome da opção
                    if (unameLower.contains(optName) || optName.contains(unameLower)) {
                        developerOptionId = opt.getId();
                        break;
                    }
                    // Match: primeiro nome
                    String firstName = username.split("\\s+")[0].toLowerCase();
                    if (!firstName.isBlank() && optName.equals(firstName)) {
                        developerOptionId = opt.getId();
                        break;
                    }
                }
            }
        }

        return ClickUpSetupFieldsResponse.builder()
                .all(all)
                .suggestedDeveloperFieldId(developerField != null ? developerField.getId() : null)
                .suggestedDeveloperOptionId(developerOptionId)
                .suggestedOrderFieldId(orderField != null ? orderField.getId() : null)
                .build();
    }

    @Override
    public void save(ClickUpSetupSaveRequest request) {
        // ===== Núcleo (sempre sobrescreve — são os 6 que o wizard gerencia) =====
        upsert("CLICKUP_INTEGRATION_ENABLED", "true",
                "Liga/desliga a integração com o ClickUp. true/false.");
        upsert("CLICKUP_TOKEN", request.getToken(),
                "Token pessoal do ClickUp (formato pk_*).");
        upsert("CLICKUP_BOARD_LIST_ID", request.getListId(),
                "ID da lista do ClickUp usada na tela /priorities.");
        upsert("CLICKUP_DEVELOPER_FIELD_ID", request.getDeveloperFieldId(),
                "ID do custom field Desenvolvedor (drop_down) no ClickUp.");
        upsert("CLICKUP_DEVELOPER_OPTION_ID", request.getDeveloperOptionId(),
                "ID da opção (você) dentro do custom field Desenvolvedor.");
        if (request.getOrderFieldId() != null && !request.getOrderFieldId().isBlank()) {
            upsert("CLICKUP_ORDER_FIELD_ID", request.getOrderFieldId(),
                    "Custom field Ordem (number) que ordena as tarefas no board.");
        }

        // Os opcionais (TASK_BOARD_PROVIDER, CLICKUP_PRIMARY_STATUS, CLICKUP_PRIORITY_STATUSES,
        // CLICKUP_BOARD_ASSIGNEE_USER_ID, CLICKUP_HIDDEN_STATUSES) NÃO são criados aqui.
        // Defaults sensatos vivem no código (TaskBoardParameterHelper). Quando o user
        // interagir com a UI do board (drag pra reordenar, marcar principal, ocultar),
        // os parâmetros são criados sob demanda pela rota PUT /priorities/board/preferences.

        // Invalida cache do board pra próxima leitura usar a config nova
        priorityBoardService.evict();
        log.info("[clickup-setup] Configuração salva (listId={}, devField={}, devOption={}, orderField={})",
                request.getListId(), request.getDeveloperFieldId(),
                request.getDeveloperOptionId(), request.getOrderFieldId());
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    @SuppressWarnings("unchecked")
    private Map<String, Object> doGet(String token, String path) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Token vazio");
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            URI uri = URI.create(API_BASE + path);
            ResponseEntity<Map> response = clickUpRestTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);
            Map<String, Object> body = response.getBody();
            if (body == null) {
                throw new ResponseStatusException(BAD_REQUEST, "Resposta vazia do ClickUp");
            }
            return body;
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new ResponseStatusException(UNAUTHORIZED, "Token inválido — verifique se está correto");
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(BAD_REQUEST, "Erro do ClickUp: " + e.getStatusCode() + " " + e.getStatusText());
        } catch (Exception e) {
            log.warn("[clickup-setup] Falha em {}: {}", path, e.getMessage());
            throw new ResponseStatusException(BAD_REQUEST, "Falha ao acessar a API do ClickUp: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private String currentUsername(String token) {
        try {
            Map<String, Object> body = doGet(token, "/user");
            Object userObj = body.get("user");
            if (userObj instanceof Map) {
                return asString(((Map<String, Object>) userObj).get("username"));
            }
        } catch (Exception e) {
            log.debug("[clickup-setup] Não consegui descobrir username pra auto-detect: {}", e.getMessage());
        }
        return null;
    }

    private static ClickUpSetupFieldsResponse.Field findByNameAlias(List<ClickUpSetupFieldsResponse.Field> fields, List<String> aliases) {
        for (ClickUpSetupFieldsResponse.Field f : fields) {
            String name = f.getName() == null ? "" : f.getName().trim().toLowerCase();
            if (aliases.contains(name)) return f;
        }
        // Match parcial (contains): pega o primeiro que contenha algum alias
        for (ClickUpSetupFieldsResponse.Field f : fields) {
            String name = f.getName() == null ? "" : f.getName().trim().toLowerCase();
            for (String alias : aliases) {
                if (name.contains(alias)) return f;
            }
        }
        return null;
    }

    private static String asString(Object o) {
        return o == null ? null : o.toString();
    }

    private void upsert(String name, String value, String description) {
        if (value == null) value = "";
        Optional<SystemParameter> existing = systemParameterRepository.findByName(name);
        SystemParameterRequest dto = SystemParameterRequest.builder()
                .name(name)
                .value(value)
                .description(description)
                .isEncrypted(false)
                .build();
        if (existing.isPresent()) {
            systemParameterService.update(existing.get().getId(), dto);
        } else {
            systemParameterService.create(dto);
        }
    }

    /** Cria o parâmetro só se ainda não existir — preserva customizações já feitas. */
    private void createIfMissing(String name, String value, String description) {
        if (systemParameterRepository.findByName(name).isPresent()) return;
        systemParameterService.create(SystemParameterRequest.builder()
                .name(name)
                .value(value == null ? "" : value)
                .description(description)
                .isEncrypted(false)
                .build());
    }

    /**
     * Todos os parâmetros relacionados à integração ClickUp/board de prioridades.
     * Reset apaga todos eles — começa 100% do zero, como um tenant novo.
     */
    private static final List<String> ALL_CLICKUP_PARAM_NAMES = List.of(
            // Núcleo da integração (criados pelo wizard)
            "CLICKUP_INTEGRATION_ENABLED",
            "CLICKUP_TOKEN",
            "CLICKUP_BOARD_LIST_ID",
            "CLICKUP_DEVELOPER_FIELD_ID",
            "CLICKUP_DEVELOPER_OPTION_ID",
            "CLICKUP_ORDER_FIELD_ID",
            // Provider e preferências do board (criados via UI / preferences)
            "TASK_BOARD_PROVIDER",
            "CLICKUP_PRIMARY_STATUS",
            "CLICKUP_PRIORITY_STATUSES",
            "CLICKUP_BOARD_ASSIGNEE_USER_ID",
            "CLICKUP_HIDDEN_STATUSES"
    );

    @Override
    public void reset() {
        int deleted = 0;
        for (String name : ALL_CLICKUP_PARAM_NAMES) {
            Optional<SystemParameter> existing = systemParameterRepository.findByName(name);
            if (existing.isPresent()) {
                systemParameterService.delete(existing.get().getId());
                deleted++;
            }
        }
        priorityBoardService.evict();
        log.info("[clickup-setup] Reset executado — {} parâmetro(s) ClickUp removido(s). Volta ao estado de tenant novo.", deleted);
    }
}

package br.com.devquote.service.impl;

import br.com.devquote.client.board.BoardTask;
import br.com.devquote.client.board.TaskBoardProvider;
import br.com.devquote.client.board.TaskBoardProviderFactory;
import br.com.devquote.dto.response.PriorityBoardResponse;
import br.com.devquote.helper.TaskBoardParameterHelper;
import br.com.devquote.repository.TaskRepository;
import br.com.devquote.service.PriorityBoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriorityBoardServiceImpl implements PriorityBoardService {

    private final TaskBoardProviderFactory providerFactory;
    private final TaskBoardParameterHelper config;
    private final TaskRepository taskRepository;

    @Override
    @Cacheable(value = "priorityBoard", key = "'board-' + #includeAssignee")
    public PriorityBoardResponse getBoard(boolean includeAssignee) {
        String fetchedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        Optional<TaskBoardProvider> active = providerFactory.getActiveProvider();
        if (active.isEmpty() || !active.get().isConfigured()) {
            return PriorityBoardResponse.builder()
                    .provider(active.map(TaskBoardProvider::getProviderName).orElse(config.getProvider()))
                    .configured(false)
                    .fetchedAt(fetchedAt)
                    .groups(new ArrayList<>())
                    .build();
        }

        TaskBoardProvider provider = active.get();
        List<BoardTask> tasks = provider.fetchPriorityTasks(includeAssignee);

        // Quais códigos (id do ClickUp) já existem como Task no DevQuote
        Set<String> existingCodes = new HashSet<>();
        List<String> ids = tasks.stream().map(BoardTask::getId).filter(java.util.Objects::nonNull).collect(Collectors.toList());
        if (!ids.isEmpty()) {
            existingCodes.addAll(taskRepository.findExistingCodes(ids));
        }

        List<String> statusOrder = config.getPriorityStatuses();
        Set<String> hidden = new HashSet<>(config.getHiddenStatuses());
        String primaryStatus = config.getPrimaryStatus();

        // Mapa case-insensitive: status do ClickUp (case original) → posição na ordem preferencial
        Map<String, Integer> orderByLower = new java.util.HashMap<>();
        for (int i = 0; i < statusOrder.size(); i++) {
            orderByLower.put(statusOrder.get(i).toLowerCase(), i);
        }

        // Agrupa por status, mantendo o NOME ORIGINAL vindo do ClickUp como chave do grupo
        Map<String, List<BoardTask>> byStatus = new LinkedHashMap<>();
        // Primeiro popula os status configurados (mantendo a ordem, mesmo se vazios)
        for (String s : statusOrder) {
            byStatus.put(s, new ArrayList<>());
        }
        // Depois inclui as tarefas — agrupando pelo nome real que veio do ClickUp,
        // mas se já existir um bucket case-insensitive (do statusOrder), reusa.
        for (BoardTask t : tasks) {
            String status = t.getStatusName();
            if (status == null || status.trim().isEmpty()) continue;
            if (hidden.contains(status.toLowerCase())) continue;

            // procura bucket existente case-insensitive
            String bucketKey = byStatus.keySet().stream()
                    .filter(k -> k.equalsIgnoreCase(status))
                    .findFirst()
                    .orElse(status);
            byStatus.computeIfAbsent(bucketKey, k -> new ArrayList<>()).add(t);
        }

        // Ordenação final: pela posição no statusOrder; quem não está vai pro fim,
        // ordenado alfabeticamente entre si (estável + previsível).
        List<Map.Entry<String, List<BoardTask>>> sortedEntries = new ArrayList<>(byStatus.entrySet());
        sortedEntries.sort((a, b) -> {
            Integer ia = orderByLower.get(a.getKey().toLowerCase());
            Integer ib = orderByLower.get(b.getKey().toLowerCase());
            if (ia != null && ib != null) return Integer.compare(ia, ib);
            if (ia != null) return -1;
            if (ib != null) return 1;
            return a.getKey().compareToIgnoreCase(b.getKey());
        });

        List<PriorityBoardResponse.Group> groups = new ArrayList<>();
        for (Map.Entry<String, List<BoardTask>> entry : sortedEntries) {
            List<BoardTask> groupTasks = entry.getValue();
            groupTasks.sort(Comparator.comparing(
                    BoardTask::getOrderValue,
                    Comparator.nullsLast(Comparator.naturalOrder())));

            List<PriorityBoardResponse.Task> mapped = new ArrayList<>();
            for (BoardTask t : groupTasks) {
                mapped.add(PriorityBoardResponse.Task.builder()
                        .id(t.getId())
                        .name(t.getName())
                        .description(t.getDescription())
                        .url(t.getUrl())
                        .ordem(t.getOrderValue())
                        .priority(t.getPriority())
                        .type(t.getType())
                        .tags(t.getTags())
                        .existsInDevQuote(t.getId() != null && existingCodes.contains(t.getId()))
                        .build());
            }

            groups.add(PriorityBoardResponse.Group.builder()
                    .status(entry.getKey())
                    .primary(entry.getKey() != null && entry.getKey().equalsIgnoreCase(primaryStatus))
                    .count(mapped.size())
                    .tasks(mapped)
                    .build());
        }

        // Info do user conectado (UI mostra "Conectado como X")
        PriorityBoardResponse.CurrentUser currentUser = null;
        Map<String, Object> u = provider.getCurrentUser();
        if (u != null) {
            currentUser = PriorityBoardResponse.CurrentUser.builder()
                    .id(u.get("id") != null ? u.get("id").toString() : null)
                    .username(u.get("username") != null ? u.get("username").toString() : null)
                    .email(u.get("email") != null ? u.get("email").toString() : null)
                    .build();
        }

        return PriorityBoardResponse.builder()
                .provider(provider.getProviderName())
                .configured(true)
                .fetchedAt(fetchedAt)
                .groups(groups)
                .currentUser(currentUser)
                .build();
    }

    @Override
    @CacheEvict(value = "priorityBoard", allEntries = true)
    public void evict() {
        log.info("Cache 'priorityBoard' invalidado (refresh manual).");
    }
}

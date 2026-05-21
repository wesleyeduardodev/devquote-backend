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
    @Cacheable(value = "priorityBoard", key = "'board'")
    public PriorityBoardResponse getBoard() {
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
        List<BoardTask> tasks = provider.fetchPriorityTasks();

        // Quais códigos (id do ClickUp) já existem como Task no DevQuote
        Set<String> existingCodes = new HashSet<>();
        List<String> ids = tasks.stream().map(BoardTask::getId).filter(java.util.Objects::nonNull).collect(Collectors.toList());
        if (!ids.isEmpty()) {
            existingCodes.addAll(taskRepository.findExistingCodes(ids));
        }

        List<String> statusOrder = config.getPriorityStatuses();
        String primaryStatus = config.getPrimaryStatus();

        // Agrupa por status mantendo a ordem configurada (primary primeiro na config).
        Map<String, List<BoardTask>> byStatus = new LinkedHashMap<>();
        for (String s : statusOrder) {
            byStatus.put(s, new ArrayList<>());
        }
        for (BoardTask t : tasks) {
            byStatus.computeIfAbsent(t.getStatusName(), k -> new ArrayList<>()).add(t);
        }

        List<PriorityBoardResponse.Group> groups = new ArrayList<>();
        for (Map.Entry<String, List<BoardTask>> entry : byStatus.entrySet()) {
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

        return PriorityBoardResponse.builder()
                .provider(provider.getProviderName())
                .configured(true)
                .fetchedAt(fetchedAt)
                .groups(groups)
                .build();
    }

    @Override
    @CacheEvict(value = "priorityBoard", allEntries = true)
    public void evict() {
        log.info("Cache 'priorityBoard' invalidado (refresh manual).");
    }
}

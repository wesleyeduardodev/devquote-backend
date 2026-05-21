package br.com.devquote.client.board;

import br.com.devquote.helper.TaskBoardParameterHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Seleciona o {@link TaskBoardProvider} ativo a partir do parâmetro
 * TASK_BOARD_PROVIDER (default CLICKUP). Espelha o padrão de GitProviderFactory.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TaskBoardProviderFactory {

    private final List<TaskBoardProvider> providers;
    private final TaskBoardParameterHelper parameterHelper;

    public Optional<TaskBoardProvider> getActiveProvider() {
        String key = parameterHelper.getProvider();
        return providers.stream()
                .filter(p -> p.supports(key))
                .findFirst();
    }
}

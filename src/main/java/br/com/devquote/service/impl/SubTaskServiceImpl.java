package br.com.devquote.service.impl;
import br.com.devquote.adapter.SubTaskAdapter;
import br.com.devquote.dto.request.SubTaskRequest;
import br.com.devquote.dto.response.SubTaskResponse;
import br.com.devquote.entity.SubTask;
import br.com.devquote.entity.Task;
import br.com.devquote.repository.SubTaskRepository;
import br.com.devquote.repository.TaskRepository;
import br.com.devquote.service.SubTaskService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SubTaskServiceImpl implements SubTaskService {

    private final SubTaskRepository subTaskRepository;
    private final TaskRepository taskRepository;

    @Override
    public List<SubTaskResponse> findAll() {
        return subTaskRepository.findAllOrderedById().stream()
                .map(SubTaskAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SubTaskResponse findById(Long id) {
        SubTask entity = subTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SubTask not found"));
        return SubTaskAdapter.toResponseDTO(entity);
    }

    @Override
    public SubTaskResponse create(SubTaskRequest dto) {
        Task task = taskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));
        SubTask entity = SubTaskAdapter.toEntity(dto, task);
        entity = subTaskRepository.save(entity);
        return SubTaskAdapter.toResponseDTO(entity);
    }

    @Override
    public SubTaskResponse update(Long id, SubTaskRequest dto) {
        SubTask entity = subTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SubTask not found"));
        Task task = taskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));
        SubTaskAdapter.updateEntityFromDto(dto, entity, task);
        entity = subTaskRepository.save(entity);
        return SubTaskAdapter.toResponseDTO(entity);
    }

    @Override
    public void delete(Long id) {
        // Busca a subtarefa antes de deletar para obter a tarefa associada
        SubTask subTask = subTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SubTask not found"));

        Task task = subTask.getTask();

        // Deleta a subtarefa
        subTaskRepository.deleteById(id);

        // Recalcula o valor total da tarefa (soma das subtarefas restantes)
        BigDecimal newAmount = subTaskRepository.findAll().stream()
                .filter(st -> st.getTask().getId().equals(task.getId()))
                .map(SubTask::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Atualiza o valor da tarefa
        task.setAmount(newAmount);
        taskRepository.save(task);
    }

    @Override
    public void deleteBulk(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // Busca todas as subtarefas antes de deletar para obter as tarefas associadas
        List<SubTask> subTasks = subTaskRepository.findAllById(ids);

        // Agrupa as subtarefas por tarefa
        var taskIds = subTasks.stream()
                .map(st -> st.getTask().getId())
                .distinct()
                .collect(Collectors.toList());

        // Deleta as subtarefas
        subTaskRepository.deleteAllById(ids);

        // Recalcula o valor para cada tarefa afetada
        for (Long taskId : taskIds) {
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("Task not found"));

            // Recalcula o valor total (soma das subtarefas restantes)
            BigDecimal newAmount = subTaskRepository.findAll().stream()
                    .filter(st -> st.getTask().getId().equals(taskId))
                    .map(SubTask::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Atualiza o valor da tarefa
            task.setAmount(newAmount);
            taskRepository.save(task);
        }
    }
}

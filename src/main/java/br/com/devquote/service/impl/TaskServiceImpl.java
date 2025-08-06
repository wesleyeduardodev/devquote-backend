package br.com.devquote.service.impl;
import br.com.devquote.adapter.TaskAdapter;
import br.com.devquote.dto.request.TaskRequestDTO;
import br.com.devquote.dto.response.TaskResponseDTO;
import br.com.devquote.entity.Requester;
import br.com.devquote.entity.Task;
import br.com.devquote.repository.RequesterRepository;
import br.com.devquote.repository.TaskRepository;
import br.com.devquote.service.TaskService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final RequesterRepository requesterRepository;

    @Override
    public List<TaskResponseDTO> findAll() {
        return taskRepository.findAll().stream()
                .map(TaskAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TaskResponseDTO findById(Long id) {
        Task entity = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        return TaskAdapter.toResponseDTO(entity);
    }

    @Override
    public TaskResponseDTO create(TaskRequestDTO dto) {
        Requester requester = requesterRepository.findById(dto.getRequesterId())
                .orElseThrow(() -> new RuntimeException("Requester not found"));
        Task entity = TaskAdapter.toEntity(dto, requester);
        entity = taskRepository.save(entity);
        return TaskAdapter.toResponseDTO(entity);
    }

    @Override
    public TaskResponseDTO update(Long id, TaskRequestDTO dto) {
        Task entity = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        Requester requester = requesterRepository.findById(dto.getRequesterId())
                .orElseThrow(() -> new RuntimeException("Requester not found"));
        TaskAdapter.updateEntityFromDto(dto, entity, requester);
        entity = taskRepository.save(entity);
        return TaskAdapter.toResponseDTO(entity);
    }

    @Override
    public void delete(Long id) {
        taskRepository.deleteById(id);
    }
}
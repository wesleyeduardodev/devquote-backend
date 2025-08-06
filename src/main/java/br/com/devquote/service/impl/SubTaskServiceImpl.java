package br.com.devquote.service.impl;
import br.com.devquote.adapter.SubTaskAdapter;
import br.com.devquote.dto.request.SubTaskRequestDTO;
import br.com.devquote.dto.response.SubTaskResponseDTO;
import br.com.devquote.entity.SubTask;
import br.com.devquote.entity.Task;
import br.com.devquote.repository.SubTaskRepository;
import br.com.devquote.repository.TaskRepository;
import br.com.devquote.service.SubTaskService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SubTaskServiceImpl implements SubTaskService {

    private final SubTaskRepository subTaskRepository;
    private final TaskRepository taskRepository;

    @Override
    public List<SubTaskResponseDTO> findAll() {
        return subTaskRepository.findAll().stream()
                .map(SubTaskAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SubTaskResponseDTO findById(Long id) {
        SubTask entity = subTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SubTask not found"));
        return SubTaskAdapter.toResponseDTO(entity);
    }

    @Override
    public SubTaskResponseDTO create(SubTaskRequestDTO dto) {
        Task task = taskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));
        SubTask entity = SubTaskAdapter.toEntity(dto, task);
        entity = subTaskRepository.save(entity);
        return SubTaskAdapter.toResponseDTO(entity);
    }

    @Override
    public SubTaskResponseDTO update(Long id, SubTaskRequestDTO dto) {
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
        subTaskRepository.deleteById(id);
    }
}
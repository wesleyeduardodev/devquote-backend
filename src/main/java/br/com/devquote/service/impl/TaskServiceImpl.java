package br.com.devquote.service.impl;
import br.com.devquote.adapter.SubTaskAdapter;
import br.com.devquote.adapter.TaskAdapter;
import br.com.devquote.dto.request.TaskRequestDTO;
import br.com.devquote.dto.request.TaskWithSubTasksRequestDTO;
import br.com.devquote.dto.response.TaskResponseDTO;
import br.com.devquote.dto.response.TaskWithSubTasksResponseDTO;
import br.com.devquote.entity.Requester;
import br.com.devquote.entity.SubTask;
import br.com.devquote.entity.Task;
import br.com.devquote.repository.RequesterRepository;
import br.com.devquote.repository.SubTaskRepository;
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
    private final SubTaskRepository subTaskRepository;

    @Override
    public List<TaskResponseDTO> findAll() {

        List<TaskResponseDTO> collect = taskRepository.findAll().stream()
                .map(TaskAdapter::toResponseDTO)
                .collect(Collectors.toList());

        collect.forEach(taskResponseDTO -> {
            List<SubTask> subTasks = subTaskRepository.findByTaskId(taskResponseDTO.getId());
            taskResponseDTO.setSubTasks(SubTaskAdapter.toResponseDTOList(subTasks));
        });

        return collect;
    }

    @Override
    public TaskResponseDTO findById(Long id) {

        Task entity = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        TaskResponseDTO responseDTO = TaskAdapter.toResponseDTO(entity);

        List<SubTask> subTasks = subTaskRepository.findByTaskId(responseDTO.getId());
        responseDTO.setSubTasks(SubTaskAdapter.toResponseDTOList(subTasks));

        return responseDTO;
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

    @Override
    public TaskWithSubTasksResponseDTO createWithSubTasks(TaskWithSubTasksRequestDTO dto) {

        Requester requester = requesterRepository.findById(dto.getRequesterId())
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        Task task = TaskAdapter.toEntity(TaskRequestDTO.builder()
                .requesterId(dto.getRequesterId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(dto.getStatus())
                .code(dto.getCode())
                .link(dto.getLink())
                .build(), requester);

        task = taskRepository.save(task);

        Task finalTask = task;
        List<SubTask> subTasks = dto.getSubTasks().stream()
                .map(subTaskDTO -> {
                    SubTask subTask = SubTaskAdapter.toEntity(subTaskDTO, finalTask);
                    return subTaskRepository.save(subTask);
                })
                .toList();

        return TaskWithSubTasksResponseDTO.builder()
                .id(task.getId())
                .requesterId(task.getRequester().getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .code(task.getCode())
                .link(task.getLink())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .subTasks(subTasks.stream().map(SubTaskAdapter::toResponseDTO).toList())
                .build();
    }
}
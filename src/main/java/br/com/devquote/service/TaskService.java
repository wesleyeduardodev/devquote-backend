package br.com.devquote.service;
import br.com.devquote.dto.request.TaskRequestDTO;
import br.com.devquote.dto.request.TaskWithSubTasksRequestDTO;
import br.com.devquote.dto.request.TaskWithSubTasksUpdateRequestDTO;
import br.com.devquote.dto.response.TaskResponseDTO;
import br.com.devquote.dto.response.TaskWithSubTasksResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface TaskService {

    List<TaskResponseDTO> findAll();

    TaskResponseDTO findById(Long id);

    TaskResponseDTO create(TaskRequestDTO dto);

    TaskResponseDTO update(Long id, TaskRequestDTO dto);

    void delete(Long id);

    TaskWithSubTasksResponseDTO createWithSubTasks(TaskWithSubTasksRequestDTO dto);

    TaskWithSubTasksResponseDTO updateWithSubTasks(Long taskId, TaskWithSubTasksUpdateRequestDTO dto);

    void deleteTaskWithSubTasks(Long taskId);

    Page<TaskResponseDTO> findAllPaginated(Long id,
                                           Long requesterId,
                                           String requesterName,
                                           String title,
                                           String description,
                                           String status,
                                           String code,
                                           String link,
                                           String createdAt,
                                           String updatedAt,
                                           Pageable pageable);
}
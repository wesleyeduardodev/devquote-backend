package br.com.devquote.service;
import br.com.devquote.dto.request.TaskRequest;
import br.com.devquote.dto.request.TaskWithSubTasksCreateRequest;
import br.com.devquote.dto.request.TaskWithSubTasksUpdateRequest;
import br.com.devquote.dto.response.TaskResponse;
import br.com.devquote.dto.response.TaskWithSubTasksResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface TaskService {

    List<TaskResponse> findAll();

    TaskResponse findById(Long id);

    TaskResponse create(TaskRequest dto);

    TaskResponse update(Long id, TaskRequest dto);

    void delete(Long id);

    void deleteBulk(List<Long> ids);

    TaskWithSubTasksResponse createWithSubTasks(TaskWithSubTasksCreateRequest dto);

    TaskWithSubTasksResponse updateWithSubTasks(Long taskId, TaskWithSubTasksUpdateRequest dto);

    void deleteTaskWithSubTasks(Long taskId);

    Page<TaskResponse> findAllPaginated(Long id,
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
package br.com.devquote.service;
import br.com.devquote.dto.request.TaskRequestDTO;
import br.com.devquote.dto.request.TaskWithSubTasksRequestDTO;
import br.com.devquote.dto.response.TaskResponseDTO;
import br.com.devquote.dto.response.TaskWithSubTasksResponseDTO;

import java.util.List;

public interface TaskService {

    List<TaskResponseDTO> findAll();

    TaskResponseDTO findById(Long id);

    TaskResponseDTO create(TaskRequestDTO dto);

    TaskResponseDTO update(Long id, TaskRequestDTO dto);

    void delete(Long id);

    TaskWithSubTasksResponseDTO createWithSubTasks(TaskWithSubTasksRequestDTO dto);
}
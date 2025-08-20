package br.com.devquote.service;
import br.com.devquote.dto.request.SubTaskRequest;
import br.com.devquote.dto.response.SubTaskResponse;
import java.util.List;

public interface SubTaskService {
    List<SubTaskResponse> findAll();
    SubTaskResponse findById(Long id);
    SubTaskResponse create(SubTaskRequest dto);
    SubTaskResponse update(Long id, SubTaskRequest dto);
    void delete(Long id);
}
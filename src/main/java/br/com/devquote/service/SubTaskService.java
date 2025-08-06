package br.com.devquote.service;
import br.com.devquote.dto.request.SubTaskRequestDTO;
import br.com.devquote.dto.response.SubTaskResponseDTO;
import java.util.List;

public interface SubTaskService {
    List<SubTaskResponseDTO> findAll();
    SubTaskResponseDTO findById(Long id);
    SubTaskResponseDTO create(SubTaskRequestDTO dto);
    SubTaskResponseDTO update(Long id, SubTaskRequestDTO dto);
    void delete(Long id);
}
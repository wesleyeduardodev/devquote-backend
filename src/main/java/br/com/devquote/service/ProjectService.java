package br.com.devquote.service;
import br.com.devquote.dto.request.ProjectRequestDTO;
import br.com.devquote.dto.response.ProjectResponseDTO;
import java.util.List;

public interface ProjectService {
    List<ProjectResponseDTO> findAll();
    ProjectResponseDTO findById(Long id);
    ProjectResponseDTO create(ProjectRequestDTO dto);
    ProjectResponseDTO update(Long id, ProjectRequestDTO dto);
    void delete(Long id);
}
package br.com.devquote.service;
import br.com.devquote.dto.request.ProjectRequestDTO;
import br.com.devquote.dto.response.ProjectResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ProjectService {
    List<ProjectResponseDTO> findAll();

    ProjectResponseDTO findById(Long id);

    ProjectResponseDTO create(ProjectRequestDTO dto);

    ProjectResponseDTO update(Long id, ProjectRequestDTO dto);

    void delete(Long id);

    Page<ProjectResponseDTO> findAllPaginated(Long id,
                                              String name,
                                              String repositoryUrl,
                                              String createdAt,
                                              String updatedAt,
                                              Pageable pageable);
}

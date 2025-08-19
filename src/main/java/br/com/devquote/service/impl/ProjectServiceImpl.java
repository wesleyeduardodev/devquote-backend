package br.com.devquote.service.impl;
import br.com.devquote.adapter.ProjectAdapter;
import br.com.devquote.dto.request.ProjectRequestDTO;
import br.com.devquote.dto.response.ProjectResponseDTO;
import br.com.devquote.entity.Project;
import br.com.devquote.repository.ProjectRepository;
import br.com.devquote.service.ProjectService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    @Override
    public List<ProjectResponseDTO> findAll() {
        return projectRepository.findAllOrderedById().stream()
                .map(ProjectAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectResponseDTO findById(Long id) {
        Project entity = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return ProjectAdapter.toResponseDTO(entity);
    }

    @Override
    public ProjectResponseDTO create(ProjectRequestDTO dto) {
        Project entity = ProjectAdapter.toEntity(dto);
        entity = projectRepository.save(entity);
        return ProjectAdapter.toResponseDTO(entity);
    }

    @Override
    public ProjectResponseDTO update(Long id, ProjectRequestDTO dto) {
        Project entity = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        ProjectAdapter.updateEntityFromDto(dto, entity);
        entity = projectRepository.save(entity);
        return ProjectAdapter.toResponseDTO(entity);
    }

    @Override
    public void delete(Long id) {
        projectRepository.deleteById(id);
    }

    @Override
    public Page<ProjectResponseDTO> findAllPaginated(Long id,
                                                     String name,
                                                     String repositoryUrl,
                                                     String createdAt,
                                                     String updatedAt,
                                                     Pageable pageable) {
        Page<Project> page = projectRepository.findByOptionalFieldsPaginated(
                id, name, repositoryUrl, createdAt, updatedAt, pageable
        );
        return page.map(ProjectAdapter::toResponseDTO);
    }
}

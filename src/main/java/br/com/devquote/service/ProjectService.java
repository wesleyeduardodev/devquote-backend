package br.com.devquote.service;
import br.com.devquote.dto.request.ProjectRequest;
import br.com.devquote.dto.response.ProjectResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ProjectService {
    List<ProjectResponse> findAll();

    ProjectResponse findById(Long id);

    ProjectResponse create(ProjectRequest dto);

    ProjectResponse update(Long id, ProjectRequest dto);

    void delete(Long id);

    void deleteBulk(List<Long> ids);

    Page<ProjectResponse> findAllPaginated(Long id,
                                           String name,
                                           String repositoryUrl,
                                           String createdAt,
                                           String updatedAt,
                                           Pageable pageable);
}

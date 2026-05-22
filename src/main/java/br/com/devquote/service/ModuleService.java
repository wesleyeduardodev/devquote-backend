package br.com.devquote.service;
import br.com.devquote.dto.request.ModuleRequest;
import br.com.devquote.dto.response.ModuleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ModuleService {
    List<ModuleResponse> findAll();

    ModuleResponse findById(Long id);

    ModuleResponse create(ModuleRequest dto);

    ModuleResponse update(Long id, ModuleRequest dto);

    void delete(Long id);

    void deleteBulk(List<Long> ids);

    Page<ModuleResponse> findAllPaginated(Long id,
                                          String name,
                                          String createdAt,
                                          String updatedAt,
                                          Pageable pageable);
}

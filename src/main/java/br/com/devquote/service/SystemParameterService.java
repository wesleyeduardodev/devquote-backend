package br.com.devquote.service;
import br.com.devquote.dto.request.SystemParameterRequest;
import br.com.devquote.dto.response.SystemParameterResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface SystemParameterService {

    List<SystemParameterResponse> findAll();

    SystemParameterResponse findById(Long id);

    SystemParameterResponse findByName(String name);

    SystemParameterResponse create(SystemParameterRequest dto);

    SystemParameterResponse update(Long id, SystemParameterRequest dto);

    void delete(Long id);

    void deleteBulk(List<Long> ids);

    Page<SystemParameterResponse> findAllPaginated(
            Long id,
            String name,
            String description,
            String createdAt,
            String updatedAt,
            Pageable pageable
    );

    String getValue(String name);

    String getString(String name);

    String getString(String name, String defaultValue);

    Boolean getBoolean(String name, Boolean defaultValue);

    Integer getInteger(String name);

    Long getLong(String name, Long defaultValue);

    List<String> getList(String name);
}

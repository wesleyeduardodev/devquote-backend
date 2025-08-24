package br.com.devquote.service;
import br.com.devquote.dto.request.RequesterRequest;
import br.com.devquote.dto.response.RequesterResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface RequesterService {

    List<RequesterResponse> findAll();

    RequesterResponse findById(Long id);

    RequesterResponse create(RequesterRequest dto);

    RequesterResponse update(Long id, RequesterRequest dto);

    void delete(Long id);

    void deleteBulk(List<Long> ids);

    Page<RequesterResponse> findAllPaginated(
            Long id,
            String name,
            String email,
            String phone,
            String createdAt,
            String updatedAt,
            Pageable pageable
    );
}

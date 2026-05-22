package br.com.devquote.service;
import br.com.devquote.dto.request.ServerRequest;
import br.com.devquote.dto.response.ServerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ServerService {
    List<ServerResponse> findAll();

    ServerResponse findById(Long id);

    ServerResponse create(ServerRequest dto);

    ServerResponse update(Long id, ServerRequest dto);

    void delete(Long id);

    void deleteBulk(List<Long> ids);

    Page<ServerResponse> findAllPaginated(Long id,
                                          String name,
                                          String link,
                                          String createdAt,
                                          String updatedAt,
                                          Pageable pageable);
}

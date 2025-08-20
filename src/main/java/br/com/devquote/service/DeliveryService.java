package br.com.devquote.service;
import br.com.devquote.dto.request.DeliveryRequest;
import br.com.devquote.dto.response.DeliveryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface DeliveryService {
    List<DeliveryResponse> findAll();

    DeliveryResponse findById(Long id);

    DeliveryResponse create(DeliveryRequest dto);

    DeliveryResponse update(Long id, DeliveryRequest dto);

    void delete(Long id);

    Page<DeliveryResponse> findAllPaginated(Long id,
                                            String taskName,
                                            String taskCode,
                                            String projectName,
                                            String branch,
                                            String pullRequest,
                                            String status,
                                            String startedAt,
                                            String finishedAt,
                                            String createdAt,
                                            String updatedAt,
                                            Pageable pageable);
}
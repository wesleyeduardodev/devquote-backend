package br.com.devquote.service.impl;
import br.com.devquote.adapter.RequesterAdapter;
import br.com.devquote.dto.request.RequesterRequest;
import br.com.devquote.dto.response.RequesterResponse;
import br.com.devquote.entity.Requester;
import br.com.devquote.repository.RequesterRepository;
import br.com.devquote.service.RequesterService;
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
public class RequesterServiceImpl implements RequesterService {

    private final RequesterRepository requesterRepository;

    @Override
    public List<RequesterResponse> findAll() {
        return requesterRepository.findAllOrderedById().stream()
                .map(RequesterAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RequesterResponse findById(Long id) {
        Requester entity = requesterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Requester not found"));
        return RequesterAdapter.toResponseDTO(entity);
    }

    @Override
    public RequesterResponse create(RequesterRequest dto) {
        Requester entity = RequesterAdapter.toEntity(dto);
        entity = requesterRepository.save(entity);
        return RequesterAdapter.toResponseDTO(entity);
    }

    @Override
    public RequesterResponse update(Long id, RequesterRequest dto) {
        Requester entity = requesterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Requester not found"));
        RequesterAdapter.updateEntityFromDto(dto, entity);
        entity = requesterRepository.save(entity);
        return RequesterAdapter.toResponseDTO(entity);
    }

    @Override
    public void delete(Long id) {
        requesterRepository.deleteById(id);
    }

    @Override
    public Page<RequesterResponse> findAllPaginated(Long id,
                                                    String name,
                                                    String email,
                                                    String phone,
                                                    String createdAt,
                                                    String updatedAt,
                                                    Pageable pageable) {
        Page<Requester> page = requesterRepository.findByOptionalFieldsPaginated(
                id, name, email, phone, createdAt, updatedAt, pageable
        );
        return page.map(RequesterAdapter::toResponseDTO);
    }
}

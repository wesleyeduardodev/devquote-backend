package br.com.devquote.service.impl;
import br.com.devquote.adapter.RequesterAdapter;
import br.com.devquote.dto.request.RequesterRequestDTO;
import br.com.devquote.dto.response.RequesterResponseDTO;
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
    public List<RequesterResponseDTO> findAll() {
        return requesterRepository.findAllOrderedById().stream()
                .map(RequesterAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RequesterResponseDTO findById(Long id) {
        Requester entity = requesterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Requester not found"));
        return RequesterAdapter.toResponseDTO(entity);
    }

    @Override
    public RequesterResponseDTO create(RequesterRequestDTO dto) {
        Requester entity = RequesterAdapter.toEntity(dto);
        entity = requesterRepository.save(entity);
        return RequesterAdapter.toResponseDTO(entity);
    }

    @Override
    public RequesterResponseDTO update(Long id, RequesterRequestDTO dto) {
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
    public Page<RequesterResponseDTO> findAllPaginated(Pageable pageable, String search) {
        Page<Requester> page;
        if (search != null && !search.trim().isEmpty()) {
            page = requesterRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    search, search, pageable);
        } else {
            page = requesterRepository.findAll(pageable);
        }
        return page.map(RequesterAdapter::toResponseDTO);
    }
}

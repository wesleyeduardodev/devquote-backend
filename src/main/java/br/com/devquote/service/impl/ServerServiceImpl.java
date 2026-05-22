package br.com.devquote.service.impl;
import br.com.devquote.adapter.ServerAdapter;
import br.com.devquote.dto.request.ServerRequest;
import br.com.devquote.dto.response.ServerResponse;
import br.com.devquote.entity.Server;
import br.com.devquote.error.ResourceNotFoundException;
import br.com.devquote.repository.ServerRepository;
import br.com.devquote.service.ServerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ServerServiceImpl implements ServerService {

    private final ServerRepository serverRepository;

    @Override
    public List<ServerResponse> findAll() {
        return serverRepository.findAllOrderedByName().stream()
                .map(ServerAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ServerResponse findById(Long id) {
        Server entity = serverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor não encontrado: " + id));
        return ServerAdapter.toResponseDTO(entity);
    }

    @Override
    public ServerResponse create(ServerRequest dto) {
        Server entity = ServerAdapter.toEntity(dto);
        entity = serverRepository.save(entity);
        return ServerAdapter.toResponseDTO(entity);
    }

    @Override
    public ServerResponse update(Long id, ServerRequest dto) {
        Server entity = serverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor não encontrado: " + id));
        ServerAdapter.updateEntityFromDto(dto, entity);
        entity = serverRepository.save(entity);
        return ServerAdapter.toResponseDTO(entity);
    }

    @Override
    public void delete(Long id) {
        serverRepository.deleteById(id);
    }

    @Override
    public void deleteBulk(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        serverRepository.deleteAllById(ids);
    }

    @Override
    public Page<ServerResponse> findAllPaginated(Long id,
                                                 String name,
                                                 String link,
                                                 String createdAt,
                                                 String updatedAt,
                                                 Pageable pageable) {
        Page<Server> page = serverRepository.findByOptionalFieldsPaginated(
                id, name, link, createdAt, updatedAt, pageable
        );
        return page.map(ServerAdapter::toResponseDTO);
    }
}

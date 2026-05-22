package br.com.devquote.service.impl;
import br.com.devquote.adapter.ModuleAdapter;
import br.com.devquote.dto.request.ModuleRequest;
import br.com.devquote.dto.response.ModuleResponse;
import br.com.devquote.entity.SystemModule;
import br.com.devquote.error.ResourceNotFoundException;
import br.com.devquote.repository.ModuleRepository;
import br.com.devquote.service.ModuleService;
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
public class ModuleServiceImpl implements ModuleService {

    private final ModuleRepository moduleRepository;

    @Override
    public List<ModuleResponse> findAll() {
        return moduleRepository.findAllOrderedByName().stream()
                .map(ModuleAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ModuleResponse findById(Long id) {
        SystemModule entity = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo não encontrado: " + id));
        return ModuleAdapter.toResponseDTO(entity);
    }

    @Override
    public ModuleResponse create(ModuleRequest dto) {
        SystemModule entity = ModuleAdapter.toEntity(dto);
        entity = moduleRepository.save(entity);
        return ModuleAdapter.toResponseDTO(entity);
    }

    @Override
    public ModuleResponse update(Long id, ModuleRequest dto) {
        SystemModule entity = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo não encontrado: " + id));
        ModuleAdapter.updateEntityFromDto(dto, entity);
        entity = moduleRepository.save(entity);
        return ModuleAdapter.toResponseDTO(entity);
    }

    @Override
    public void delete(Long id) {
        moduleRepository.deleteById(id);
    }

    @Override
    public void deleteBulk(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        moduleRepository.deleteAllById(ids);
    }

    @Override
    public Page<ModuleResponse> findAllPaginated(Long id,
                                                 String name,
                                                 String createdAt,
                                                 String updatedAt,
                                                 Pageable pageable) {
        Page<SystemModule> page = moduleRepository.findByOptionalFieldsPaginated(
                id, name, createdAt, updatedAt, pageable
        );
        return page.map(ModuleAdapter::toResponseDTO);
    }
}

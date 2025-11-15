package br.com.devquote.service.impl;

import br.com.devquote.adapter.SystemParameterAdapter;
import br.com.devquote.dto.request.SystemParameterRequest;
import br.com.devquote.dto.response.SystemParameterResponse;
import br.com.devquote.entity.SystemParameter;
import br.com.devquote.error.BusinessException;
import br.com.devquote.error.ResourceNotFoundException;
import br.com.devquote.repository.SystemParameterRepository;
import br.com.devquote.service.SystemParameterService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SystemParameterServiceImpl implements SystemParameterService {

    private final SystemParameterRepository systemParameterRepository;

    @Override
    public List<SystemParameterResponse> findAll() {
        return systemParameterRepository.findAllOrderedById().stream()
                .map(SystemParameterAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SystemParameterResponse findById(Long id) {
        SystemParameter entity = systemParameterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parâmetro", id));
        return SystemParameterAdapter.toResponseDTO(entity);
    }

    @Override
    public SystemParameterResponse findByName(String name) {
        SystemParameter entity = systemParameterRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Parâmetro com nome: " + name));
        return SystemParameterAdapter.toResponseDTO(entity);
    }

    @Override
    public SystemParameterResponse create(SystemParameterRequest dto) {
        if (systemParameterRepository.existsByName(dto.getName())) {
            throw new BusinessException(
                    "Já existe um parâmetro com o nome '" + dto.getName() + "'. Por favor, use um nome diferente.",
                    "DUPLICATE_PARAMETER_NAME"
            );
        }

        SystemParameter entity = SystemParameterAdapter.toEntity(dto);

        try {
            entity = systemParameterRepository.save(entity);
        } catch (DataIntegrityViolationException e) {
            String errorMessage = e.getMessage();
            if (errorMessage != null && (errorMessage.contains("uk_system_parameter_name") || errorMessage.contains("name"))) {
                throw new BusinessException(
                        "Já existe um parâmetro com o nome '" + dto.getName() + "'. Por favor, use um nome diferente.",
                        "DUPLICATE_PARAMETER_NAME"
                );
            }
            throw new BusinessException("Erro ao salvar parâmetro: " + e.getMessage(), "PARAMETER_SAVE_ERROR");
        }

        return SystemParameterAdapter.toResponseDTO(entity);
    }

    @Override
    public SystemParameterResponse update(Long id, SystemParameterRequest dto) {
        SystemParameter entity = systemParameterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parâmetro", id));

        if (systemParameterRepository.existsByNameAndIdNot(dto.getName(), id)) {
            throw new BusinessException(
                    "Já existe um parâmetro com o nome '" + dto.getName() + "'. Por favor, use um nome diferente.",
                    "DUPLICATE_PARAMETER_NAME"
            );
        }

        SystemParameterAdapter.updateEntityFromDto(dto, entity);

        try {
            entity = systemParameterRepository.save(entity);
        } catch (DataIntegrityViolationException e) {
            String errorMessage = e.getMessage();
            if (errorMessage != null && (errorMessage.contains("uk_system_parameter_name") || errorMessage.contains("name"))) {
                throw new BusinessException(
                        "Já existe um parâmetro com o nome '" + dto.getName() + "'. Por favor, use um nome diferente.",
                        "DUPLICATE_PARAMETER_NAME"
                );
            }
            throw new BusinessException("Erro ao atualizar parâmetro: " + e.getMessage(), "PARAMETER_UPDATE_ERROR");
        }

        return SystemParameterAdapter.toResponseDTO(entity);
    }

    @Override
    public void delete(Long id) {
        systemParameterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parâmetro", id));
        systemParameterRepository.deleteById(id);
    }

    @Override
    public void deleteBulk(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        systemParameterRepository.deleteAllById(ids);
    }

    @Override
    public Page<SystemParameterResponse> findAllPaginated(
            Long id,
            String name,
            String description,
            String createdAt,
            String updatedAt,
            Pageable pageable) {

        Page<SystemParameter> page = systemParameterRepository.findByOptionalFieldsPaginated(
                id, name, description, createdAt, updatedAt, pageable
        );
        return page.map(SystemParameterAdapter::toResponseDTO);
    }
}

package br.com.devquote.service.impl;

import br.com.devquote.adapter.SystemParameterAdapter;
import br.com.devquote.dto.request.SystemParameterRequest;
import br.com.devquote.dto.response.SystemParameterResponse;
import br.com.devquote.entity.SystemParameter;
import br.com.devquote.error.BusinessException;
import br.com.devquote.error.ResourceNotFoundException;
import br.com.devquote.repository.SystemParameterRepository;
import br.com.devquote.service.SystemParameterService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SystemParameterServiceImpl implements SystemParameterService {

    private final SystemParameterRepository systemParameterRepository;
    private final ObjectMapper objectMapper;
    private final br.com.devquote.utils.EncryptionUtil encryptionUtil;

    @Override
    public List<SystemParameterResponse> findAll() {
        return systemParameterRepository.findAllOrderedById().stream()
                .map(entity -> {
                    SystemParameterResponse response = SystemParameterAdapter.toResponseDTO(entity);
                    if (Boolean.TRUE.equals(entity.getIsEncrypted()) && response.getValue() != null) {
                        response.setValue(encryptionUtil.decrypt(response.getValue()));
                    }
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public SystemParameterResponse findById(Long id) {
        SystemParameter entity = systemParameterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parâmetro", id));
        SystemParameterResponse response = SystemParameterAdapter.toResponseDTO(entity);
        if (Boolean.TRUE.equals(entity.getIsEncrypted()) && response.getValue() != null) {
            response.setValue(encryptionUtil.decrypt(response.getValue()));
        }
        return response;
    }

    @Override
    public SystemParameterResponse findByName(String name) {
        SystemParameter entity = systemParameterRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Parâmetro com nome: " + name));
        SystemParameterResponse response = SystemParameterAdapter.toResponseDTO(entity);
        if (Boolean.TRUE.equals(entity.getIsEncrypted()) && response.getValue() != null) {
            response.setValue(encryptionUtil.decrypt(response.getValue()));
        }
        return response;
    }

    @Override
    @CacheEvict(value = "systemParameters", key = "#dto.name")
    public SystemParameterResponse create(SystemParameterRequest dto) {
        if (systemParameterRepository.existsByName(dto.getName())) {
            throw new BusinessException(
                    "Já existe um parâmetro com o nome '" + dto.getName() + "'. Por favor, use um nome diferente.",
                    "DUPLICATE_PARAMETER_NAME"
            );
        }

        SystemParameter entity = SystemParameterAdapter.toEntity(dto);

        if (Boolean.TRUE.equals(entity.getIsEncrypted()) && entity.getValue() != null) {
            entity.setValue(encryptionUtil.encrypt(entity.getValue()));
        }

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

        SystemParameterResponse response = SystemParameterAdapter.toResponseDTO(entity);
        if (Boolean.TRUE.equals(entity.getIsEncrypted()) && response.getValue() != null) {
            response.setValue(encryptionUtil.decrypt(response.getValue()));
        }
        return response;
    }

    @Override
    @CacheEvict(value = "systemParameters", key = "#dto.name")
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

        if (Boolean.TRUE.equals(entity.getIsEncrypted()) && entity.getValue() != null) {
            entity.setValue(encryptionUtil.encrypt(entity.getValue()));
        }

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

        SystemParameterResponse response = SystemParameterAdapter.toResponseDTO(entity);
        if (Boolean.TRUE.equals(entity.getIsEncrypted()) && response.getValue() != null) {
            response.setValue(encryptionUtil.decrypt(response.getValue()));
        }
        return response;
    }

    @Override
    @CacheEvict(value = "systemParameters", allEntries = true)
    public void delete(Long id) {
        SystemParameter entity = systemParameterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parâmetro", id));
        systemParameterRepository.deleteById(id);
    }

    @Override
    @CacheEvict(value = "systemParameters", allEntries = true)
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
        return page.map(entity -> {
            SystemParameterResponse response = SystemParameterAdapter.toResponseDTO(entity);
            if (Boolean.TRUE.equals(entity.getIsEncrypted()) && response.getValue() != null) {
                response.setValue(encryptionUtil.decrypt(response.getValue()));
            }
            return response;
        });
    }

    @Override
    @Cacheable(value = "systemParameters", key = "#name")
    public String getValue(String name) {
        SystemParameter parameter = systemParameterRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Parâmetro com nome: " + name));
        String value = parameter.getValue();
        if (Boolean.TRUE.equals(parameter.getIsEncrypted()) && value != null) {
            value = encryptionUtil.decrypt(value);
        }
        return value;
    }

    @Override
    public String getString(String name) {
        return getValue(name);
    }

    @Override
    public String getString(String name, String defaultValue) {
        try {
            return getValue(name);
        } catch (ResourceNotFoundException e) {
            log.warn("Parâmetro '{}' não encontrado. Usando valor padrão: '{}'", name, defaultValue);
            return defaultValue;
        }
    }

    @Override
    public Boolean getBoolean(String name) {
        String value = getValue(name);
        return Boolean.parseBoolean(value);
    }

    @Override
    public Boolean getBoolean(String name, Boolean defaultValue) {
        try {
            String value = getValue(name);
            return Boolean.parseBoolean(value);
        } catch (ResourceNotFoundException e) {
            log.warn("Parâmetro '{}' não encontrado. Usando valor padrão: '{}'", name, defaultValue);
            return defaultValue;
        }
    }

    @Override
    public Integer getInteger(String name) {
        String value = getValue(name);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new BusinessException("Parâmetro '" + name + "' não é um número inteiro válido: " + value, "INVALID_INTEGER_PARAMETER");
        }
    }

    @Override
    public Integer getInteger(String name, Integer defaultValue) {
        try {
            String value = getValue(name);
            return Integer.parseInt(value);
        } catch (ResourceNotFoundException e) {
            log.warn("Parâmetro '{}' não encontrado. Usando valor padrão: '{}'", name, defaultValue);
            return defaultValue;
        } catch (NumberFormatException e) {
            log.error("Parâmetro '{}' não é um número inteiro válido. Usando valor padrão: '{}'", name, defaultValue);
            return defaultValue;
        }
    }

    @Override
    public Long getLong(String name) {
        String value = getValue(name);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new BusinessException("Parâmetro '" + name + "' não é um número long válido: " + value, "INVALID_LONG_PARAMETER");
        }
    }

    @Override
    public Long getLong(String name, Long defaultValue) {
        try {
            String value = getValue(name);
            return Long.parseLong(value);
        } catch (ResourceNotFoundException e) {
            log.warn("Parâmetro '{}' não encontrado. Usando valor padrão: '{}'", name, defaultValue);
            return defaultValue;
        } catch (NumberFormatException e) {
            log.error("Parâmetro '{}' não é um número long válido. Usando valor padrão: '{}'", name, defaultValue);
            return defaultValue;
        }
    }

    @Override
    public List<String> getList(String name) {
        String value = getValue(name);
        if (value == null || value.trim().isEmpty()) {
            return Collections.emptyList();
        }

        if (value.trim().startsWith("[")) {
            try {
                return objectMapper.readValue(value, new TypeReference<List<String>>() {});
            } catch (Exception e) {
                log.error("Erro ao fazer parse do JSON para lista no parâmetro '{}': {}", name, e.getMessage());
                throw new BusinessException("Parâmetro '" + name + "' não é um JSON válido para lista: " + value, "INVALID_JSON_LIST_PARAMETER");
            }
        }

        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getList(String name, List<String> defaultValue) {
        try {
            return getList(name);
        } catch (ResourceNotFoundException e) {
            log.warn("Parâmetro '{}' não encontrado. Usando valor padrão: '{}'", name, defaultValue);
            return defaultValue;
        } catch (BusinessException e) {
            log.error("Erro ao processar parâmetro '{}'. Usando valor padrão: '{}'", name, defaultValue);
            return defaultValue;
        }
    }
}

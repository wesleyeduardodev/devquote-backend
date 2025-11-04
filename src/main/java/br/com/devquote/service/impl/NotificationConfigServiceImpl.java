package br.com.devquote.service.impl;
import br.com.devquote.adapter.NotificationConfigAdapter;
import br.com.devquote.dto.request.NotificationConfigRequest;
import br.com.devquote.dto.response.NotificationConfigResponse;
import br.com.devquote.entity.NotificationConfig;
import br.com.devquote.enums.NotificationConfigType;
import br.com.devquote.enums.NotificationType;
import br.com.devquote.repository.NotificationConfigRepository;
import br.com.devquote.service.NotificationConfigService;
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
public class NotificationConfigServiceImpl implements NotificationConfigService {

    private final NotificationConfigRepository notificationConfigRepository;

    @Override
    public List<NotificationConfigResponse> findAll() {
        return notificationConfigRepository.findAllOrderedById().stream()
                .map(NotificationConfigAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public NotificationConfigResponse findById(Long id) {
        NotificationConfig entity = notificationConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification config not found"));
        return NotificationConfigAdapter.toResponseDTO(entity);
    }

    @Override
    public NotificationConfigResponse create(NotificationConfigRequest dto) {
        NotificationConfig entity = NotificationConfigAdapter.toEntity(dto);
        entity = notificationConfigRepository.save(entity);
        return NotificationConfigAdapter.toResponseDTO(entity);
    }

    @Override
    public NotificationConfigResponse update(Long id, NotificationConfigRequest dto) {
        NotificationConfig entity = notificationConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification config not found"));
        NotificationConfigAdapter.updateEntityFromDto(dto, entity);
        entity = notificationConfigRepository.save(entity);
        return NotificationConfigAdapter.toResponseDTO(entity);
    }

    @Override
    public void delete(Long id) {
        notificationConfigRepository.deleteById(id);
    }

    @Override
    public void deleteBulk(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        notificationConfigRepository.deleteAllById(ids);
    }

    @Override
    public Page<NotificationConfigResponse> findAllPaginated(
            Long id,
            NotificationConfigType configType,
            NotificationType notificationType,
            String primaryEmail,
            String createdAt,
            String updatedAt,
            Pageable pageable) {

        Page<NotificationConfig> page = notificationConfigRepository.findByOptionalFieldsPaginated(
                id, configType, notificationType, primaryEmail, createdAt, updatedAt, pageable
        );
        return page.map(NotificationConfigAdapter::toResponseDTO);
    }

    @Override
    public List<NotificationConfigResponse> findByConfigTypeAndNotificationType(
            NotificationConfigType configType,
            NotificationType notificationType) {

        return notificationConfigRepository.findByConfigTypeAndNotificationType(configType, notificationType)
                .stream()
                .map(NotificationConfigAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public NotificationConfig findEntityByConfigTypeAndNotificationType(NotificationConfigType configType, NotificationType notificationType) {
        return notificationConfigRepository.findByConfigTypeAndNotificationType(configType, notificationType)
                .stream()
                .findFirst()
                .orElse(null);
    }
}
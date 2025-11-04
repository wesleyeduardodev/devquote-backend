package br.com.devquote.service;
import br.com.devquote.dto.request.NotificationConfigRequest;
import br.com.devquote.dto.response.NotificationConfigResponse;
import br.com.devquote.entity.NotificationConfig;
import br.com.devquote.enums.NotificationConfigType;
import br.com.devquote.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface NotificationConfigService {

    List<NotificationConfigResponse> findAll();

    NotificationConfigResponse findById(Long id);

    NotificationConfigResponse create(NotificationConfigRequest dto);

    NotificationConfigResponse update(Long id, NotificationConfigRequest dto);

    void delete(Long id);

    void deleteBulk(List<Long> ids);

    Page<NotificationConfigResponse> findAllPaginated(
            Long id,
            NotificationConfigType configType,
            NotificationType notificationType,
            String primaryEmail,
            String createdAt,
            String updatedAt,
            Pageable pageable
    );

    List<NotificationConfigResponse> findByConfigTypeAndNotificationType(
            NotificationConfigType configType,
            NotificationType notificationType
    );

    NotificationConfig findEntityByConfigTypeAndNotificationType(
            NotificationConfigType configType,
            NotificationType notificationType
    );
}
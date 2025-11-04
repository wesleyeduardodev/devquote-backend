package br.com.devquote.controller;
import br.com.devquote.adapter.PageAdapter;
import br.com.devquote.dto.request.NotificationConfigRequest;
import br.com.devquote.dto.response.NotificationConfigResponse;
import br.com.devquote.dto.response.PagedResponse;
import br.com.devquote.enums.NotificationConfigType;
import br.com.devquote.enums.NotificationType;
import br.com.devquote.service.NotificationConfigService;
import br.com.devquote.utils.SortUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/notification-configs")
@Validated
@RequiredArgsConstructor
public class NotificationConfigController {

    private final NotificationConfigService notificationConfigService;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "configType", "notificationType", "primaryEmail", "createdAt", "updatedAt"
    );

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<PagedResponse<NotificationConfigResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) NotificationConfigType configType,
            @RequestParam(required = false) NotificationType notificationType,
            @RequestParam(required = false) String primaryEmail,
            @RequestParam(required = false) String createdAt,
            @RequestParam(required = false) String updatedAt,
            @RequestParam(required = false) MultiValueMap<String, String> params
    ) {

        List<String> sortParams = params.get("sort");

        Pageable pageable = PageRequest.of(
                page,
                size,
                SortUtils.buildAndSanitize(sortParams, ALLOWED_SORT_FIELDS, "id")
        );

        Page<NotificationConfigResponse> pageResult = notificationConfigService.findAllPaginated(
                id, configType, notificationType, primaryEmail, createdAt, updatedAt, pageable
        );

        PagedResponse<NotificationConfigResponse> response = PageAdapter.toPagedResponseDTO(pageResult);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<NotificationConfigResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationConfigService.findById(id));
    }

    @GetMapping("/by-type")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<List<NotificationConfigResponse>> findByConfigTypeAndNotificationType(
            @RequestParam NotificationConfigType configType,
            @RequestParam NotificationType notificationType
    ) {
        List<NotificationConfigResponse> configs = notificationConfigService
                .findByConfigTypeAndNotificationType(configType, notificationType);
        return ResponseEntity.ok(configs);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationConfigResponse> create(@RequestBody @Valid NotificationConfigRequest dto) {
        return new ResponseEntity<>(notificationConfigService.create(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationConfigResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid NotificationConfigRequest dto) {
        return ResponseEntity.ok(notificationConfigService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        notificationConfigService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBulk(@RequestBody List<Long> ids) {
        notificationConfigService.deleteBulk(ids);
        return ResponseEntity.noContent().build();
    }
}
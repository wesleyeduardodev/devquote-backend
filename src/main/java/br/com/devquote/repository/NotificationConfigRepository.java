package br.com.devquote.repository;
import br.com.devquote.entity.NotificationConfig;
import br.com.devquote.enums.NotificationConfigType;
import br.com.devquote.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationConfigRepository extends JpaRepository<NotificationConfig, Long> {

    @Query("SELECT nc FROM NotificationConfig nc ORDER BY nc.id ASC")
    List<NotificationConfig> findAllOrderedById();

    @Query("SELECT nc FROM NotificationConfig nc WHERE " +
            "(:id IS NULL OR nc.id = :id) AND " +
            "(:configType IS NULL OR nc.configType = :configType) AND " +
            "(:notificationType IS NULL OR nc.notificationType = :notificationType) AND " +
            "(:primaryEmail IS NULL OR :primaryEmail = '' OR LOWER(nc.primaryEmail) LIKE LOWER(CONCAT('%', :primaryEmail, '%'))) AND " +
            "(:createdAt IS NULL OR :createdAt = '' OR CAST(nc.createdAt AS STRING) LIKE CONCAT('%', :createdAt, '%')) AND " +
            "(:updatedAt IS NULL OR :updatedAt = '' OR CAST(nc.updatedAt AS STRING) LIKE CONCAT('%', :updatedAt, '%'))")
    Page<NotificationConfig> findByOptionalFieldsPaginated(
            @Param("id") Long id,
            @Param("configType") NotificationConfigType configType,
            @Param("notificationType") NotificationType notificationType,
            @Param("primaryEmail") String primaryEmail,
            @Param("createdAt") String createdAt,
            @Param("updatedAt") String updatedAt,
            Pageable pageable
    );

    List<NotificationConfig> findByConfigTypeAndNotificationType(
            NotificationConfigType configType,
            NotificationType notificationType
    );
}
package br.com.devquote.repository;
import br.com.devquote.entity.DeliveryItem;
import br.com.devquote.enums.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryItemRepository extends JpaRepository<DeliveryItem, Long> {

    @EntityGraph(attributePaths = {"delivery", "delivery.task", "project"})
    @Override
    Optional<DeliveryItem> findById(Long id);

    @EntityGraph(attributePaths = {"delivery", "delivery.task", "project"})
    @Query("SELECT di FROM DeliveryItem di WHERE di.delivery.id = :deliveryId ORDER BY di.id")
    List<DeliveryItem> findByDeliveryId(@Param("deliveryId") Long deliveryId);

    @EntityGraph(attributePaths = {"delivery", "delivery.task", "project"})
    @Query("SELECT di FROM DeliveryItem di WHERE di.delivery.task.id = :taskId ORDER BY di.id")
    List<DeliveryItem> findByTaskId(@Param("taskId") Long taskId);

    @EntityGraph(attributePaths = {"delivery", "delivery.task", "project"})
    @Query("SELECT di FROM DeliveryItem di WHERE di.project.id = :projectId ORDER BY di.id")
    List<DeliveryItem> findByProjectId(@Param("projectId") Long projectId);

    @EntityGraph(attributePaths = {"delivery", "delivery.task", "project"})
    @Query("SELECT di FROM DeliveryItem di WHERE di.status = :status ORDER BY di.id")
    List<DeliveryItem> findByStatus(@Param("status") DeliveryStatus status);

    @Query("SELECT COUNT(di) FROM DeliveryItem di WHERE di.delivery.id = :deliveryId")
    long countByDeliveryId(@Param("deliveryId") Long deliveryId);

    @Query("SELECT COUNT(di) FROM DeliveryItem di WHERE di.delivery.id = :deliveryId AND di.status = :status")
    long countByDeliveryIdAndStatus(@Param("deliveryId") Long deliveryId, @Param("status") DeliveryStatus status);

    @Query("""
        SELECT di.id
          FROM DeliveryItem di
          JOIN di.delivery d
          JOIN d.task t
          JOIN di.project p
         WHERE (:id IS NULL OR di.id = :id)
           AND (:deliveryId IS NULL OR d.id = :deliveryId)
           AND (:taskId IS NULL OR t.id = :taskId)
           AND (:taskName IS NULL OR :taskName = '' OR LOWER(t.title) LIKE LOWER(CONCAT('%', :taskName, '%')))
           AND (:taskCode IS NULL OR :taskCode = '' OR LOWER(t.code) LIKE LOWER(CONCAT('%', :taskCode, '%')))        
           AND (:projectName IS NULL OR :projectName = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :projectName, '%')))
           AND (:branch IS NULL OR :branch = '' OR LOWER(di.branch) LIKE LOWER(CONCAT('%', :branch, '%')))
           AND (:pullRequest IS NULL OR :pullRequest = '' OR LOWER(di.pullRequest) LIKE LOWER(CONCAT('%', :pullRequest, '%')))          
           AND (:status IS NULL OR di.status = :status)
           AND (:startedAt IS NULL OR :startedAt = '' OR CAST(di.startedAt AS string) LIKE CONCAT('%', :startedAt, '%'))
           AND (:finishedAt IS NULL OR :finishedAt = '' OR CAST(di.finishedAt AS string) LIKE CONCAT('%', :finishedAt, '%'))
           AND (:createdAt IS NULL OR :createdAt = '' OR CAST(di.createdAt AS string) LIKE CONCAT('%', :createdAt, '%'))
           AND (:updatedAt IS NULL OR :updatedAt = '' OR CAST(di.updatedAt AS string) LIKE CONCAT('%', :updatedAt, '%'))
        """)
    Page<Long> findIdsByOptionalFieldsPaginated(
            @Param("id") Long id,
            @Param("deliveryId") Long deliveryId,
            @Param("taskId") Long taskId,
            @Param("taskName") String taskName,
            @Param("taskCode") String taskCode,
            @Param("projectName") String projectName,
            @Param("branch") String branch,
            @Param("pullRequest") String pullRequest,
            @Param("status") DeliveryStatus status,
            @Param("startedAt") String startedAt,
            @Param("finishedAt") String finishedAt,
            @Param("createdAt") String createdAt,
            @Param("updatedAt") String updatedAt,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"delivery", "delivery.task", "project"})
    @Query("SELECT di FROM DeliveryItem di WHERE di.id IN :ids")
    List<DeliveryItem> findByIdsWithEntityGraph(@Param("ids") List<Long> ids);

    @Query(value = """
        SELECT 
            di.id,
            di.delivery_id,
            di.project_id,
            di.status,
            di.branch,
            di.source_branch,
            di.pull_request,
            di.notes,
            di.started_at,
            di.finished_at,
            di.created_at,
            di.updated_at,
            di.created_by,
            di.updated_by,
            p.name as project_name,
            t.id as task_id,
            t.title as task_name,
            t.code as task_code
        FROM delivery_item di
        INNER JOIN project p ON di.project_id = p.id
        INNER JOIN delivery d ON di.delivery_id = d.id
        INNER JOIN task t ON d.task_id = t.id
        WHERE t.id = :taskId
        ORDER BY di.id
        """, nativeQuery = true)
    List<Object[]> findItemsByTaskIdOptimized(@Param("taskId") Long taskId);
}
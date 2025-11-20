package br.com.devquote.repository;
import br.com.devquote.entity.Task;
import br.com.devquote.enums.FlowType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t ORDER BY t.id ASC")
    List<Task> findAllOrderedById();
    
    boolean existsByCode(String code);
    
    boolean existsByCodeAndIdNot(String code, Long id);

    @Query("""
            SELECT t FROM Task t
            WHERE (:id IS NULL OR t.id = :id)
              AND (:requesterId IS NULL OR t.requester.id = :requesterId)
              AND (:requesterName IS NULL OR :requesterName = '' OR LOWER(t.requester.name) LIKE LOWER(CONCAT('%', :requesterName, '%')))
              AND (:title IS NULL OR :title = '' OR LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%')))
              AND (:description IS NULL OR :description = '' OR LOWER(t.description) LIKE LOWER(CONCAT('%', :description, '%')))
              AND (:code IS NULL OR :code = '' OR LOWER(t.code) LIKE LOWER(CONCAT('%', :code, '%')))
              AND (:link IS NULL OR :link = '' OR LOWER(t.link) LIKE LOWER(CONCAT('%', :link, '%')))
              AND (:createdAt IS NULL OR :createdAt = '' OR CAST(t.createdAt AS string) LIKE CONCAT('%', :createdAt, '%'))
              AND (:updatedAt IS NULL OR :updatedAt = '' OR CAST(t.updatedAt AS string) LIKE CONCAT('%', :updatedAt, '%'))
              AND (:flowType IS NULL OR t.flowType = :flowType)
              AND (:taskType IS NULL OR :taskType = '' OR CAST(t.taskType AS string) = :taskType)
              AND (:environment IS NULL OR :environment = '' OR CAST(t.environment AS string) = :environment)
              AND (:startDate IS NULL OR :startDate = '' OR CAST(t.createdAt AS date) >= CAST(:startDate AS date))
              AND (:endDate IS NULL OR :endDate = '' OR CAST(t.createdAt AS date) <= CAST(:endDate AS date))
            """)
    Page<Task> findByOptionalFieldsPaginated(
            @Param("id") Long id,
            @Param("requesterId") Long requesterId,
            @Param("requesterName") String requesterName,
            @Param("title") String title,
            @Param("description") String description,
            @Param("code") String code,
            @Param("link") String link,
            @Param("createdAt") String createdAt,
            @Param("updatedAt") String updatedAt,
            @Param("flowType") FlowType flowType,
            @Param("taskType") String taskType,
            @Param("environment") String environment,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            Pageable pageable
    );

    @Query("""
            SELECT t FROM Task t
            WHERE NOT EXISTS (
                SELECT 1 FROM BillingPeriodTask bpt WHERE bpt.task.id = t.id
            )
              AND (:id IS NULL OR t.id = :id)
              AND (:requesterId IS NULL OR t.requester.id = :requesterId)
              AND (:requesterName IS NULL OR :requesterName = '' OR LOWER(t.requester.name) LIKE LOWER(CONCAT('%', :requesterName, '%')))
              AND (:title IS NULL OR :title = '' OR LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%')))
              AND (:description IS NULL OR :description = '' OR LOWER(t.description) LIKE LOWER(CONCAT('%', :description, '%')))
              AND (:code IS NULL OR :code = '' OR LOWER(t.code) LIKE LOWER(CONCAT('%', :code, '%')))
              AND (:link IS NULL OR :link = '' OR LOWER(t.link) LIKE LOWER(CONCAT('%', :link, '%')))
              AND (:createdAt IS NULL OR :createdAt = '' OR CAST(t.createdAt AS string) LIKE CONCAT('%', :createdAt, '%'))
              AND (:updatedAt IS NULL OR :updatedAt = '' OR CAST(t.updatedAt AS string) LIKE CONCAT('%', :updatedAt, '%'))
              AND (:flowType IS NULL OR t.flowType = :flowType)
            """)
    Page<Task> findUnlinkedBillingByOptionalFieldsPaginated(
            @Param("id") Long id,
            @Param("requesterId") Long requesterId,
            @Param("requesterName") String requesterName,
            @Param("title") String title,
            @Param("description") String description,
            @Param("code") String code,
            @Param("link") String link,
            @Param("createdAt") String createdAt,
            @Param("updatedAt") String updatedAt,
            @Param("flowType") FlowType flowType,
            Pageable pageable
    );

    @Query("""
            SELECT t FROM Task t
            WHERE NOT EXISTS (
                SELECT 1 FROM Delivery d WHERE d.task.id = t.id
            )
              AND (:id IS NULL OR t.id = :id)
              AND (:requesterId IS NULL OR t.requester.id = :requesterId)
              AND (:requesterName IS NULL OR :requesterName = '' OR LOWER(t.requester.name) LIKE LOWER(CONCAT('%', :requesterName, '%')))
              AND (:title IS NULL OR :title = '' OR LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%')))
              AND (:description IS NULL OR :description = '' OR LOWER(t.description) LIKE LOWER(CONCAT('%', :description, '%')))
              AND (:code IS NULL OR :code = '' OR LOWER(t.code) LIKE LOWER(CONCAT('%', :code, '%')))
              AND (:link IS NULL OR :link = '' OR LOWER(t.link) LIKE LOWER(CONCAT('%', :link, '%')))
              AND (:createdAt IS NULL OR :createdAt = '' OR CAST(t.createdAt AS string) LIKE CONCAT('%', :createdAt, '%'))
              AND (:updatedAt IS NULL OR :updatedAt = '' OR CAST(t.updatedAt AS string) LIKE CONCAT('%', :updatedAt, '%'))
              AND (:flowType IS NULL OR t.flowType = :flowType)
            """)
    Page<Task> findUnlinkedDeliveryByOptionalFieldsPaginated(
            @Param("id") Long id,
            @Param("requesterId") Long requesterId,
            @Param("requesterName") String requesterName,
            @Param("title") String title,
            @Param("description") String description,
            @Param("code") String code,
            @Param("link") String link,
            @Param("createdAt") String createdAt,
            @Param("updatedAt") String updatedAt,
            @Param("flowType") FlowType flowType,
            Pageable pageable
    );
}

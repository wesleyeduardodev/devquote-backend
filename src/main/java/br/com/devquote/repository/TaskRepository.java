package br.com.devquote.repository;
import br.com.devquote.entity.Task;
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

    @Query("""
            SELECT t FROM Task t
            WHERE (:id IS NULL OR t.id = :id)
              AND (:requesterId IS NULL OR t.requester.id = :requesterId)
              AND (:requesterName IS NULL OR :requesterName = '' OR LOWER(t.requester.name) LIKE LOWER(CONCAT('%', :requesterName, '%')))
              AND (:title IS NULL OR :title = '' OR LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%')))
              AND (:description IS NULL OR :description = '' OR LOWER(t.description) LIKE LOWER(CONCAT('%', :description, '%')))
              AND (:status IS NULL OR :status = '' OR LOWER(t.status) LIKE LOWER(CONCAT('%', :status, '%')))
              AND (:code IS NULL OR :code = '' OR LOWER(t.code) LIKE LOWER(CONCAT('%', :code, '%')))
              AND (:link IS NULL OR :link = '' OR LOWER(t.link) LIKE LOWER(CONCAT('%', :link, '%')))
              AND (:createdAt IS NULL OR :createdAt = '' OR CAST(t.createdAt AS string) LIKE CONCAT('%', :createdAt, '%'))
              AND (:updatedAt IS NULL OR :updatedAt = '' OR CAST(t.updatedAt AS string) LIKE CONCAT('%', :updatedAt, '%'))
            """)
    Page<Task> findByOptionalFieldsPaginated(
            @Param("id") Long id,
            @Param("requesterId") Long requesterId,
            @Param("requesterName") String requesterName,
            @Param("title") String title,
            @Param("description") String description,
            @Param("status") String status,
            @Param("code") String code,
            @Param("link") String link,
            @Param("createdAt") String createdAt,
            @Param("updatedAt") String updatedAt,
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
              AND (:status IS NULL OR :status = '' OR LOWER(t.status) LIKE LOWER(CONCAT('%', :status, '%')))
              AND (:code IS NULL OR :code = '' OR LOWER(t.code) LIKE LOWER(CONCAT('%', :code, '%')))
              AND (:link IS NULL OR :link = '' OR LOWER(t.link) LIKE LOWER(CONCAT('%', :link, '%')))
              AND (:createdAt IS NULL OR :createdAt = '' OR CAST(t.createdAt AS string) LIKE CONCAT('%', :createdAt, '%'))
              AND (:updatedAt IS NULL OR :updatedAt = '' OR CAST(t.updatedAt AS string) LIKE CONCAT('%', :updatedAt, '%'))
            """)
    Page<Task> findUnlinkedTasksByOptionalFieldsPaginated(
            @Param("id") Long id,
            @Param("requesterId") Long requesterId,
            @Param("requesterName") String requesterName,
            @Param("title") String title,
            @Param("description") String description,
            @Param("status") String status,
            @Param("code") String code,
            @Param("link") String link,
            @Param("createdAt") String createdAt,
            @Param("updatedAt") String updatedAt,
            Pageable pageable
    );
}

package br.com.devquote.repository;
import br.com.devquote.entity.Delivery;
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
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    @Query("SELECT d FROM Delivery d ORDER BY d.id ASC")
    List<Delivery> findAllOrderedById();

    @EntityGraph(attributePaths = {"quote", "quote.task", "project"})
    @Override
    Optional<Delivery> findById(Long id);

    @EntityGraph(attributePaths = {"quote", "quote.task", "project"})
    @Query("""
        SELECT d
          FROM Delivery d
          JOIN d.quote q
          JOIN q.task t
          JOIN d.project p
         WHERE (:id IS NULL OR d.id = :id)
           AND (:taskName IS NULL OR :taskName = '' OR LOWER(t.title) LIKE LOWER(CONCAT('%', :taskName, '%')))
           AND (:taskCode IS NULL OR :taskCode = '' OR LOWER(t.code) LIKE LOWER(CONCAT('%', :taskCode, '%')))        
           AND (:projectName IS NULL OR :projectName = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :projectName, '%')))
           AND (:branch IS NULL OR :branch = '' OR LOWER(d.branch) LIKE LOWER(CONCAT('%', :branch, '%')))
           AND (:pullRequest IS NULL OR :pullRequest = '' OR LOWER(d.pullRequest) LIKE LOWER(CONCAT('%', :pullRequest, '%')))          
           AND (:status IS NULL OR :status = '' OR LOWER(d.status) LIKE LOWER(CONCAT('%', :status, '%')))
           AND (:startedAt IS NULL OR :startedAt = '' OR CAST(d.startedAt AS string) LIKE CONCAT('%', :startedAt, '%'))
           AND (:finishedAt IS NULL OR :finishedAt = '' OR CAST(d.finishedAt AS string) LIKE CONCAT('%', :finishedAt, '%'))
           AND (:createdAt IS NULL OR :createdAt = '' OR CAST(d.createdAt AS string) LIKE CONCAT('%', :createdAt, '%'))
           AND (:updatedAt IS NULL OR :updatedAt = '' OR CAST(d.updatedAt AS string) LIKE CONCAT('%', :updatedAt, '%'))
        """)
    Page<Delivery> findByOptionalFieldsPaginated(
            @Param("id") Long id,
            @Param("taskName") String taskName,
            @Param("taskCode") String taskCode,
            @Param("projectName") String projectName,
            @Param("branch") String branch,
            @Param("pullRequest") String pullRequest,
            @Param("status") String status,
            @Param("startedAt") String startedAt,
            @Param("finishedAt") String finishedAt,
            @Param("createdAt") String createdAt,
            @Param("updatedAt") String updatedAt,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"quote", "quote.task", "project"})
    @Query("""
        SELECT d
          FROM Delivery d
          JOIN d.quote q
          JOIN q.task t
         WHERE (:taskName IS NULL OR :taskName = '' OR LOWER(t.title) LIKE LOWER(CONCAT('%', :taskName, '%')))
           AND (:taskCode IS NULL OR :taskCode = '' OR LOWER(t.code) LIKE LOWER(CONCAT('%', :taskCode, '%')))
           AND (:status IS NULL OR :status = '' OR LOWER(d.status) LIKE LOWER(CONCAT('%', :status, '%')))
           AND (:createdAt IS NULL OR :createdAt = '' OR CAST(d.createdAt AS string) LIKE CONCAT('%', :createdAt, '%'))
           AND (:updatedAt IS NULL OR :updatedAt = '' OR CAST(d.updatedAt AS string) LIKE CONCAT('%', :updatedAt, '%'))
         ORDER BY q.id, d.id
        """)
    List<Delivery> findByTaskFilters(
            @Param("taskName") String taskName,
            @Param("taskCode") String taskCode,
            @Param("status") String status,
            @Param("createdAt") String createdAt,
            @Param("updatedAt") String updatedAt
    );

    @EntityGraph(attributePaths = {"quote", "quote.task", "project"})
    @Query("SELECT d FROM Delivery d WHERE d.quote.id = :quoteId ORDER BY d.id")
    List<Delivery> findByQuoteId(@Param("quoteId") Long quoteId);
}

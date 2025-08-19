package br.com.devquote.repository;
import br.com.devquote.entity.Quote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {

    boolean existsByTaskId(Long taskId);

    @Query("SELECT q FROM Quote q ORDER BY q.id ASC")
    List<Quote> findAllOrderedById();

    @Query("""
           SELECT q FROM Quote q
             JOIN q.task t
           WHERE (:id IS NULL OR q.id = :id)
             AND (:taskId IS NULL OR t.id = :taskId)
             AND (:taskName IS NULL OR :taskName = '' OR LOWER(t.title) LIKE LOWER(CONCAT('%', :taskName, '%')))
             AND (:taskCode IS NULL OR :taskCode = '' OR LOWER(t.code) LIKE LOWER(CONCAT('%', :taskCode, '%')))
             AND (:status IS NULL OR :status = '' OR LOWER(q.status) LIKE LOWER(CONCAT('%', :status, '%')))            
             AND (:createdAt IS NULL OR :createdAt = '' OR CAST(q.createdAt AS string) LIKE CONCAT('%', :createdAt, '%'))
             AND (:updatedAt IS NULL OR :updatedAt = '' OR CAST(q.updatedAt AS string) LIKE CONCAT('%', :updatedAt, '%'))
           """)
    Page<Quote> findByOptionalFieldsPaginated(
            @Param("id") Long id,
            @Param("taskId") Long taskId,
            @Param("taskName") String taskName,
            @Param("taskCode") String taskCode,
            @Param("status") String status,
            @Param("createdAt") String createdAt,
            @Param("updatedAt") String updatedAt,
            Pageable pageable
    );
}

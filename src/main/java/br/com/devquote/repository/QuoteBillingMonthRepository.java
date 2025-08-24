package br.com.devquote.repository;
import br.com.devquote.entity.QuoteBillingMonth;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuoteBillingMonthRepository extends JpaRepository<QuoteBillingMonth, Long> {

    @Query("SELECT q FROM QuoteBillingMonth q ORDER BY q.year DESC, q.month DESC, q.id DESC")
    List<QuoteBillingMonth> findAllOrderedById();


    @Query("SELECT q FROM QuoteBillingMonth q WHERE q.year = :year AND q.month = :month")
    Optional<QuoteBillingMonth> findByYearAndMonth(@Param("year") Integer year,
                                                   @Param("month") Integer month);

    boolean existsByYearAndMonth(Integer year, Integer month);

    @Query("""
        SELECT q FROM QuoteBillingMonth q
        WHERE (:month IS NULL OR q.month = :month)
          AND (:year IS NULL OR q.year = :year)
          AND (:status IS NULL OR :status = '' OR LOWER(q.status) = LOWER(:status))
        ORDER BY q.year DESC, q.month DESC, q.id DESC
        """)
    Page<QuoteBillingMonth> findByOptionalFiltersPaginated(
        @Param("month") Integer month,
        @Param("year") Integer year,
        @Param("status") String status,
        Pageable pageable
    );

    @Query("SELECT COUNT(q) FROM QuoteBillingMonth q WHERE q.status = :status")
    Long countByStatus(@Param("status") String status);

    @Query("SELECT q.status, COUNT(q) FROM QuoteBillingMonth q GROUP BY q.status")
    List<Object[]> getStatusStatistics();
}

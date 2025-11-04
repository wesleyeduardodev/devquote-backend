package br.com.devquote.repository;
import br.com.devquote.entity.BillingPeriod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillingPeriodRepository extends JpaRepository<BillingPeriod, Long> {

    @Query("SELECT bp FROM BillingPeriod bp ORDER BY bp.id DESC")
    List<BillingPeriod> findAllOrderedById();

    Optional<BillingPeriod> findByYearAndMonth(Integer year, Integer month);

    @Query("SELECT bp FROM BillingPeriod bp WHERE " +
           "(:month IS NULL OR bp.month = :month) AND " +
           "(:year IS NULL OR bp.year = :year) AND " +
           "(:status IS NULL OR bp.status = :status)")
    Page<BillingPeriod> findByOptionalFiltersPaginated(@Param("month") Integer month,
                                                          @Param("year") Integer year,
                                                          @Param("status") String status,
                                                          Pageable pageable);

    @Query("SELECT bp.status as status, COUNT(bp) as count FROM BillingPeriod bp GROUP BY bp.status")
    List<Object[]> getStatusStatistics();

    @Query("""
        SELECT bp FROM BillingPeriod bp
        WHERE (:year IS NULL OR bp.year = :year)
          AND (:month IS NULL OR bp.month = :month)
          AND (:status IS NULL OR bp.status = :status)
        ORDER BY bp.id DESC
        """)
    List<BillingPeriod> findByFilters(
        @Param("year") Integer year,
        @Param("month") Integer month,
        @Param("status") String status
    );
}
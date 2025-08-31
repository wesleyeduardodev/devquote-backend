package br.com.devquote.repository;

import br.com.devquote.entity.BillingPeriodTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillingPeriodTaskRepository extends JpaRepository<BillingPeriodTask, Long> {

    @Query("SELECT bpt FROM BillingPeriodTask bpt ORDER BY bpt.id DESC")
    List<BillingPeriodTask> findAllOrderedById();

    List<BillingPeriodTask> findByBillingPeriodId(Long billingPeriodId);

    @Query("SELECT bpt FROM BillingPeriodTask bpt WHERE bpt.billingPeriod.id = :billingPeriodId")
    Page<BillingPeriodTask> findByBillingPeriodIdPaginated(@Param("billingPeriodId") Long billingPeriodId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM BillingPeriodTask bpt WHERE bpt.billingPeriod.id = :billingPeriodId AND bpt.task.id IN :taskIds")
    void deleteByBillingPeriodIdAndTaskIds(@Param("billingPeriodId") Long billingPeriodId, @Param("taskIds") List<Long> taskIds);

    boolean existsByBillingPeriodIdAndTaskId(Long billingPeriodId, Long taskId);
    
    boolean existsByTaskId(Long taskId);
    
    Optional<BillingPeriodTask> findByTaskId(Long taskId);
}
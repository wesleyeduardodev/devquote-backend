package br.com.devquote.repository;

import br.com.devquote.entity.BillingPeriodTask;
import br.com.devquote.enums.FlowType;
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

    @Query("SELECT bpt FROM BillingPeriodTask bpt " +
           "JOIN FETCH bpt.task t " +
           "JOIN FETCH t.requester " +
           "WHERE bpt.billingPeriod.id = :billingPeriodId " +
           "ORDER BY bpt.id DESC")
    List<BillingPeriodTask> findByBillingPeriodId(@Param("billingPeriodId") Long billingPeriodId);

    @Query("SELECT bpt.id FROM BillingPeriodTask bpt WHERE bpt.billingPeriod.id = :billingPeriodId")
    Page<Long> findIdsByBillingPeriodIdPaginated(@Param("billingPeriodId") Long billingPeriodId, Pageable pageable);

    @Query("""
        SELECT bpt.id FROM BillingPeriodTask bpt
        JOIN bpt.task t
        WHERE bpt.billingPeriod.id = :billingPeriodId
          AND (:flowType IS NULL OR t.flowType = :flowType)
        """)
    Page<Long> findIdsByBillingPeriodIdAndFlowTypePaginated(
        @Param("billingPeriodId") Long billingPeriodId,
        @Param("flowType") FlowType flowType,
        Pageable pageable
    );

    @Query("SELECT bpt FROM BillingPeriodTask bpt " +
           "JOIN FETCH bpt.task t " +
           "JOIN FETCH t.requester " +
           "WHERE bpt.id IN :ids ORDER BY bpt.id DESC")
    List<BillingPeriodTask> findByIdsWithDetails(@Param("ids") List<Long> ids);

    @Modifying
    @Query("DELETE FROM BillingPeriodTask bpt WHERE bpt.billingPeriod.id = :billingPeriodId AND bpt.task.id IN :taskIds")
    void deleteByBillingPeriodIdAndTaskIds(@Param("billingPeriodId") Long billingPeriodId, @Param("taskIds") List<Long> taskIds);
    
    @Modifying
    @Query("DELETE FROM BillingPeriodTask bpt WHERE bpt.billingPeriod.id = :billingPeriodId")
    void deleteByBillingPeriodId(@Param("billingPeriodId") Long billingPeriodId);

    boolean existsByBillingPeriodIdAndTaskId(Long billingPeriodId, Long taskId);
    
    boolean existsByTaskId(Long taskId);
    
    Optional<BillingPeriodTask> findByTaskId(Long taskId);

    @Query(value = """
        SELECT bpt.id as billing_period_task_id,
               t.code as task_code,
               t.title as task_title,
               t.description as task_description,
               t.amount as task_amount,
               r.name as requester_name,
               r.email as requester_email,
               t.flow_type as task_flow_type,
               t.task_type as task_type
        FROM billing_period_task bpt
        JOIN task t ON bpt.task_id = t.id
        JOIN requester r ON t.requester_id = r.id
        WHERE bpt.billing_period_id = :billingPeriodId
        ORDER BY t.code
        """, nativeQuery = true)
    List<Object[]> findTasksWithDetailsByBillingPeriodId(@Param("billingPeriodId") Long billingPeriodId);

    @Query(value = """
        SELECT bpt.id as billing_period_task_id,
               t.code as task_code,
               t.title as task_title,
               t.description as task_description,
               t.amount as task_amount,
               r.name as requester_name,
               r.email as requester_email,
               t.flow_type as task_flow_type,
               t.task_type as task_type
        FROM billing_period_task bpt
        JOIN task t ON bpt.task_id = t.id
        JOIN requester r ON t.requester_id = r.id
        WHERE bpt.billing_period_id = :billingPeriodId
          AND (:flowType IS NULL OR :flowType = '' OR :flowType = 'TODOS' OR t.flow_type = :flowType)
        ORDER BY t.code
        """, nativeQuery = true)
    List<Object[]> findTasksWithDetailsByBillingPeriodIdAndFlowType(
        @Param("billingPeriodId") Long billingPeriodId,
        @Param("flowType") String flowType
    );

    @Query("""
        SELECT bpt FROM BillingPeriodTask bpt
        JOIN FETCH bpt.task t
        JOIN FETCH t.requester
        WHERE bpt.billingPeriod.id = :billingPeriodId
          AND (:flowType IS NULL OR t.flowType = :flowType)
        ORDER BY bpt.id DESC
        """)
    List<BillingPeriodTask> findByBillingPeriodIdAndFlowType(
        @Param("billingPeriodId") Long billingPeriodId,
        @Param("flowType") FlowType flowType
    );
}
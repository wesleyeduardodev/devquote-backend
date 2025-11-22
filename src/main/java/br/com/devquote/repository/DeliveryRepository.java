package br.com.devquote.repository;
import br.com.devquote.entity.Delivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    @Query("SELECT d FROM Delivery d ORDER BY d.id ASC")
    List<Delivery> findAllOrderedById();

    @EntityGraph(attributePaths = {"task", "items", "items.project"})
    @Override
    Optional<Delivery> findById(Long id);

    @Query("SELECT d.id FROM Delivery d JOIN d.task t ORDER BY t.id DESC")
    Page<Long> findAllOrderedByTaskIdDescPaginated(Pageable pageable);

    @EntityGraph(attributePaths = {"task", "items", "items.project"})
    @Query("SELECT d FROM Delivery d WHERE d.id IN :ids ORDER BY d.task.id DESC")
    List<Delivery> findByIdsWithEntityGraph(@Param("ids") List<Long> ids);

    @Query("""
        SELECT d.id
          FROM Delivery d
          JOIN d.task t
         WHERE (:taskId IS NULL OR t.id = :taskId)
           AND (:taskName IS NULL OR :taskName = '' OR LOWER(t.title) LIKE LOWER(CONCAT('%', :taskName, '%')))
           AND (:taskCode IS NULL OR :taskCode = '' OR LOWER(t.code) LIKE LOWER(CONCAT('%', :taskCode, '%')))
           AND (:flowType IS NULL OR :flowType = '' OR CAST(t.flowType AS string) = :flowType)
           AND (:taskType IS NULL OR :taskType = '' OR CAST(t.taskType AS string) = :taskType)
           AND (:environment IS NULL OR :environment = '' OR CAST(t.environment AS string) = :environment)
           AND (:status IS NULL OR :status = '' OR d.status = :status)
           AND (:startDate IS NULL OR :startDate = '' OR CAST(d.startedAt AS date) >= CAST(:startDate AS date))
           AND (:endDate IS NULL OR :endDate = '' OR CAST(d.finishedAt AS date) <= CAST(:endDate AS date))
         ORDER BY t.id DESC
        """)
    Page<Long> findIdsByOptionalFieldsPaginated(
            @Param("taskId") Long taskId,
            @Param("taskName") String taskName,
            @Param("taskCode") String taskCode,
            @Param("flowType") String flowType,
            @Param("taskType") String taskType,
            @Param("environment") String environment,
            @Param("status") br.com.devquote.enums.DeliveryStatus status,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("createdAt") String createdAt,
            @Param("updatedAt") String updatedAt,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"task", "items", "items.project"})
    @Query("SELECT d FROM Delivery d WHERE d.task.id = :taskId")
    Optional<Delivery> findByTaskId(@Param("taskId") Long taskId);
    
    boolean existsByTaskId(Long taskId);
    
    @Modifying
    @Query("DELETE FROM Delivery d WHERE d.task.id = :taskId")
    void deleteByTaskId(@Param("taskId") Long taskId);


    @Query(value = """
        SELECT
            d.id as delivery_id,
            d.status as delivery_status,
            t.id as task_id,
            t.title as task_name,
            t.code as task_code,
            t.task_type as task_type,
            t.amount as task_value,
            t.created_at,
            t.updated_at,
            (COUNT(di.id) + COUNT(doi.id)) as total_items,
            (SUM(CASE WHEN di.status = 'PENDING' THEN 1 ELSE 0 END) + SUM(CASE WHEN doi.status = 'PENDING' THEN 1 ELSE 0 END)) as pending_count,
            SUM(CASE WHEN di.status = 'DEVELOPMENT' THEN 1 ELSE 0 END) as development_count,
            (SUM(CASE WHEN di.status = 'DELIVERED' THEN 1 ELSE 0 END) + SUM(CASE WHEN doi.status = 'DELIVERED' THEN 1 ELSE 0 END)) as delivered_count,
            SUM(CASE WHEN di.status = 'HOMOLOGATION' THEN 1 ELSE 0 END) as homologation_count,
            SUM(CASE WHEN di.status = 'APPROVED' THEN 1 ELSE 0 END) as approved_count,
            SUM(CASE WHEN di.status = 'REJECTED' THEN 1 ELSE 0 END) as rejected_count,
            SUM(CASE WHEN di.status = 'PRODUCTION' THEN 1 ELSE 0 END) as production_count
        FROM task t
        INNER JOIN delivery d ON d.task_id = t.id
        LEFT JOIN delivery_item di ON di.delivery_id = d.id
        LEFT JOIN delivery_operational_item doi ON doi.delivery_id = d.id
        WHERE t.id = :taskId
        GROUP BY d.id, d.status, t.id, t.title, t.code, t.task_type, t.amount, t.created_at, t.updated_at
        """, nativeQuery = true)
    Object[] findDeliveryGroupByTaskIdOptimized(@Param("taskId") Long taskId);

    @Query(value = """
        SELECT
            SUM(CASE WHEN d.status = 'PENDING' THEN 1 ELSE 0 END) as pending_count,
            SUM(CASE WHEN d.status = 'DEVELOPMENT' THEN 1 ELSE 0 END) as development_count,
            SUM(CASE WHEN d.status = 'DELIVERED' THEN 1 ELSE 0 END) as delivered_count,
            SUM(CASE WHEN d.status = 'HOMOLOGATION' THEN 1 ELSE 0 END) as homologation_count,
            SUM(CASE WHEN d.status = 'APPROVED' THEN 1 ELSE 0 END) as approved_count,
            SUM(CASE WHEN d.status = 'REJECTED' THEN 1 ELSE 0 END) as rejected_count,
            SUM(CASE WHEN d.status = 'PRODUCTION' THEN 1 ELSE 0 END) as production_count
        FROM delivery d
        """, nativeQuery = true)
    Object[] findGlobalDeliveryStatistics();

    @Query(value = """
        SELECT
            t.task_type AS tipoTarefa,
            d.environment AS ambiente,
            COUNT(d.id) AS quantidade
        FROM delivery d
        INNER JOIN task t ON t.id = d.task_id
        WHERE
            d.flow_type = 'OPERACIONAL'
            AND (d.started_at BETWEEN :dataInicio AND :dataFim OR
                 d.finished_at BETWEEN :dataInicio AND :dataFim)
            AND (:tipoTarefa IS NULL OR t.task_type = :tipoTarefa)
            AND (:ambiente IS NULL OR d.environment = CAST(:ambiente AS VARCHAR))
        GROUP BY t.task_type, d.environment
        ORDER BY t.task_type, d.environment
        """, nativeQuery = true)
    List<Object[]> findOperationalReportData(
            @Param("dataInicio") java.time.LocalDateTime dataInicio,
            @Param("dataFim") java.time.LocalDateTime dataFim,
            @Param("tipoTarefa") String tipoTarefa,
            @Param("ambiente") String ambiente
    );

    @Query(value = """
        SELECT
            MIN(COALESCE(d.started_at, d.finished_at, d.created_at)),
            MAX(COALESCE(d.finished_at, d.started_at, d.updated_at, d.created_at))
        FROM delivery d
        WHERE d.flow_type = 'OPERACIONAL'
        """, nativeQuery = true)
    List<Object[]> findOperationalDateRange();
}

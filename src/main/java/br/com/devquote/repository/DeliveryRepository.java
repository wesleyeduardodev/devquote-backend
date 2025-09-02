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

    @EntityGraph(attributePaths = {"task", "items", "items.project"})
    @Query("""
        SELECT d
          FROM Delivery d
          JOIN d.task t
         WHERE (:id IS NULL OR d.id = :id)
           AND (:taskName IS NULL OR :taskName = '' OR LOWER(t.title) LIKE LOWER(CONCAT('%', :taskName, '%')))
           AND (:taskCode IS NULL OR :taskCode = '' OR LOWER(t.code) LIKE LOWER(CONCAT('%', :taskCode, '%')))        
           AND (:status IS NULL OR :status = '' OR d.status = :status)
           AND (:createdAt IS NULL OR :createdAt = '' OR CAST(d.createdAt AS string) LIKE CONCAT('%', :createdAt, '%'))
           AND (:updatedAt IS NULL OR :updatedAt = '' OR CAST(d.updatedAt AS string) LIKE CONCAT('%', :updatedAt, '%'))
        """)
    Page<Delivery> findByOptionalFieldsPaginated(
            @Param("id") Long id,
            @Param("taskName") String taskName,
            @Param("taskCode") String taskCode,
            @Param("status") String status,
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

    /**
     * Query otimizada para buscar delivery com itens agrupados por tarefa
     */
    @Query(value = """
        SELECT 
            d.id as delivery_id,
            d.status as delivery_status,
            t.id as task_id,
            t.title as task_name,
            t.code as task_code,
            t.amount as task_value,
            t.created_at,
            t.updated_at,
            COUNT(di.id) as total_items,
            SUM(CASE WHEN di.status = 'PENDING' THEN 1 ELSE 0 END) as pending_count,
            SUM(CASE WHEN di.status = 'DEVELOPMENT' THEN 1 ELSE 0 END) as development_count,
            SUM(CASE WHEN di.status = 'DELIVERED' THEN 1 ELSE 0 END) as delivered_count,
            SUM(CASE WHEN di.status = 'HOMOLOGATION' THEN 1 ELSE 0 END) as homologation_count,
            SUM(CASE WHEN di.status = 'APPROVED' THEN 1 ELSE 0 END) as approved_count,
            SUM(CASE WHEN di.status = 'REJECTED' THEN 1 ELSE 0 END) as rejected_count,
            SUM(CASE WHEN di.status = 'PRODUCTION' THEN 1 ELSE 0 END) as production_count
        FROM task t
        INNER JOIN delivery d ON d.task_id = t.id
        LEFT JOIN delivery_item di ON di.delivery_id = d.id
        WHERE t.id = :taskId
        GROUP BY d.id, d.status, t.id, t.title, t.code, t.amount, t.created_at, t.updated_at
        """, nativeQuery = true)
    Object[] findDeliveryGroupByTaskIdOptimized(@Param("taskId") Long taskId);
}

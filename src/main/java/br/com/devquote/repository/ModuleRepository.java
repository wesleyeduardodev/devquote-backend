package br.com.devquote.repository;
import br.com.devquote.entity.SystemModule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<SystemModule, Long> {

    @Query("SELECT m FROM SystemModule m ORDER BY m.name ASC")
    List<SystemModule> findAllOrderedByName();

    @Query("""
            SELECT m FROM SystemModule m
            WHERE (:id IS NULL OR m.id = :id)
              AND (:name IS NULL OR :name = '' OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%')))
              AND (:createdAt IS NULL OR :createdAt = '' OR CAST(m.createdAt AS string) LIKE CONCAT('%', :createdAt, '%'))
              AND (:updatedAt IS NULL OR :updatedAt = '' OR CAST(m.updatedAt AS string) LIKE CONCAT('%', :updatedAt, '%'))
            """)
    Page<SystemModule> findByOptionalFieldsPaginated(
            @Param("id") Long id,
            @Param("name") String name,
            @Param("createdAt") String createdAt,
            @Param("updatedAt") String updatedAt,
            Pageable pageable
    );
}

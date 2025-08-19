package br.com.devquote.repository;
import br.com.devquote.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT p FROM Project p ORDER BY p.id ASC")
    List<Project> findAllOrderedById();

    @Query("""
            SELECT p FROM Project p
            WHERE (:id IS NULL OR p.id = :id)
              AND (:name IS NULL OR :name = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
              AND (:repositoryUrl IS NULL OR :repositoryUrl = '' OR LOWER(p.repositoryUrl) LIKE LOWER(CONCAT('%', :repositoryUrl, '%')))
              AND (:createdAt IS NULL OR :createdAt = '' OR CAST(p.createdAt AS string) LIKE CONCAT('%', :createdAt, '%'))
              AND (:updatedAt IS NULL OR :updatedAt = '' OR CAST(p.updatedAt AS string) LIKE CONCAT('%', :updatedAt, '%'))
            """)
    Page<Project> findByOptionalFieldsPaginated(
            @Param("id") Long id,
            @Param("name") String name,
            @Param("repositoryUrl") String repositoryUrl,
            @Param("createdAt") String createdAt,
            @Param("updatedAt") String updatedAt,
            Pageable pageable
    );
}

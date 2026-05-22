package br.com.devquote.repository;
import br.com.devquote.entity.Server;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServerRepository extends JpaRepository<Server, Long> {

    @Query("SELECT s FROM Server s ORDER BY s.name ASC")
    List<Server> findAllOrderedByName();

    @Query("""
            SELECT s FROM Server s
            WHERE (:id IS NULL OR s.id = :id)
              AND (:name IS NULL OR :name = '' OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')))
              AND (:link IS NULL OR :link = '' OR LOWER(s.link) LIKE LOWER(CONCAT('%', :link, '%')))
              AND (:createdAt IS NULL OR :createdAt = '' OR CAST(s.createdAt AS string) LIKE CONCAT('%', :createdAt, '%'))
              AND (:updatedAt IS NULL OR :updatedAt = '' OR CAST(s.updatedAt AS string) LIKE CONCAT('%', :updatedAt, '%'))
            """)
    Page<Server> findByOptionalFieldsPaginated(
            @Param("id") Long id,
            @Param("name") String name,
            @Param("link") String link,
            @Param("createdAt") String createdAt,
            @Param("updatedAt") String updatedAt,
            Pageable pageable
    );
}

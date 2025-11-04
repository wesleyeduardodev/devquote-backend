package br.com.devquote.repository;
import br.com.devquote.entity.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByCode(String code);

    @Query("SELECT p FROM Profile p WHERE p.active = true ORDER BY p.level ASC, p.name ASC")
    List<Profile> findAllOrderedByLevel();

    @Query("SELECT p FROM Profile p WHERE " +
           "(:id IS NULL OR p.id = :id) AND " +
           "(:code IS NULL OR :code = '' OR LOWER(p.code) LIKE LOWER(CONCAT('%', :code, '%'))) AND " +
           "(:name IS NULL OR :name = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:description IS NULL OR :description = '' OR p.description IS NULL OR LOWER(p.description) LIKE LOWER(CONCAT('%', :description, '%'))) AND " +
           "(:level IS NULL OR p.level = :level) AND " +
           "(:active IS NULL OR p.active = :active)")
    Page<Profile> findAllWithFilters(@Param("id") Long id,
                                      @Param("code") String code,
                                      @Param("name") String name,
                                      @Param("description") String description,
                                      @Param("level") Integer level,
                                      @Param("active") Boolean active,
                                      Pageable pageable);

    boolean existsByCode(String code);
}
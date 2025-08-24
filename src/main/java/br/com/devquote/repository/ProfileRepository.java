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

    List<Profile> findByActiveTrue();

    @Query("SELECT p FROM Profile p WHERE p.active = true ORDER BY p.level ASC, p.name ASC")
    List<Profile> findAllOrderedByLevel();

    @Query("SELECT p FROM Profile p WHERE p.active = true AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Profile> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    boolean existsByCode(String code);
}
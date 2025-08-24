package br.com.devquote.repository;

import br.com.devquote.entity.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    Optional<Resource> findByCode(String code);

    List<Resource> findByActiveTrue();

    @Query("SELECT r FROM Resource r WHERE r.active = true ORDER BY r.name ASC")
    List<Resource> findAllOrderedByName();

    @Query("SELECT r FROM Resource r WHERE r.active = true AND (:name IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Resource> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    boolean existsByCode(String code);
}
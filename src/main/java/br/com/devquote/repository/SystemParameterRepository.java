package br.com.devquote.repository;

import br.com.devquote.entity.SystemParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemParameterRepository extends JpaRepository<SystemParameter, Long> {

    Optional<SystemParameter> findByName(String name);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    @Query("SELECT sp FROM SystemParameter sp ORDER BY sp.id ASC")
    List<SystemParameter> findAllOrderedById();

    @Query("SELECT sp FROM SystemParameter sp")
    Page<SystemParameter> findByOptionalFieldsPaginated(
            Long id,
            String name,
            String description,
            String createdAt,
            String updatedAt,
            Pageable pageable
    );
}

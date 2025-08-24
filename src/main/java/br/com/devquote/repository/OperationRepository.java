package br.com.devquote.repository;

import br.com.devquote.entity.Operation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OperationRepository extends JpaRepository<Operation, Long> {

    Optional<Operation> findByCode(String code);

    List<Operation> findByActiveTrue();

    @Query("SELECT o FROM Operation o WHERE o.active = true ORDER BY o.name ASC")
    List<Operation> findAllOrderedByName();

    boolean existsByCode(String code);
}
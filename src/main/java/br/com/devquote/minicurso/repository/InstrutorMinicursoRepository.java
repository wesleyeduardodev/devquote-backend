package br.com.devquote.minicurso.repository;

import br.com.devquote.minicurso.entity.InstrutorMinicurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstrutorMinicursoRepository extends JpaRepository<InstrutorMinicurso, Long> {

    List<InstrutorMinicurso> findByAtivoTrueOrderByNomeAsc();

    @Query("SELECT DISTINCT i FROM InstrutorMinicurso i JOIN i.modulos m WHERE m.id = :moduloId AND i.ativo = true")
    List<InstrutorMinicurso> findByModulosIdAndAtivoTrue(@Param("moduloId") Long moduloId);

    @Query("SELECT i FROM InstrutorMinicurso i WHERE i.modulos IS EMPTY AND i.ativo = true ORDER BY i.nome ASC")
    List<InstrutorMinicurso> findInstrutoresGeraisAtivos();

    @Query("SELECT DISTINCT i FROM InstrutorMinicurso i LEFT JOIN FETCH i.modulos ORDER BY i.nome ASC")
    List<InstrutorMinicurso> findAllWithModulosOrderByNomeAsc();

    @Query("SELECT i FROM InstrutorMinicurso i LEFT JOIN FETCH i.modulos WHERE i.id = :id")
    Optional<InstrutorMinicurso> findByIdWithModulos(@Param("id") Long id);

    List<InstrutorMinicurso> findAllByOrderByNomeAsc();

    boolean existsByEmail(String email);
}

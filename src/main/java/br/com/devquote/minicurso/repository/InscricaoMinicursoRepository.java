package br.com.devquote.minicurso.repository;

import br.com.devquote.minicurso.entity.InscricaoMinicurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InscricaoMinicursoRepository extends JpaRepository<InscricaoMinicurso, Long> {

    boolean existsByEmail(String email);

    Optional<InscricaoMinicurso> findByEmail(String email);
}

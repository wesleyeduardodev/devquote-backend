package br.com.devquote.minicurso.repository;

import br.com.devquote.minicurso.entity.ConfiguracaoEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracaoEventoRepository extends JpaRepository<ConfiguracaoEvento, Long> {

    Optional<ConfiguracaoEvento> findFirstByOrderByIdDesc();
}

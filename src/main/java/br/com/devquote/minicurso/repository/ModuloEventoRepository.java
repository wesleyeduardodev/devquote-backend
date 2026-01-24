package br.com.devquote.minicurso.repository;

import br.com.devquote.minicurso.entity.ModuloEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuloEventoRepository extends JpaRepository<ModuloEvento, Long> {

    List<ModuloEvento> findByConfiguracaoEventoIdAndAtivoTrueOrderByOrdemAsc(Long configuracaoEventoId);

    List<ModuloEvento> findByConfiguracaoEventoIdOrderByOrdemAsc(Long configuracaoEventoId);
}

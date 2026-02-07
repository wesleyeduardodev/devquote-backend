package br.com.devquote.minicurso.repository;

import br.com.devquote.minicurso.entity.DataEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataEventoRepository extends JpaRepository<DataEvento, Long> {

    List<DataEvento> findByConfiguracaoEventoIdOrderByOrdemAsc(Long configuracaoEventoId);
}

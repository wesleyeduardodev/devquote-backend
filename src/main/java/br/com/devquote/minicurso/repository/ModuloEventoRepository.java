package br.com.devquote.minicurso.repository;

import br.com.devquote.minicurso.dto.response.ModuloSimplificadoResponse;
import br.com.devquote.minicurso.entity.ModuloEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuloEventoRepository extends JpaRepository<ModuloEvento, Long> {

    List<ModuloEvento> findByConfiguracaoEventoIdAndAtivoTrueOrderByOrdemAsc(Long configuracaoEventoId);

    List<ModuloEvento> findByConfiguracaoEventoIdOrderByOrdemAsc(Long configuracaoEventoId);

    @Query("SELECT new br.com.devquote.minicurso.dto.response.ModuloSimplificadoResponse(m.id, m.titulo) " +
           "FROM ModuloEvento m JOIN m.instrutores i WHERE i.id = :instrutorId ORDER BY m.titulo")
    List<ModuloSimplificadoResponse> findModulosByInstrutorId(@Param("instrutorId") Long instrutorId);
}

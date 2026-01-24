package br.com.devquote.minicurso.repository;

import br.com.devquote.minicurso.entity.ItemModulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemModuloRepository extends JpaRepository<ItemModulo, Long> {

    List<ItemModulo> findByModuloIdAndAtivoTrueOrderByOrdemAsc(Long moduloId);

    List<ItemModulo> findByModuloIdOrderByOrdemAsc(Long moduloId);
}

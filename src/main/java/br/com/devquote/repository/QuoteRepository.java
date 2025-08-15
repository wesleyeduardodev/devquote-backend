package br.com.devquote.repository;
import br.com.devquote.entity.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {

    boolean existsByTaskId(Long taskId);

    @Query("SELECT q FROM Quote q ORDER BY q.id ASC")
    List<Quote> findAllOrderedById();
}

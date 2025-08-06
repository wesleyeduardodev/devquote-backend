package br.com.devquote.repository;
import br.com.devquote.entity.MeasurementQuote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeasurementQuoteRepository extends JpaRepository<MeasurementQuote, Long> {
}
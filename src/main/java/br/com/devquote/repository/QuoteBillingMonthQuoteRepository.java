package br.com.devquote.repository;
import br.com.devquote.entity.QuoteBillingMonthQuote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuoteBillingMonthQuoteRepository extends JpaRepository<QuoteBillingMonthQuote, Long> {

    boolean existsByQuoteBillingMonth_IdAndQuote_Id(Long quoteBillingMonthId, Long quoteId);
}
package br.com.devquote.repository;
import br.com.devquote.entity.QuoteBillingMonthQuote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuoteBillingMonthQuoteRepository extends JpaRepository<QuoteBillingMonthQuote, Long> {

    boolean existsByQuoteBillingMonth_IdAndQuote_Id(Long quoteBillingMonthId, Long quoteId);

    List<QuoteBillingMonthQuote> findByQuoteBillingMonth_Id(Long billingMonthId);

    @Query("SELECT q FROM QuoteBillingMonthQuote q ORDER BY q.id ASC")
    List<QuoteBillingMonthQuote> findAllOrderedById();

    @Query("""
           SELECT qbmq FROM QuoteBillingMonthQuote qbmq
           WHERE qbmq.quoteBillingMonth.id = :billingMonthId
             AND qbmq.quote.id = :quoteId
           """)
    Optional<QuoteBillingMonthQuote> findByQuoteBillingMonthIdAndQuoteId(Long billingMonthId, Long quoteId);
}

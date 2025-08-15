package br.com.devquote.repository;
import br.com.devquote.entity.QuoteBillingMonth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuoteBillingMonthRepository extends JpaRepository<QuoteBillingMonth, Long> {

    @Query("SELECT q FROM QuoteBillingMonth q ORDER BY q.id ASC")
    List<QuoteBillingMonth> findAllOrderedById();
}

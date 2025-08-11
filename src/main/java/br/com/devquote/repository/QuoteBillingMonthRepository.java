package br.com.devquote.repository;
import br.com.devquote.entity.QuoteBillingMonth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuoteBillingMonthRepository extends JpaRepository<QuoteBillingMonth, Long> {
}

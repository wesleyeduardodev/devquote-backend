package br.com.devquote.repository;
import br.com.devquote.entity.BillingNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BillingNoteRepository extends JpaRepository<BillingNote, Long> {

    @Query("SELECT bn FROM BillingNote bn LEFT JOIN FETCH bn.createdBy WHERE bn.billingPeriod IS NULL ORDER BY bn.createdAt DESC")
    List<BillingNote> findGeneralNotes();

    @Query("SELECT bn FROM BillingNote bn LEFT JOIN FETCH bn.createdBy WHERE bn.billingPeriod.id = :billingPeriodId ORDER BY bn.createdAt DESC")
    List<BillingNote> findByBillingPeriodId(@Param("billingPeriodId") Long billingPeriodId);

    @Query("SELECT COUNT(bn) FROM BillingNote bn WHERE bn.billingPeriod IS NULL")
    long countGeneralNotes();

    @Query("SELECT bn.billingPeriod.id, COUNT(bn) FROM BillingNote bn WHERE bn.billingPeriod IS NOT NULL GROUP BY bn.billingPeriod.id")
    List<Object[]> countGroupedByBillingPeriod();
}

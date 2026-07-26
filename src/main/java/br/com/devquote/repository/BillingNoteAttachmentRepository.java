package br.com.devquote.repository;
import br.com.devquote.entity.BillingNoteAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BillingNoteAttachmentRepository extends JpaRepository<BillingNoteAttachment, Long> {

    @Query("SELECT bna FROM BillingNoteAttachment bna WHERE bna.billingNote.id = :billingNoteId AND bna.excluded = false ORDER BY bna.createdAt ASC")
    List<BillingNoteAttachment> findByBillingNoteId(@Param("billingNoteId") Long billingNoteId);

    @Query("SELECT bna.billingNote.id, COUNT(bna) FROM BillingNoteAttachment bna WHERE bna.excluded = false AND bna.billingNote.id IN :billingNoteIds GROUP BY bna.billingNote.id")
    List<Object[]> countGroupedByBillingNoteIds(@Param("billingNoteIds") List<Long> billingNoteIds);
}

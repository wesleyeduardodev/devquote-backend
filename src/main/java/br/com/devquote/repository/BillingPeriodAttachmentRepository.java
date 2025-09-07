package br.com.devquote.repository;

import br.com.devquote.entity.BillingPeriodAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillingPeriodAttachmentRepository extends JpaRepository<BillingPeriodAttachment, Long> {

    @Query("SELECT bpa FROM BillingPeriodAttachment bpa WHERE bpa.billingPeriod.id = :billingPeriodId AND bpa.excluded = false ORDER BY bpa.createdAt ASC")
    List<BillingPeriodAttachment> findByBillingPeriodId(@Param("billingPeriodId") Long billingPeriodId);

    @Query("SELECT COUNT(bpa) FROM BillingPeriodAttachment bpa WHERE bpa.billingPeriod.id = :billingPeriodId AND bpa.excluded = false")
    Long countByBillingPeriodId(@Param("billingPeriodId") Long billingPeriodId);

    @Query("SELECT bpa FROM BillingPeriodAttachment bpa WHERE bpa.filePath = :filePath")
    BillingPeriodAttachment findByFilePath(@Param("filePath") String filePath);
}
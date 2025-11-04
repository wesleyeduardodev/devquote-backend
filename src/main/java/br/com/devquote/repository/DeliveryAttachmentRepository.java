package br.com.devquote.repository;
import br.com.devquote.entity.DeliveryAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DeliveryAttachmentRepository extends JpaRepository<DeliveryAttachment, Long> {

    @Query("SELECT da FROM DeliveryAttachment da WHERE da.delivery.id = :deliveryId ORDER BY da.createdAt ASC")
    List<DeliveryAttachment> findByDeliveryId(@Param("deliveryId") Long deliveryId);}
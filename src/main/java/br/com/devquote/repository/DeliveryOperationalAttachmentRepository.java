package br.com.devquote.repository;
import br.com.devquote.entity.DeliveryOperationalAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DeliveryOperationalAttachmentRepository extends JpaRepository<DeliveryOperationalAttachment, Long> {

    List<DeliveryOperationalAttachment> findByDeliveryOperationalItemId(Long deliveryOperationalItemId);

    @Query("SELECT a FROM DeliveryOperationalAttachment a " +
           "JOIN a.deliveryOperationalItem i " +
           "WHERE i.delivery.id = :deliveryId")
    List<DeliveryOperationalAttachment> findByDeliveryId(@Param("deliveryId") Long deliveryId);
}

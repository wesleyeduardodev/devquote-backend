package br.com.devquote.repository;
import br.com.devquote.entity.DeliveryOperationalItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DeliveryOperationalItemRepository extends JpaRepository<DeliveryOperationalItem, Long> {

    @Query("SELECT doi FROM DeliveryOperationalItem doi LEFT JOIN FETCH doi.attachments WHERE doi.delivery.id = :deliveryId")
    List<DeliveryOperationalItem> findByDeliveryIdWithAttachments(@Param("deliveryId") Long deliveryId);

    List<DeliveryOperationalItem> findByDeliveryId(Long deliveryId);

    void deleteByDeliveryId(Long deliveryId);
}

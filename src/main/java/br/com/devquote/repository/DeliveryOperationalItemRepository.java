package br.com.devquote.repository;
import br.com.devquote.entity.DeliveryOperationalItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DeliveryOperationalItemRepository extends JpaRepository<DeliveryOperationalItem, Long> {

    @Query("SELECT doi FROM DeliveryOperationalItem doi LEFT JOIN FETCH doi.attachments WHERE doi.delivery.id = :deliveryId ORDER BY doi.sortOrder ASC, doi.id ASC")
    List<DeliveryOperationalItem> findByDeliveryIdWithAttachments(@Param("deliveryId") Long deliveryId);

    @Query("SELECT doi FROM DeliveryOperationalItem doi WHERE doi.delivery.id = :deliveryId ORDER BY doi.sortOrder ASC, doi.id ASC")
    List<DeliveryOperationalItem> findByDeliveryId(@Param("deliveryId") Long deliveryId);

    void deleteByDeliveryId(Long deliveryId);

    @Query("SELECT COALESCE(MAX(doi.sortOrder), 0) FROM DeliveryOperationalItem doi WHERE doi.delivery.id = :deliveryId")
    Integer findMaxSortOrderByDeliveryId(@Param("deliveryId") Long deliveryId);
}

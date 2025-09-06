package br.com.devquote.repository;

import br.com.devquote.entity.DeliveryItemAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryItemAttachmentRepository extends JpaRepository<DeliveryItemAttachment, Long> {

    @Query("SELECT dia FROM DeliveryItemAttachment dia WHERE dia.deliveryItem.id = :deliveryItemId ORDER BY dia.createdAt ASC")
    List<DeliveryItemAttachment> findByDeliveryItemId(@Param("deliveryItemId") Long deliveryItemId);

    @Query("SELECT COUNT(dia) FROM DeliveryItemAttachment dia WHERE dia.deliveryItem.id = :deliveryItemId")
    Long countByDeliveryItemId(@Param("deliveryItemId") Long deliveryItemId);

    @Query("SELECT dia FROM DeliveryItemAttachment dia WHERE dia.filePath = :filePath")
    DeliveryItemAttachment findByFilePath(@Param("filePath") String filePath);

    @Query("SELECT dia FROM DeliveryItemAttachment dia WHERE dia.deliveryItem.delivery.id = :deliveryId")
    List<DeliveryItemAttachment> findByDeliveryId(@Param("deliveryId") Long deliveryId);
}
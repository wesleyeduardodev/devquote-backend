package br.com.devquote.repository;

import br.com.devquote.entity.DeliveryOperationalAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryOperationalAttachmentRepository extends JpaRepository<DeliveryOperationalAttachment, Long> {

    List<DeliveryOperationalAttachment> findByDeliveryOperationalItemId(Long deliveryOperationalItemId);

    void deleteByDeliveryOperationalItemId(Long deliveryOperationalItemId);

    long countByDeliveryOperationalItemId(Long deliveryOperationalItemId);
}

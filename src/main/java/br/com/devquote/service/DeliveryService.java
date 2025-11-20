package br.com.devquote.service;
import br.com.devquote.dto.request.DeliveryRequest;
import br.com.devquote.dto.response.DeliveryResponse;
import br.com.devquote.dto.response.DeliveryGroupResponse;
import br.com.devquote.dto.response.DeliveryStatusCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.io.IOException;
import java.util.List;

public interface DeliveryService {

    List<DeliveryResponse> findAll();

    DeliveryResponse findById(Long id);

    DeliveryResponse create(DeliveryRequest dto);

    DeliveryResponse update(Long id, DeliveryRequest dto);

    void delete(Long id);

    void deleteBulk(List<Long> ids);
    
    void deleteByTaskId(Long taskId);

    Page<DeliveryResponse> findAllPaginated(Long id,
                                            String taskName,
                                            String taskCode,
                                            String flowType,
                                            String status,
                                            String createdAt,
                                            String updatedAt,
                                            Pageable pageable);

    Page<DeliveryGroupResponse> findAllGroupedByTask(Long taskId,
                                                      String taskName,
                                                      String taskCode,
                                                      String flowType,
                                                      String taskType,
                                                      String environment,
                                                      String status,
                                                      String startDate,
                                                      String endDate,
                                                      String createdAt,
                                                      String updatedAt,
                                                      Pageable pageable);

    DeliveryGroupResponse findGroupDetailsByTaskId(Long taskId);

    Page<DeliveryGroupResponse> findAllGroupedByTaskOptimized(String taskName,
                                                              String taskCode,
                                                              String status,
                                                              String createdAt,
                                                              String updatedAt,
                                                              Pageable pageable);

    DeliveryGroupResponse findGroupDetailsByTaskIdOptimized(Long taskId);

    boolean existsByTaskId(Long taskId);

    DeliveryStatusCount getGlobalStatistics();

    void updateAllDeliveryStatuses();

    byte[] exportToExcel(String flowType, boolean canViewAmounts) throws IOException;
    
    DeliveryResponse findByTaskId(Long taskId);

    void sendDeliveryEmail(Long id, List<String> additionalEmails, List<String> additionalWhatsAppRecipients, boolean sendEmail, boolean sendWhatsApp);

    DeliveryResponse updateNotes(Long id, String notes);
}
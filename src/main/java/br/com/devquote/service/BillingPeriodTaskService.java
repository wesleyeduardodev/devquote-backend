package br.com.devquote.service;

import br.com.devquote.dto.request.BillingPeriodTaskRequest;
import br.com.devquote.dto.response.BillingPeriodTaskResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface BillingPeriodTaskService {
    List<BillingPeriodTaskResponse> findAll();
    BillingPeriodTaskResponse findById(Long id);
    BillingPeriodTaskResponse create(BillingPeriodTaskRequest dto);
    BillingPeriodTaskResponse update(Long id, BillingPeriodTaskRequest dto);
    void delete(Long id);
    
    List<BillingPeriodTaskResponse> findTaskLinksByBillingPeriod(Long billingPeriodId);
    List<BillingPeriodTaskResponse> bulkLinkTasks(List<BillingPeriodTaskRequest> requests);
    void bulkUnlinkTasks(Long billingPeriodId, List<Long> taskIds);
    Page<BillingPeriodTaskResponse> findTaskLinksPaginated(Long billingPeriodId, Pageable pageable);
    boolean existsByTaskId(Long taskId);
    Optional<BillingPeriodTaskResponse> findByTaskId(Long taskId);
}
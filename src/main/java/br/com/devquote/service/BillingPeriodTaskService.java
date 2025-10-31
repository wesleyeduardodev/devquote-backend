package br.com.devquote.service;

import br.com.devquote.dto.request.BillingPeriodTaskRequest;
import br.com.devquote.dto.response.BillingPeriodTaskResponse;
import br.com.devquote.enums.FlowType;
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
    void deleteBulk(List<Long> ids);

    List<BillingPeriodTaskResponse> findByBillingPeriod(Long billingPeriodId);
    List<BillingPeriodTaskResponse> findByBillingPeriodAndFlowType(Long billingPeriodId, FlowType flowType);
    Page<BillingPeriodTaskResponse> findByBillingPeriodPaginated(Long billingPeriodId, Pageable pageable);
    Page<BillingPeriodTaskResponse> findByBillingPeriodPaginated(Long billingPeriodId, Pageable pageable, FlowType flowType);
    List<BillingPeriodTaskResponse> bulkCreate(List<BillingPeriodTaskRequest> requests);
    void bulkUnlinkTasks(Long billingPeriodId, List<Long> taskIds);
    
    // Métodos de compatibilidade (deprecated)
    List<BillingPeriodTaskResponse> findTaskLinksByBillingPeriod(Long billingPeriodId);
    List<BillingPeriodTaskResponse> bulkLinkTasks(List<BillingPeriodTaskRequest> requests);
    Page<BillingPeriodTaskResponse> findTaskLinksPaginated(Long billingPeriodId, Pageable pageable);
    boolean existsByTaskId(Long taskId);
    Optional<BillingPeriodTaskResponse> findByTaskId(Long taskId);
}
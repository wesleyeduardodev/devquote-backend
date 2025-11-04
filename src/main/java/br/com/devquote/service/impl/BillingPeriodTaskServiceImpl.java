package br.com.devquote.service.impl;
import br.com.devquote.adapter.BillingPeriodTaskAdapter;
import br.com.devquote.dto.request.BillingPeriodTaskRequest;
import br.com.devquote.dto.response.BillingPeriodTaskResponse;
import br.com.devquote.entity.Task;
import br.com.devquote.entity.BillingPeriod;
import br.com.devquote.entity.BillingPeriodTask;
import br.com.devquote.enums.FlowType;
import br.com.devquote.repository.BillingPeriodRepository;
import br.com.devquote.repository.BillingPeriodTaskRepository;
import br.com.devquote.repository.TaskRepository;
import br.com.devquote.service.BillingPeriodTaskService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import br.com.devquote.error.TaskAlreadyInBillingException;

@Service
@Transactional
@RequiredArgsConstructor
public class BillingPeriodTaskServiceImpl implements BillingPeriodTaskService {

    private final BillingPeriodTaskRepository billingPeriodTaskRepository;
    private final BillingPeriodRepository billingPeriodRepository;
    private final TaskRepository taskRepository;

    @Override
    public List<BillingPeriodTaskResponse> findAll() {
        return billingPeriodTaskRepository.findAllOrderedById().stream()
                .map(BillingPeriodTaskAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BillingPeriodTaskResponse findById(Long id) {
        BillingPeriodTask entity = billingPeriodTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BillingPeriodTask not found"));
        return BillingPeriodTaskAdapter.toResponseDTO(entity);
    }

    @Override
    public BillingPeriodTaskResponse create(BillingPeriodTaskRequest dto) {
        BillingPeriod billingPeriod = billingPeriodRepository.findById(dto.getBillingPeriodId())
                .orElseThrow(() -> new RuntimeException("BillingPeriod not found"));
        Task task = taskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (billingPeriodTaskRepository.existsByTaskId(dto.getTaskId())) {
            Optional<BillingPeriodTask> existingBilling = billingPeriodTaskRepository.findByTaskId(dto.getTaskId());
            if (existingBilling.isPresent()) {
                BillingPeriod existingPeriod = existingBilling.get().getBillingPeriod();
                String periodInfo = String.format("%02d/%d", existingPeriod.getMonth(), existingPeriod.getYear());
                throw new TaskAlreadyInBillingException(dto.getTaskId(), periodInfo);
            }
        }

        BillingPeriodTask entity = BillingPeriodTaskAdapter.toEntity(dto, billingPeriod, task);
        entity = billingPeriodTaskRepository.save(entity);
        return BillingPeriodTaskAdapter.toResponseDTO(entity);
    }

    @Override
    public BillingPeriodTaskResponse update(Long id, BillingPeriodTaskRequest dto) {
        BillingPeriodTask entity = billingPeriodTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BillingPeriodTask not found"));

        BillingPeriod billingPeriod = billingPeriodRepository.findById(dto.getBillingPeriodId())
                .orElseThrow(() -> new RuntimeException("BillingPeriod not found"));
        Task task = taskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!entity.getTask().getId().equals(dto.getTaskId())) {
            if (billingPeriodTaskRepository.existsByTaskId(dto.getTaskId())) {
                Optional<BillingPeriodTask> existingBilling = billingPeriodTaskRepository.findByTaskId(dto.getTaskId());
                if (existingBilling.isPresent()) {
                    BillingPeriod existingPeriod = existingBilling.get().getBillingPeriod();
                    String periodInfo = String.format("%02d/%d", existingPeriod.getMonth(), existingPeriod.getYear());
                    throw new TaskAlreadyInBillingException(dto.getTaskId(), periodInfo);
                }
            }
        }

        entity.setBillingPeriod(billingPeriod);
        entity.setTask(task);
        entity = billingPeriodTaskRepository.save(entity);
        return BillingPeriodTaskAdapter.toResponseDTO(entity);
    }

    @Override
    public void delete(Long id) {
        billingPeriodTaskRepository.deleteById(id);
    }

    @Override
    public List<BillingPeriodTaskResponse> bulkLinkTasks(List<BillingPeriodTaskRequest> requests) {
        return requests.stream()
                .map(this::create)
                .collect(Collectors.toList());
    }

    @Override
    public void bulkUnlinkTasks(Long taskBillingMonthId, List<Long> taskIds) {
        if (taskIds != null && !taskIds.isEmpty()) {
            billingPeriodTaskRepository.deleteByBillingPeriodIdAndTaskIds(taskBillingMonthId, taskIds);
        }
    }

    @Override
    public Page<BillingPeriodTaskResponse> findTaskLinksPaginated(Long taskBillingMonthId, Pageable pageable) {

        Page<Long> idsPage = billingPeriodTaskRepository.findIdsByBillingPeriodIdPaginated(taskBillingMonthId, pageable);

        if (idsPage.getContent().isEmpty()) {
            return Page.empty(pageable);
        }

        List<BillingPeriodTask> tasks = billingPeriodTaskRepository.findByIdsWithDetails(idsPage.getContent());

        List<BillingPeriodTaskResponse> content = tasks.stream()
                .map(BillingPeriodTaskAdapter::toResponseDTO)
                .toList();
                
        return new PageImpl<>(content, pageable, idsPage.getTotalElements());
    }

    @Override
    public boolean existsByTaskId(Long taskId) {
        return billingPeriodTaskRepository.existsByTaskId(taskId);
    }

    @Override
    public Optional<BillingPeriodTaskResponse> findByTaskId(Long taskId) {
        return billingPeriodTaskRepository.findByTaskId(taskId)
                .map(BillingPeriodTaskAdapter::toResponseDTO);
    }

    @Override
    public void deleteBulk(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            billingPeriodTaskRepository.deleteAllById(ids);
        }
    }

    @Override
    public List<BillingPeriodTaskResponse> findByBillingPeriodAndFlowType(Long billingPeriodId, FlowType flowType) {
        return billingPeriodTaskRepository.findByBillingPeriodIdAndFlowType(billingPeriodId, flowType).stream()
                .map(BillingPeriodTaskAdapter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<BillingPeriodTaskResponse> findByBillingPeriodPaginated(Long billingPeriodId, Pageable pageable) {
        return findTaskLinksPaginated(billingPeriodId, pageable);
    }

    @Override
    public Page<BillingPeriodTaskResponse> findByBillingPeriodPaginated(Long billingPeriodId, Pageable pageable, FlowType flowType) {

        if (flowType == null) {
            return findByBillingPeriodPaginated(billingPeriodId, pageable);
        }

        Page<Long> idsPage = billingPeriodTaskRepository.findIdsByBillingPeriodIdAndFlowTypePaginated(billingPeriodId, flowType, pageable);

        if (idsPage.getContent().isEmpty()) {
            return Page.empty(pageable);
        }

        List<BillingPeriodTask> tasks = billingPeriodTaskRepository.findByIdsWithDetails(idsPage.getContent());

        List<BillingPeriodTaskResponse> content = tasks.stream()
                .map(BillingPeriodTaskAdapter::toResponseDTO)
                .toList();

        return new PageImpl<>(content, pageable, idsPage.getTotalElements());
    }

    @Override
    public List<BillingPeriodTaskResponse> bulkCreate(List<BillingPeriodTaskRequest> requests) {
        return bulkLinkTasks(requests);
    }
}

package br.com.devquote.service.impl;
import br.com.devquote.adapter.BillingNoteAdapter;
import br.com.devquote.dto.request.BillingNoteRequest;
import br.com.devquote.dto.response.BillingNoteResponse;
import br.com.devquote.entity.BillingNote;
import br.com.devquote.entity.BillingPeriod;
import br.com.devquote.entity.User;
import br.com.devquote.error.BusinessException;
import br.com.devquote.error.ResourceNotFoundException;
import br.com.devquote.repository.BillingNoteRepository;
import br.com.devquote.repository.BillingPeriodRepository;
import br.com.devquote.service.BillingNoteAttachmentService;
import br.com.devquote.service.BillingNoteService;
import br.com.devquote.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BillingNoteServiceImpl implements BillingNoteService {

    private final BillingNoteRepository billingNoteRepository;
    private final BillingPeriodRepository billingPeriodRepository;
    private final BillingNoteAttachmentService billingNoteAttachmentService;
    private final BillingNoteAdapter billingNoteAdapter;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public List<BillingNoteResponse> getGeneralNotes() {
        return toResponseList(billingNoteRepository.findGeneralNotes());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillingNoteResponse> getNotesByBillingPeriod(Long billingPeriodId) {
        return toResponseList(billingNoteRepository.findByBillingPeriodId(billingPeriodId));
    }

    @Override
    @Transactional(readOnly = true)
    public BillingNoteResponse getById(Long id) {
        BillingNote note = findNoteOrThrow(id);
        Map<Long, Integer> counts = billingNoteAttachmentService.countByBillingNoteIds(List.of(id));
        return billingNoteAdapter.toResponse(note, counts.getOrDefault(id, 0));
    }

    @Override
    public BillingNoteResponse create(BillingNoteRequest request) {
        validateContent(request);

        BillingNote note = BillingNote.builder()
                .billingPeriod(resolveBillingPeriod(request.getBillingPeriodId()))
                .title(normalize(request.getTitle()))
                .content(request.getContent())
                .createdBy(currentUser())
                .build();

        BillingNote saved = billingNoteRepository.save(note);
        log.debug("BILLING_NOTE CREATE id={} billingPeriodId={}", saved.getId(), request.getBillingPeriodId());
        return billingNoteAdapter.toResponse(saved, 0);
    }

    @Override
    public BillingNoteResponse update(Long id, BillingNoteRequest request) {
        validateContent(request);

        BillingNote note = findNoteOrThrow(id);
        note.setTitle(normalize(request.getTitle()));
        note.setContent(request.getContent());

        BillingNote saved = billingNoteRepository.save(note);
        Map<Long, Integer> counts = billingNoteAttachmentService.countByBillingNoteIds(List.of(id));
        log.debug("BILLING_NOTE UPDATE id={}", id);
        return billingNoteAdapter.toResponse(saved, counts.getOrDefault(id, 0));
    }

    @Override
    public void delete(Long id) {
        BillingNote note = findNoteOrThrow(id);
        billingNoteAttachmentService.deleteAllByBillingNote(id);
        billingNoteRepository.delete(note);
        log.debug("BILLING_NOTE DELETE id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> countNotes() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("general", billingNoteRepository.countGeneralNotes());

        for (Object[] row : billingNoteRepository.countGroupedByBillingPeriod()) {
            Long billingPeriodId = (Long) row[0];
            Long count = (Long) row[1];
            counts.put(String.valueOf(billingPeriodId), count);
        }

        return counts;
    }

    @Override
    public void deleteAllByBillingPeriod(Long billingPeriodId) {
        List<BillingNote> notes = billingNoteRepository.findByBillingPeriodId(billingPeriodId);
        for (BillingNote note : notes) {
            try {
                billingNoteAttachmentService.deleteAllByBillingNote(note.getId());
            } catch (Exception e) {
                log.error("Error deleting attachments of billing note {}: {}", note.getId(), e.getMessage());
            }
        }
        if (!notes.isEmpty()) {
            billingNoteRepository.deleteAll(notes);
            log.debug("BILLING_NOTE DELETE_BY_PERIOD billingPeriodId={} removed={}", billingPeriodId, notes.size());
        }
    }

    private List<BillingNoteResponse> toResponseList(List<BillingNote> notes) {
        if (notes.isEmpty()) {
            return List.of();
        }
        List<Long> ids = notes.stream().map(BillingNote::getId).toList();
        Map<Long, Integer> counts = billingNoteAttachmentService.countByBillingNoteIds(ids);
        return billingNoteAdapter.toResponseList(notes, counts);
    }

    private BillingNote findNoteOrThrow(Long id) {
        return billingNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Anotação de faturamento", id));
    }

    private BillingPeriod resolveBillingPeriod(Long billingPeriodId) {
        if (billingPeriodId == null) {
            return null;
        }
        return billingPeriodRepository.findById(billingPeriodId)
                .orElseThrow(() -> new ResourceNotFoundException("Período de faturamento", billingPeriodId));
    }

    private User currentUser() {
        return securityUtils.getCurrentUser();
    }

    private void validateContent(BillingNoteRequest request) {
        boolean emptyContent = request.getContent() == null || request.getContent().isBlank();
        boolean emptyTitle = request.getTitle() == null || request.getTitle().isBlank();
        if (emptyContent && emptyTitle) {
            throw new BusinessException("Informe um título ou um conteúdo para a anotação");
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

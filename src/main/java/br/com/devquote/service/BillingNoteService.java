package br.com.devquote.service;
import br.com.devquote.dto.request.BillingNoteRequest;
import br.com.devquote.dto.response.BillingNoteResponse;
import java.util.List;
import java.util.Map;

public interface BillingNoteService {

    List<BillingNoteResponse> getGeneralNotes();

    List<BillingNoteResponse> getNotesByBillingPeriod(Long billingPeriodId);

    BillingNoteResponse getById(Long id);

    BillingNoteResponse create(BillingNoteRequest request);

    BillingNoteResponse update(Long id, BillingNoteRequest request);

    void delete(Long id);

    /** Contagem de anotações: chave "general" para as gerais e o id do período para as demais. */
    Map<String, Long> countNotes();

    /** Remove todas as anotações (e seus anexos) de um período — usado ao excluir o período. */
    void deleteAllByBillingPeriod(Long billingPeriodId);
}

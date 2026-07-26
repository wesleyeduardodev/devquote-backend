package br.com.devquote.adapter;
import br.com.devquote.dto.response.BillingNoteResponse;
import br.com.devquote.entity.BillingNote;
import br.com.devquote.entity.User;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
public class BillingNoteAdapter {

    public BillingNoteResponse toResponse(BillingNote entity, Integer attachmentCount) {
        if (entity == null) {
            return null;
        }

        User author = entity.getCreatedBy();

        return BillingNoteResponse.builder()
                .id(entity.getId())
                .billingPeriodId(entity.getBillingPeriod() != null ? entity.getBillingPeriod().getId() : null)
                .billingPeriodMonth(entity.getBillingPeriod() != null ? entity.getBillingPeriod().getMonth() : null)
                .billingPeriodYear(entity.getBillingPeriod() != null ? entity.getBillingPeriod().getYear() : null)
                .title(entity.getTitle())
                .content(entity.getContent())
                .createdByUserId(author != null ? author.getId() : null)
                .createdByName(resolveAuthorName(author))
                .attachmentCount(attachmentCount != null ? attachmentCount : 0)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public BillingNoteResponse toResponse(BillingNote entity) {
        return toResponse(entity, 0);
    }

    public List<BillingNoteResponse> toResponseList(List<BillingNote> entities, Map<Long, Integer> attachmentCounts) {
        return entities.stream()
                .map(entity -> toResponse(entity, attachmentCounts.getOrDefault(entity.getId(), 0)))
                .toList();
    }

    private String resolveAuthorName(User author) {
        if (author == null) {
            return null;
        }
        return author.getName() != null && !author.getName().isBlank()
                ? author.getName()
                : author.getUsername();
    }
}

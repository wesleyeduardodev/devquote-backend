package br.com.devquote.dto.response;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class BillingNoteResponse {

    private Long id;
    private Long billingPeriodId;
    private Integer billingPeriodMonth;
    private Integer billingPeriodYear;
    private String title;
    private String content;
    private Long createdByUserId;
    private String createdByName;
    private Integer attachmentCount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}

package br.com.devquote.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskBillingLookupResponse {
    private Long taskId;
    private String taskCode;
    private String taskTitle;
    private Long billingPeriodId;
    private Integer month;
    private Integer year;
    private String status;
    private LocalDate paymentDate;
}

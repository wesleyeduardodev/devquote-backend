package br.com.devquote.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubTaskReportRow {

    private Long id;

    private String title;

    private String description;

    private BigDecimal amount;

    private String amountFormatted;

    private Integer order;
}

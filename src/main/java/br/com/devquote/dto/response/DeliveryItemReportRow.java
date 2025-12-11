package br.com.devquote.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryItemReportRow {

    private Long id;

    private Integer order;

    private String itemType;

    private String projectName;

    private String title;

    private String description;

    private String status;

    private String statusLabel;

    private String branch;

    private String sourceBranch;

    private String pullRequest;

    private String notes;

    private String startedAtFormatted;

    private String finishedAtFormatted;
}

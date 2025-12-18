package br.com.devquote.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    private List<ContentBlock> descriptionBlocks;

    private boolean hasDescriptionContent;

    private String status;

    private String statusLabel;

    private String branch;

    private String sourceBranch;

    private String pullRequest;

    private String notes;

    private List<ContentBlock> notesBlocks;

    private boolean hasNotesContent;

    private String startedAtFormatted;

    private String finishedAtFormatted;
}

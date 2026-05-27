package br.com.devquote.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resposta do endpoint que sincroniza os PRs de uma delivery pro ClickUp.
 * Informa exatamente o que mudou pra UI dar feedback adequado.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncPullRequestsResponse {
    /** True se o campo Branch foi escrito na task do ClickUp. False = no-op (já estava igual ou não configurado). */
    private boolean branchUpdated;

    /** True se um comentário com a lista de PRs foi criado ou atualizado. False = no-op (já estava igual). */
    private boolean commentUpdated;

    /** Quantidade de items da delivery com PR preenchido (pode ser 0 — caso de "remoção"). */
    private int pullRequestCount;

    /** Mensagem amigável pra UI mostrar em toast. */
    private String message;
}

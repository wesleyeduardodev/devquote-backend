package br.com.devquote.service;

import br.com.devquote.dto.request.BoardPreferencesRequest;
import br.com.devquote.dto.response.PriorityBoardResponse;
import br.com.devquote.enums.BoardFilterMode;

public interface PriorityBoardService {

    PriorityBoardResponse getBoard(BoardFilterMode mode);

    /** Compat: equivalente a getBoard(BoardFilterMode.DEV_OR_ASSIGNEE). */
    default PriorityBoardResponse getBoard() {
        return getBoard(BoardFilterMode.DEV_OR_ASSIGNEE);
    }

    /**
     * Atualiza as preferências do board (ordem, status principal, status ocultos).
     * Atualiza apenas os campos não-null do request — atualização parcial.
     */
    void updatePreferences(BoardPreferencesRequest request);

    void evict();
}

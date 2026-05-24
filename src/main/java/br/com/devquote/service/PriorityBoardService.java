package br.com.devquote.service;

import br.com.devquote.dto.request.BoardPreferencesRequest;
import br.com.devquote.dto.response.PriorityBoardResponse;

public interface PriorityBoardService {

    PriorityBoardResponse getBoard(boolean includeAssignee);

    /** Compat: equivalente a getBoard(false). */
    default PriorityBoardResponse getBoard() {
        return getBoard(false);
    }

    /**
     * Atualiza as preferências do board (ordem, status principal, status ocultos).
     * Atualiza apenas os campos não-null do request — atualização parcial.
     */
    void updatePreferences(BoardPreferencesRequest request);

    void evict();
}

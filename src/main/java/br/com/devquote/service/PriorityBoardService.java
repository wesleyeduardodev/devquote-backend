package br.com.devquote.service;

import br.com.devquote.dto.response.PriorityBoardResponse;

public interface PriorityBoardService {

    PriorityBoardResponse getBoard(boolean includeAssignee);

    /** Compat: equivalente a getBoard(false). */
    default PriorityBoardResponse getBoard() {
        return getBoard(false);
    }

    void evict();
}

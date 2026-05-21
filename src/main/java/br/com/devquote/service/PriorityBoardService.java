package br.com.devquote.service;

import br.com.devquote.dto.response.PriorityBoardResponse;

public interface PriorityBoardService {

    PriorityBoardResponse getBoard();

    void evict();
}

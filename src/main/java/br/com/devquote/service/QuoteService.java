package br.com.devquote.service;
import br.com.devquote.dto.request.QuoteRequestDTO;
import br.com.devquote.dto.response.QuoteResponseDTO;
import java.util.List;

public interface QuoteService {
    List<QuoteResponseDTO> findAll();
    QuoteResponseDTO findById(Long id);
    QuoteResponseDTO create(QuoteRequestDTO dto);
    QuoteResponseDTO update(Long id, QuoteRequestDTO dto);
    void delete(Long id);
}
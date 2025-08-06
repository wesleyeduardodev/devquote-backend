package br.com.devquote.service;
import br.com.devquote.dto.request.RequesterRequestDTO;
import br.com.devquote.dto.response.RequesterResponseDTO;
import java.util.List;

public interface RequesterService {

    List<RequesterResponseDTO> findAll();

    RequesterResponseDTO findById(Long id);

    RequesterResponseDTO create(RequesterRequestDTO dto);

    RequesterResponseDTO update(Long id, RequesterRequestDTO dto);

    void delete(Long id);
}
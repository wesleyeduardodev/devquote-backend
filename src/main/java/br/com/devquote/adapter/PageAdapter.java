package br.com.devquote.adapter;
import br.com.devquote.dto.response.PagedResponseDTO;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;

@UtilityClass
public class PageAdapter {

    public static <T> PagedResponseDTO<T> toPagedResponseDTO(Page<T> page) {
        return new PagedResponseDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getTotalPages(),
                page.getSize(),
                page.getTotalElements(),
                page.isFirst(),
                page.isLast()
        );
    }
}
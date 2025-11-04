package br.com.devquote.adapter;
import br.com.devquote.dto.response.PagedResponse;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;

@UtilityClass
public class PageAdapter {

    public static <T> PagedResponse<T> toPagedResponseDTO(Page<T> page) {
        return new PagedResponse<>(
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
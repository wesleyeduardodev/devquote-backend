package br.com.devquote.dto.request;

import br.com.devquote.enums.Environment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationalReportRequest {

    private LocalDateTime dataInicio;

    private LocalDateTime dataFim;

    private String tipoTarefa;

    private Environment ambiente;
}

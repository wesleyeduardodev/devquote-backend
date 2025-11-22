package br.com.devquote.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationalReportStatistics {

    private Long totalGeral;

    private String ambienteMaisFrequente;

    private Long volumeAmbienteMaisFrequente;

    private Double percentualAmbienteMaisFrequente;

    private String tipoMaisFrequente;

    private Long volumeTipoMaisFrequente;

    private Double percentualTipoMaisFrequente;

    private Double mediaDiaria;
}

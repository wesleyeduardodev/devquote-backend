package br.com.devquote.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationalReportRow {

    private String tipoTarefa;

    private Long quantidadeProducao;

    private Long quantidadeHomologacao;

    private Long quantidadeDesenvolvimento;

    private Long quantidadeNaoEspecificado;

    private Long total;
}

package br.com.devquote.service.impl;

import br.com.devquote.dto.request.OperationalReportRequest;
import br.com.devquote.dto.response.OperationalReportData;
import br.com.devquote.dto.response.OperationalReportRow;
import br.com.devquote.dto.response.OperationalReportStatistics;
import br.com.devquote.enums.Environment;
import br.com.devquote.repository.DeliveryRepository;
import br.com.devquote.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final DeliveryRepository deliveryRepository;

    @Override
    public byte[] generateOperationalReportPdf(OperationalReportRequest request) {
        try {
            log.info("Gerando relatório operacional - Período: {} a {}", request.getDataInicio(), request.getDataFim());

            OperationalReportData reportData = buildReportData(request);

            JasperReport jasperReport = loadJasperReport();

            Map<String, Object> parameters = buildReportParameters(reportData);

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reportData.getLinhas());

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);

            log.info("Relatório operacional gerado com sucesso - {} linhas", reportData.getLinhas().size());

            return pdfBytes;

        } catch (Exception e) {
            log.error("Erro ao gerar relatório operacional", e);
            throw new RuntimeException("Erro ao gerar relatório: " + e.getMessage(), e);
        }
    }

    private OperationalReportData buildReportData(OperationalReportRequest request) {
        String ambiente = request.getAmbiente() != null ? request.getAmbiente().name() : null;

        LocalDateTime dataInicio = request.getDataInicio();
        LocalDateTime dataFim = request.getDataFim();

        if (dataInicio == null || dataFim == null) {
            try {
                List<Object[]> dateRangeList = deliveryRepository.findOperationalDateRange();

                if (dateRangeList != null && !dateRangeList.isEmpty()) {
                    Object[] dateRange = dateRangeList.get(0);

                    if (dateRange != null && dateRange.length >= 2 && dateRange[0] != null && dateRange[1] != null) {
                        LocalDateTime minDate = ((java.sql.Timestamp) dateRange[0]).toLocalDateTime();
                        LocalDateTime maxDate = ((java.sql.Timestamp) dateRange[1]).toLocalDateTime();

                        if (dataInicio == null) {
                            dataInicio = minDate;
                        }
                        if (dataFim == null) {
                            dataFim = maxDate;
                        }
                    } else {
                        log.warn("Range de datas retornou valores nulos");
                        if (dataInicio == null) {
                            dataInicio = LocalDateTime.now().minusMonths(1);
                        }
                        if (dataFim == null) {
                            dataFim = LocalDateTime.now();
                        }
                    }
                } else {
                    log.warn("Nenhum registro encontrado para determinar o range de datas");
                    if (dataInicio == null) {
                        dataInicio = LocalDateTime.now().minusMonths(1);
                    }
                    if (dataFim == null) {
                        dataFim = LocalDateTime.now();
                    }
                }
            } catch (Exception e) {
                log.error("Erro ao buscar range de datas: {}", e.getMessage(), e);
                if (dataInicio == null) {
                    dataInicio = LocalDateTime.now().minusMonths(1);
                }
                if (dataFim == null) {
                    dataFim = LocalDateTime.now();
                }
            }
        }

        List<Object[]> queryResults = deliveryRepository.findOperationalReportData(
                dataInicio,
                dataFim,
                request.getTipoTarefa(),
                ambiente
        );

        Map<String, OperationalReportRow> rowMap = new LinkedHashMap<>();

        Map<String, String> taskTypeTranslations = new LinkedHashMap<>();
        taskTypeTranslations.put("BACKUP", "Backup");
        taskTypeTranslations.put("DEPLOY", "Deploy");
        taskTypeTranslations.put("LOGS", "Logs");
        taskTypeTranslations.put("DATABASE_APPLICATION", "Aplicação de Banco");
        taskTypeTranslations.put("NEW_SERVER", "Novo Servidor");
        taskTypeTranslations.put("MONITORING", "Monitoramento");
        taskTypeTranslations.put("SUPPORT", "Suporte");

        for (Map.Entry<String, String> entry : taskTypeTranslations.entrySet()) {
            rowMap.put(entry.getKey(), OperationalReportRow.builder()
                    .tipoTarefa(entry.getValue())
                    .quantidadeProducao(0L)
                    .quantidadeHomologacao(0L)
                    .quantidadeDesenvolvimento(0L)
                    .quantidadeNaoEspecificado(0L)
                    .total(0L)
                    .build());
        }

        for (Object[] row : queryResults) {
            String tipoTarefaRaw = (String) row[0];
            String ambienteNome = (String) row[1];
            Long quantidade = ((Number) row[2]).longValue();

            if (tipoTarefaRaw == null || tipoTarefaRaw.trim().isEmpty()) {
                continue;
            }

            OperationalReportRow reportRow = rowMap.get(tipoTarefaRaw);
            if (reportRow == null) {
                String translatedName = taskTypeTranslations.getOrDefault(tipoTarefaRaw, tipoTarefaRaw);
                reportRow = OperationalReportRow.builder()
                        .tipoTarefa(translatedName)
                        .quantidadeProducao(0L)
                        .quantidadeHomologacao(0L)
                        .quantidadeDesenvolvimento(0L)
                        .quantidadeNaoEspecificado(0L)
                        .total(0L)
                        .build();
                rowMap.put(tipoTarefaRaw, reportRow);
            }

            if (ambienteNome != null) {
                switch (Environment.valueOf(ambienteNome)) {
                    case PRODUCAO:
                        reportRow.setQuantidadeProducao(quantidade);
                        break;
                    case HOMOLOGACAO:
                        reportRow.setQuantidadeHomologacao(quantidade);
                        break;
                    case DESENVOLVIMENTO:
                        reportRow.setQuantidadeDesenvolvimento(quantidade);
                        break;
                }
            } else {
                reportRow.setQuantidadeNaoEspecificado(quantidade);
            }

            reportRow.setTotal(
                    reportRow.getQuantidadeProducao() +
                            reportRow.getQuantidadeHomologacao() +
                            reportRow.getQuantidadeDesenvolvimento() +
                            reportRow.getQuantidadeNaoEspecificado()
            );
        }

        List<OperationalReportRow> linhas = new ArrayList<>(rowMap.values());

        long totalProducao = linhas.stream()
                .mapToLong(OperationalReportRow::getQuantidadeProducao)
                .sum();

        long totalHomologacao = linhas.stream()
                .mapToLong(OperationalReportRow::getQuantidadeHomologacao)
                .sum();

        long totalDesenvolvimento = linhas.stream()
                .mapToLong(OperationalReportRow::getQuantidadeDesenvolvimento)
                .sum();

        long totalNaoEspecificado = linhas.stream()
                .mapToLong(OperationalReportRow::getQuantidadeNaoEspecificado)
                .sum();

        long totalGeral = totalProducao + totalHomologacao + totalDesenvolvimento + totalNaoEspecificado;

        OperationalReportStatistics statistics = calculateStatistics(
                linhas,
                totalProducao,
                totalHomologacao,
                totalDesenvolvimento,
                totalGeral,
                dataInicio,
                dataFim
        );

        String filtrosTipos = request.getTipoTarefa() != null && !request.getTipoTarefa().trim().isEmpty()
                ? request.getTipoTarefa()
                : "Todos";

        String filtrosAmbientes = "Todos";
        if (request.getAmbiente() != null) {
            filtrosAmbientes = request.getAmbiente().getDisplayName();
        }

        return OperationalReportData.builder()
                .dataInicio(dataInicio)
                .dataFim(dataFim)
                .dataGeracao(LocalDateTime.now())
                .filtrosTipos(filtrosTipos)
                .filtrosAmbientes(filtrosAmbientes)
                .logoPath("reports/images/logo-devquote.png")
                .linhas(linhas)
                .totalProducao(totalProducao)
                .totalHomologacao(totalHomologacao)
                .totalDesenvolvimento(totalDesenvolvimento)
                .totalNaoEspecificado(totalNaoEspecificado)
                .totalGeral(totalGeral)
                .statistics(statistics)
                .build();
    }

    private OperationalReportStatistics calculateStatistics(
            List<OperationalReportRow> linhas,
            long totalProducao,
            long totalHomologacao,
            long totalDesenvolvimento,
            long totalGeral,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    ) {
        String ambienteMaisFrequente;
        long volumeAmbienteMaisFrequente;
        double percentualAmbienteMaisFrequente;

        if (totalProducao >= totalHomologacao && totalProducao >= totalDesenvolvimento) {
            ambienteMaisFrequente = "Produção";
            volumeAmbienteMaisFrequente = totalProducao;
        } else if (totalHomologacao >= totalDesenvolvimento) {
            ambienteMaisFrequente = "Homologação";
            volumeAmbienteMaisFrequente = totalHomologacao;
        } else {
            ambienteMaisFrequente = "Desenvolvimento";
            volumeAmbienteMaisFrequente = totalDesenvolvimento;
        }

        percentualAmbienteMaisFrequente = totalGeral > 0
                ? (volumeAmbienteMaisFrequente * 100.0) / totalGeral
                : 0.0;

        String tipoMaisFrequente = "";
        long volumeTipoMaisFrequente = 0L;
        double percentualTipoMaisFrequente = 0.0;

        if (!linhas.isEmpty()) {
            OperationalReportRow maxRow = linhas.stream()
                    .max(Comparator.comparing(OperationalReportRow::getTotal))
                    .orElse(null);

            if (maxRow != null) {
                tipoMaisFrequente = maxRow.getTipoTarefa();
                volumeTipoMaisFrequente = maxRow.getTotal();
                percentualTipoMaisFrequente = totalGeral > 0
                        ? (volumeTipoMaisFrequente * 100.0) / totalGeral
                        : 0.0;
            }
        }

        long diasNoPeriodo = ChronoUnit.DAYS.between(dataInicio, dataFim) + 1;
        double mediaDiaria = diasNoPeriodo > 0 ? (double) totalGeral / diasNoPeriodo : 0.0;

        return OperationalReportStatistics.builder()
                .totalGeral(totalGeral)
                .ambienteMaisFrequente(ambienteMaisFrequente)
                .volumeAmbienteMaisFrequente(volumeAmbienteMaisFrequente)
                .percentualAmbienteMaisFrequente(percentualAmbienteMaisFrequente)
                .tipoMaisFrequente(tipoMaisFrequente)
                .volumeTipoMaisFrequente(volumeTipoMaisFrequente)
                .percentualTipoMaisFrequente(percentualTipoMaisFrequente)
                .mediaDiaria(mediaDiaria)
                .build();
    }

    private JasperReport loadJasperReport() throws JRException {
        try {
            ClassPathResource resource = new ClassPathResource("reports/operational_report.jasper");
            return (JasperReport) JRLoader.loadObject(resource.getInputStream());
        } catch (Exception e) {
            log.error("Erro ao carregar template Jasper compilado, tentando carregar .jrxml", e);
            try {
                ClassPathResource resourceJrxml = new ClassPathResource("reports/operational_report.jrxml");
                return JasperCompileManager.compileReport(resourceJrxml.getInputStream());
            } catch (Exception ex) {
                log.error("Erro ao compilar template Jasper", ex);
                throw new RuntimeException("Não foi possível carregar o template do relatório", ex);
            }
        }
    }

    private Map<String, Object> buildReportParameters(OperationalReportData data) {
        Map<String, Object> parameters = new HashMap<>();

        parameters.put("dataInicio", data.getDataInicio());
        parameters.put("dataFim", data.getDataFim());
        parameters.put("dataGeracao", data.getDataGeracao());
        parameters.put("filtrosTipos", data.getFiltrosTipos());
        parameters.put("filtrosAmbientes", data.getFiltrosAmbientes());
        parameters.put("totalProducao", data.getTotalProducao());
        parameters.put("totalHomologacao", data.getTotalHomologacao());
        parameters.put("totalDesenvolvimento", data.getTotalDesenvolvimento());
        parameters.put("totalNaoEspecificado", data.getTotalNaoEspecificado());
        parameters.put("totalGeral", data.getTotalGeral());

        if (data.getStatistics() != null) {
            OperationalReportStatistics stats = data.getStatistics();
            parameters.put("totalGeralStats", stats.getTotalGeral());
            parameters.put("ambienteMaisFrequente", stats.getAmbienteMaisFrequente());
            parameters.put("volumeAmbienteMaisFrequente", stats.getVolumeAmbienteMaisFrequente());
            parameters.put("percentualAmbienteMaisFrequente", String.format("%.2f%%", stats.getPercentualAmbienteMaisFrequente()));
            parameters.put("tipoMaisFrequente", stats.getTipoMaisFrequente());
            parameters.put("volumeTipoMaisFrequente", stats.getVolumeTipoMaisFrequente());
            parameters.put("percentualTipoMaisFrequente", String.format("%.2f%%", stats.getPercentualTipoMaisFrequente()));
            parameters.put("mediaDiaria", String.format("%.2f", stats.getMediaDiaria()));
        }

        parameters.put("desenvolvedorNome", data.getDesenvolvedorNome());
        parameters.put("desenvolvedorTitulo", data.getDesenvolvedorTitulo());
        parameters.put("desenvolvedorEmail", data.getDesenvolvedorEmail());
        parameters.put("desenvolvedorTelefone", data.getDesenvolvedorTelefone());
        parameters.put("copyright", data.getCopyright());
        parameters.put("sistemaTagline", data.getSistemaTagline());
        parameters.put("linkedinUrl", data.getLinkedinUrl());
        parameters.put("githubUrl", data.getGithubUrl());
        parameters.put("instagramUrl", data.getInstagramUrl());
        parameters.put("facebookUrl", data.getFacebookUrl());

        try {
            ClassPathResource logoResource = new ClassPathResource(data.getLogoPath());
            parameters.put("logoPath", logoResource.getURL().toString());
        } catch (Exception e) {
            log.warn("Logo não encontrado: {}", data.getLogoPath(), e);
            parameters.put("logoPath", "");
        }

        return parameters;
    }
}

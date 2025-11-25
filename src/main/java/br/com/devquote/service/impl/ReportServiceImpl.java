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

        LocalDateTime dataInicio = request.getDataInicio();
        LocalDateTime dataFim = request.getDataFim();
        boolean filtroInformado = dataInicio != null && dataFim != null;

        if (dataFim != null) {
            dataFim = dataFim.withHour(23).withMinute(59).withSecond(59);
        }

        LocalDateTime dataInicioOperacional = dataInicio;
        LocalDateTime dataFimOperacional = dataFim;
        LocalDateTime dataInicioDesenvolvimento = dataInicio;
        LocalDateTime dataFimDesenvolvimento = dataFim;

        if (!filtroInformado) {
            try {
                List<Object[]> operationalDateRangeList = deliveryRepository.findOperationalDateRange();
                List<Object[]> developmentDateRangeList = deliveryRepository.findDevelopmentDateRange();

                if (operationalDateRangeList != null && !operationalDateRangeList.isEmpty()) {
                    Object[] operationalRange = operationalDateRangeList.get(0);
                    if (operationalRange != null && operationalRange.length >= 2 && operationalRange[0] != null && operationalRange[1] != null) {
                        dataInicioOperacional = ((java.sql.Timestamp) operationalRange[0]).toLocalDateTime();
                        dataFimOperacional = ((java.sql.Timestamp) operationalRange[1]).toLocalDateTime();
                    }
                }

                if (developmentDateRangeList != null && !developmentDateRangeList.isEmpty()) {
                    Object[] developmentRange = developmentDateRangeList.get(0);
                    if (developmentRange != null && developmentRange.length >= 2 && developmentRange[0] != null && developmentRange[1] != null) {
                        dataInicioDesenvolvimento = ((java.sql.Timestamp) developmentRange[0]).toLocalDateTime();
                        dataFimDesenvolvimento = ((java.sql.Timestamp) developmentRange[1]).toLocalDateTime();
                    }
                }

                if (dataInicioOperacional == null || dataFimOperacional == null) {
                    log.warn("Range de datas operacional retornou valores nulos");
                    dataInicioOperacional = LocalDateTime.now().minusMonths(1);
                    dataFimOperacional = LocalDateTime.now();
                }

                if (dataInicioDesenvolvimento == null || dataFimDesenvolvimento == null) {
                    log.warn("Range de datas desenvolvimento retornou valores nulos");
                    dataInicioDesenvolvimento = LocalDateTime.now().minusMonths(1);
                    dataFimDesenvolvimento = LocalDateTime.now();
                }

                dataInicio = dataInicioOperacional.isBefore(dataInicioDesenvolvimento) ? dataInicioOperacional : dataInicioDesenvolvimento;
                dataFim = dataFimOperacional.isAfter(dataFimDesenvolvimento) ? dataFimOperacional : dataFimDesenvolvimento;

            } catch (Exception e) {
                log.error("Erro ao buscar range de datas: {}", e.getMessage(), e);
                dataInicioOperacional = LocalDateTime.now().minusMonths(1);
                dataFimOperacional = LocalDateTime.now();
                dataInicioDesenvolvimento = LocalDateTime.now().minusMonths(1);
                dataFimDesenvolvimento = LocalDateTime.now();
                dataInicio = dataInicioOperacional;
                dataFim = dataFimOperacional;
            }
        } else {
            dataInicioOperacional = dataInicio;
            dataFimOperacional = dataFim;
            dataInicioDesenvolvimento = dataInicio;
            dataFimDesenvolvimento = dataFim;
        }

        List<Object[]> queryResults = deliveryRepository.findOperationalReportData(
                dataInicioOperacional,
                dataFimOperacional
        );

        List<Object[]> financialResults = deliveryRepository.findOperationalReportFinancialData(
                dataInicioOperacional,
                dataFimOperacional

        );

        List<Object[]> developmentQueryResults = deliveryRepository.findDevelopmentReportData(
                dataInicioDesenvolvimento,
                dataFimDesenvolvimento
        );

        List<Object[]> developmentFinancialResults = deliveryRepository.findDevelopmentReportFinancialData(
                dataInicioDesenvolvimento,
                dataFimDesenvolvimento
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

        Map<String, OperationalReportRow> developmentRowMap = new LinkedHashMap<>();

        Map<String, String> developmentTaskTypeTranslations = new LinkedHashMap<>();
        developmentTaskTypeTranslations.put("BUG", "Bug");
        developmentTaskTypeTranslations.put("ENHANCEMENT", "Melhoria");
        developmentTaskTypeTranslations.put("NEW_FEATURE", "Nova Funcionalidade");

        for (Map.Entry<String, String> entry : taskTypeTranslations.entrySet()) {
            rowMap.put(entry.getKey(), OperationalReportRow.builder()
                    .tipoTarefa(entry.getValue())
                    .quantidadeProducao(0L)
                    .quantidadeHomologacao(0L)
                    .quantidadeDesenvolvimento(0L)
                    .quantidadeNaoEspecificado(0L)
                    .total(0L)
                    .valorProducao(java.math.BigDecimal.ZERO)
                    .valorHomologacao(java.math.BigDecimal.ZERO)
                    .valorDesenvolvimento(java.math.BigDecimal.ZERO)
                    .valorNaoEspecificado(java.math.BigDecimal.ZERO)
                    .valorTotal(java.math.BigDecimal.ZERO)
                    .build());
        }

        for (Map.Entry<String, String> entry : developmentTaskTypeTranslations.entrySet()) {
            developmentRowMap.put(entry.getKey(), OperationalReportRow.builder()
                    .tipoTarefa(entry.getValue())
                    .quantidadeProducao(0L)
                    .quantidadeHomologacao(0L)
                    .quantidadeDesenvolvimento(0L)
                    .quantidadeNaoEspecificado(0L)
                    .total(0L)
                    .valorProducao(java.math.BigDecimal.ZERO)
                    .valorHomologacao(java.math.BigDecimal.ZERO)
                    .valorDesenvolvimento(java.math.BigDecimal.ZERO)
                    .valorNaoEspecificado(java.math.BigDecimal.ZERO)
                    .valorTotal(java.math.BigDecimal.ZERO)
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

        for (Object[] row : financialResults) {
            String tipoTarefaRaw = (String) row[0];
            String ambienteNome = (String) row[1];
            java.math.BigDecimal valor = row[2] != null ? new java.math.BigDecimal(row[2].toString()) : java.math.BigDecimal.ZERO;

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
                        .valorProducao(java.math.BigDecimal.ZERO)
                        .valorHomologacao(java.math.BigDecimal.ZERO)
                        .valorDesenvolvimento(java.math.BigDecimal.ZERO)
                        .valorNaoEspecificado(java.math.BigDecimal.ZERO)
                        .valorTotal(java.math.BigDecimal.ZERO)
                        .build();
                rowMap.put(tipoTarefaRaw, reportRow);
            }

            if (ambienteNome != null) {
                switch (Environment.valueOf(ambienteNome)) {
                    case PRODUCAO:
                        reportRow.setValorProducao(valor);
                        break;
                    case HOMOLOGACAO:
                        reportRow.setValorHomologacao(valor);
                        break;
                    case DESENVOLVIMENTO:
                        reportRow.setValorDesenvolvimento(valor);
                        break;
                }
            } else {
                reportRow.setValorNaoEspecificado(valor);
            }

            reportRow.setValorTotal(
                    reportRow.getValorProducao()
                            .add(reportRow.getValorHomologacao())
                            .add(reportRow.getValorDesenvolvimento())
                            .add(reportRow.getValorNaoEspecificado())
            );
        }

        for (Object[] row : developmentQueryResults) {
            String tipoTarefaRaw = (String) row[0];
            String ambienteNome = (String) row[1];
            Long quantidade = ((Number) row[2]).longValue();

            if (tipoTarefaRaw == null || tipoTarefaRaw.trim().isEmpty()) {
                continue;
            }

            OperationalReportRow reportRow = developmentRowMap.get(tipoTarefaRaw);
            if (reportRow == null) {
                String translatedName = developmentTaskTypeTranslations.getOrDefault(tipoTarefaRaw, tipoTarefaRaw);
                reportRow = OperationalReportRow.builder()
                        .tipoTarefa(translatedName)
                        .quantidadeProducao(0L)
                        .quantidadeHomologacao(0L)
                        .quantidadeDesenvolvimento(0L)
                        .quantidadeNaoEspecificado(0L)
                        .total(0L)
                        .build();
                developmentRowMap.put(tipoTarefaRaw, reportRow);
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

        for (Object[] row : developmentFinancialResults) {
            String tipoTarefaRaw = (String) row[0];
            String ambienteNome = (String) row[1];
            java.math.BigDecimal valor = row[2] != null ? new java.math.BigDecimal(row[2].toString()) : java.math.BigDecimal.ZERO;

            if (tipoTarefaRaw == null || tipoTarefaRaw.trim().isEmpty()) {
                continue;
            }

            OperationalReportRow reportRow = developmentRowMap.get(tipoTarefaRaw);
            if (reportRow == null) {
                String translatedName = developmentTaskTypeTranslations.getOrDefault(tipoTarefaRaw, tipoTarefaRaw);
                reportRow = OperationalReportRow.builder()
                        .tipoTarefa(translatedName)
                        .quantidadeProducao(0L)
                        .quantidadeHomologacao(0L)
                        .quantidadeDesenvolvimento(0L)
                        .quantidadeNaoEspecificado(0L)
                        .total(0L)
                        .valorProducao(java.math.BigDecimal.ZERO)
                        .valorHomologacao(java.math.BigDecimal.ZERO)
                        .valorDesenvolvimento(java.math.BigDecimal.ZERO)
                        .valorNaoEspecificado(java.math.BigDecimal.ZERO)
                        .valorTotal(java.math.BigDecimal.ZERO)
                        .build();
                developmentRowMap.put(tipoTarefaRaw, reportRow);
            }

            if (ambienteNome != null) {
                switch (Environment.valueOf(ambienteNome)) {
                    case PRODUCAO:
                        reportRow.setValorProducao(valor);
                        break;
                    case HOMOLOGACAO:
                        reportRow.setValorHomologacao(valor);
                        break;
                    case DESENVOLVIMENTO:
                        reportRow.setValorDesenvolvimento(valor);
                        break;
                }
            } else {
                reportRow.setValorNaoEspecificado(valor);
            }

            reportRow.setValorTotal(
                    reportRow.getValorProducao()
                            .add(reportRow.getValorHomologacao())
                            .add(reportRow.getValorDesenvolvimento())
                            .add(reportRow.getValorNaoEspecificado())
            );
        }

        List<OperationalReportRow> linhas = new ArrayList<>(rowMap.values());
        List<OperationalReportRow> linhasDesenvolvimento = new ArrayList<>(developmentRowMap.values());

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

        java.math.BigDecimal totalValorProducao = linhas.stream()
                .map(OperationalReportRow::getValorProducao)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        java.math.BigDecimal totalValorHomologacao = linhas.stream()
                .map(OperationalReportRow::getValorHomologacao)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        java.math.BigDecimal totalValorDesenvolvimento = linhas.stream()
                .map(OperationalReportRow::getValorDesenvolvimento)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        java.math.BigDecimal totalValorNaoEspecificado = linhas.stream()
                .map(OperationalReportRow::getValorNaoEspecificado)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        java.math.BigDecimal totalValorGeral = totalValorProducao
                .add(totalValorHomologacao)
                .add(totalValorDesenvolvimento)
                .add(totalValorNaoEspecificado);

        long totalProducaoDesenv = linhasDesenvolvimento.stream()
                .mapToLong(OperationalReportRow::getQuantidadeProducao)
                .sum();

        long totalHomologacaoDesenv = linhasDesenvolvimento.stream()
                .mapToLong(OperationalReportRow::getQuantidadeHomologacao)
                .sum();

        long totalDesenvolvimentoDesenv = linhasDesenvolvimento.stream()
                .mapToLong(OperationalReportRow::getQuantidadeDesenvolvimento)
                .sum();

        long totalNaoEspecificadoDesenv = linhasDesenvolvimento.stream()
                .mapToLong(OperationalReportRow::getQuantidadeNaoEspecificado)
                .sum();

        long totalGeralDesenv = totalProducaoDesenv + totalHomologacaoDesenv + totalDesenvolvimentoDesenv + totalNaoEspecificadoDesenv;

        java.math.BigDecimal totalValorProducaoDesenv = linhasDesenvolvimento.stream()
                .map(OperationalReportRow::getValorProducao)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        java.math.BigDecimal totalValorHomologacaoDesenv = linhasDesenvolvimento.stream()
                .map(OperationalReportRow::getValorHomologacao)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        java.math.BigDecimal totalValorDesenvolvimentoDesenv = linhasDesenvolvimento.stream()
                .map(OperationalReportRow::getValorDesenvolvimento)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        java.math.BigDecimal totalValorNaoEspecificadoDesenv = linhasDesenvolvimento.stream()
                .map(OperationalReportRow::getValorNaoEspecificado)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        java.math.BigDecimal totalValorGeralDesenv = totalValorProducaoDesenv
                .add(totalValorHomologacaoDesenv)
                .add(totalValorDesenvolvimentoDesenv)
                .add(totalValorNaoEspecificadoDesenv);

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
                .dataInicioOperacional(dataInicioOperacional)
                .dataFimOperacional(dataFimOperacional)
                .dataInicioDesenvolvimento(dataInicioDesenvolvimento)
                .dataFimDesenvolvimento(dataFimDesenvolvimento)
                .dataGeracao(LocalDateTime.now())
                .filtrosTipos(filtrosTipos)
                .filtrosAmbientes(filtrosAmbientes)
                //.logoPath("reports/images/logo-devquote.png")
                .linhas(linhas)
                .totalProducao(totalProducao)
                .totalHomologacao(totalHomologacao)
                .totalDesenvolvimento(totalDesenvolvimento)
                .totalNaoEspecificado(totalNaoEspecificado)
                .totalGeral(totalGeral)
                .totalValorProducao(totalValorProducao)
                .totalValorHomologacao(totalValorHomologacao)
                .totalValorDesenvolvimento(totalValorDesenvolvimento)
                .totalValorNaoEspecificado(totalValorNaoEspecificado)
                .totalValorGeral(totalValorGeral)
                .linhasDesenvolvimento(linhasDesenvolvimento)
                .totalProducaoDesenv(totalProducaoDesenv)
                .totalHomologacaoDesenv(totalHomologacaoDesenv)
                .totalDesenvolvimentoDesenv(totalDesenvolvimentoDesenv)
                .totalNaoEspecificadoDesenv(totalNaoEspecificadoDesenv)
                .totalGeralDesenv(totalGeralDesenv)
                .totalValorProducaoDesenv(totalValorProducaoDesenv)
                .totalValorHomologacaoDesenv(totalValorHomologacaoDesenv)
                .totalValorDesenvolvimentoDesenv(totalValorDesenvolvimentoDesenv)
                .totalValorNaoEspecificadoDesenv(totalValorNaoEspecificadoDesenv)
                .totalValorGeralDesenv(totalValorGeralDesenv)
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
        parameters.put("dataInicioOperacional", data.getDataInicioOperacional());
        parameters.put("dataFimOperacional", data.getDataFimOperacional());
        parameters.put("dataInicioDesenvolvimento", data.getDataInicioDesenvolvimento());
        parameters.put("dataFimDesenvolvimento", data.getDataFimDesenvolvimento());
        parameters.put("dataGeracao", data.getDataGeracao());
        parameters.put("filtrosTipos", data.getFiltrosTipos());
        parameters.put("filtrosAmbientes", data.getFiltrosAmbientes());
        parameters.put("totalProducao", data.getTotalProducao());
        parameters.put("totalHomologacao", data.getTotalHomologacao());
        parameters.put("totalDesenvolvimento", data.getTotalDesenvolvimento());
        parameters.put("totalNaoEspecificado", data.getTotalNaoEspecificado());
        parameters.put("totalGeral", data.getTotalGeral());
        parameters.put("totalValorProducao", data.getTotalValorProducao());
        parameters.put("totalValorHomologacao", data.getTotalValorHomologacao());
        parameters.put("totalValorDesenvolvimento", data.getTotalValorDesenvolvimento());
        parameters.put("totalValorNaoEspecificado", data.getTotalValorNaoEspecificado());
        parameters.put("totalValorGeral", data.getTotalValorGeral());
        parameters.put("linhas", data.getLinhas());
        parameters.put("linhasDesenvolvimento", data.getLinhasDesenvolvimento());
        parameters.put("totalProducaoDesenv", data.getTotalProducaoDesenv());
        parameters.put("totalHomologacaoDesenv", data.getTotalHomologacaoDesenv());
        parameters.put("totalDesenvolvimentoDesenv", data.getTotalDesenvolvimentoDesenv());
        parameters.put("totalNaoEspecificadoDesenv", data.getTotalNaoEspecificadoDesenv());
        parameters.put("totalGeralDesenv", data.getTotalGeralDesenv());
        parameters.put("totalValorProducaoDesenv", data.getTotalValorProducaoDesenv());
        parameters.put("totalValorHomologacaoDesenv", data.getTotalValorHomologacaoDesenv());
        parameters.put("totalValorDesenvolvimentoDesenv", data.getTotalValorDesenvolvimentoDesenv());
        parameters.put("totalValorNaoEspecificadoDesenv", data.getTotalValorNaoEspecificadoDesenv());
        parameters.put("totalValorGeralDesenv", data.getTotalValorGeralDesenv());

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

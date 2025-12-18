package br.com.devquote.service.impl;

import br.com.devquote.dto.request.OperationalReportRequest;
import br.com.devquote.dto.response.DeliveryItemReportRow;
import br.com.devquote.dto.response.DeliveryReportData;
import br.com.devquote.dto.response.OperationalReportData;
import br.com.devquote.dto.response.OperationalReportRow;
import br.com.devquote.dto.response.OperationalReportStatistics;
import br.com.devquote.dto.response.ContentBlock;
import br.com.devquote.dto.response.SubTaskReportRow;
import br.com.devquote.dto.response.TaskReportData;
import br.com.devquote.utils.HtmlImageExtractor;
import br.com.devquote.entity.Delivery;
import br.com.devquote.entity.DeliveryItem;
import br.com.devquote.entity.DeliveryOperationalItem;
import br.com.devquote.entity.SubTask;
import br.com.devquote.entity.Task;
import br.com.devquote.enums.Environment;
import br.com.devquote.repository.BillingPeriodTaskRepository;
import br.com.devquote.repository.DeliveryRepository;
import br.com.devquote.repository.SubTaskRepository;
import br.com.devquote.repository.TaskRepository;
import br.com.devquote.service.ReportService;
import br.com.devquote.service.storage.FileStorageStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import java.io.ByteArrayOutputStream;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final DeliveryRepository deliveryRepository;
    private final TaskRepository taskRepository;
    private final SubTaskRepository subTaskRepository;
    private final BillingPeriodTaskRepository billingPeriodTaskRepository;
    private final FileStorageStrategy fileStorageStrategy;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

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

        if (dataInicio != null) {
            dataInicio = dataInicio.withHour(0).withMinute(0).withSecond(0);
        }

        if (dataFim != null) {
            dataFim = dataFim.withHour(23).withMinute(59).withSecond(59);
        }

        LocalDateTime dataInicioOperacional;
        LocalDateTime dataFimOperacional;
        LocalDateTime dataInicioDesenvolvimento;
        LocalDateTime dataFimDesenvolvimento;

        if (dataInicio == null && dataFim == null) {
            try {
                List<Object[]> operationalDateRangeList = deliveryRepository.findOperationalDateRange();
                List<Object[]> developmentDateRangeList = deliveryRepository.findDevelopmentDateRange();

                if (operationalDateRangeList != null && !operationalDateRangeList.isEmpty()) {
                    Object[] operationalRange = operationalDateRangeList.get(0);
                    if (operationalRange != null && operationalRange.length >= 2 && operationalRange[0] != null && operationalRange[1] != null) {
                        dataInicioOperacional = ((java.sql.Timestamp) operationalRange[0]).toLocalDateTime();
                        dataFimOperacional = ((java.sql.Timestamp) operationalRange[1]).toLocalDateTime();
                    } else {
                        log.warn("Range de datas operacional retornou valores nulos");
                        dataInicioOperacional = LocalDateTime.now().minusMonths(1);
                        dataFimOperacional = LocalDateTime.now();
                    }
                } else {
                    log.warn("Range de datas operacional vazio");
                    dataInicioOperacional = LocalDateTime.now().minusMonths(1);
                    dataFimOperacional = LocalDateTime.now();
                }

                if (developmentDateRangeList != null && !developmentDateRangeList.isEmpty()) {
                    Object[] developmentRange = developmentDateRangeList.get(0);
                    if (developmentRange != null && developmentRange.length >= 2 && developmentRange[0] != null && developmentRange[1] != null) {
                        dataInicioDesenvolvimento = ((java.sql.Timestamp) developmentRange[0]).toLocalDateTime();
                        dataFimDesenvolvimento = ((java.sql.Timestamp) developmentRange[1]).toLocalDateTime();
                    } else {
                        log.warn("Range de datas desenvolvimento retornou valores nulos");
                        dataInicioDesenvolvimento = LocalDateTime.now().minusMonths(1);
                        dataFimDesenvolvimento = LocalDateTime.now();
                    }
                } else {
                    log.warn("Range de datas desenvolvimento vazio");
                    dataInicioDesenvolvimento = LocalDateTime.now().minusMonths(1);
                    dataFimDesenvolvimento = LocalDateTime.now();
                }

            } catch (Exception e) {
                log.error("Erro ao buscar range de datas: {}", e.getMessage(), e);
                dataInicioOperacional = LocalDateTime.now().minusMonths(1);
                dataFimOperacional = LocalDateTime.now();
                dataInicioDesenvolvimento = LocalDateTime.now().minusMonths(1);
                dataFimDesenvolvimento = LocalDateTime.now();
            }
        } else if (dataInicio != null && dataFim == null) {
            dataInicioOperacional = dataInicio;
            dataInicioDesenvolvimento = dataInicio;

            try {
                List<Object[]> operationalDateRangeList = deliveryRepository.findOperationalDateRange();
                List<Object[]> developmentDateRangeList = deliveryRepository.findDevelopmentDateRange();

                if (operationalDateRangeList != null && !operationalDateRangeList.isEmpty()) {
                    Object[] operationalRange = operationalDateRangeList.get(0);
                    if (operationalRange != null && operationalRange.length >= 2 && operationalRange[1] != null) {
                        dataFimOperacional = ((java.sql.Timestamp) operationalRange[1]).toLocalDateTime();
                    } else {
                        dataFimOperacional = LocalDateTime.now();
                    }
                } else {
                    dataFimOperacional = LocalDateTime.now();
                }

                if (developmentDateRangeList != null && !developmentDateRangeList.isEmpty()) {
                    Object[] developmentRange = developmentDateRangeList.get(0);
                    if (developmentRange != null && developmentRange.length >= 2 && developmentRange[1] != null) {
                        dataFimDesenvolvimento = ((java.sql.Timestamp) developmentRange[1]).toLocalDateTime();
                    } else {
                        dataFimDesenvolvimento = LocalDateTime.now();
                    }
                } else {
                    dataFimDesenvolvimento = LocalDateTime.now();
                }

            } catch (Exception e) {
                log.error("Erro ao buscar data fim: {}", e.getMessage(), e);
                dataFimOperacional = LocalDateTime.now();
                dataFimDesenvolvimento = LocalDateTime.now();
            }
        } else if (dataInicio == null && dataFim != null) {
            dataFimOperacional = dataFim;
            dataFimDesenvolvimento = dataFim;

            try {
                List<Object[]> operationalDateRangeList = deliveryRepository.findOperationalDateRange();
                List<Object[]> developmentDateRangeList = deliveryRepository.findDevelopmentDateRange();

                if (operationalDateRangeList != null && !operationalDateRangeList.isEmpty()) {
                    Object[] operationalRange = operationalDateRangeList.get(0);
                    if (operationalRange != null && operationalRange.length >= 2 && operationalRange[0] != null) {
                        dataInicioOperacional = ((java.sql.Timestamp) operationalRange[0]).toLocalDateTime();
                    } else {
                        dataInicioOperacional = LocalDateTime.now().minusMonths(1);
                    }
                } else {
                    dataInicioOperacional = LocalDateTime.now().minusMonths(1);
                }

                if (developmentDateRangeList != null && !developmentDateRangeList.isEmpty()) {
                    Object[] developmentRange = developmentDateRangeList.get(0);
                    if (developmentRange != null && developmentRange.length >= 2 && developmentRange[0] != null) {
                        dataInicioDesenvolvimento = ((java.sql.Timestamp) developmentRange[0]).toLocalDateTime();
                    } else {
                        dataInicioDesenvolvimento = LocalDateTime.now().minusMonths(1);
                    }
                } else {
                    dataInicioDesenvolvimento = LocalDateTime.now().minusMonths(1);
                }

            } catch (Exception e) {
                log.error("Erro ao buscar data início: {}", e.getMessage(), e);
                dataInicioOperacional = LocalDateTime.now().minusMonths(1);
                dataInicioDesenvolvimento = LocalDateTime.now().minusMonths(1);
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
                dataInicioOperacional,
                dataFimOperacional
        );

        String filtrosTipos = request.getTipoTarefa() != null && !request.getTipoTarefa().trim().isEmpty()
                ? request.getTipoTarefa()
                : "Todos";

        String filtrosAmbientes = "Todos";
        if (request.getAmbiente() != null) {
            filtrosAmbientes = request.getAmbiente().getDisplayName();
        }

        return OperationalReportData.builder()
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

    @Override
    public byte[] generateTaskReportPdf(Long taskId, boolean showValues) {
        try {
            log.info("Gerando relatorio PDF da tarefa ID: {} - showValues: {}", taskId, showValues);

            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("Tarefa nao encontrada: " + taskId));

            List<SubTask> subTasks = subTaskRepository.findByTaskId(taskId);

            TaskReportData reportData = buildTaskReportData(task, subTasks, showValues);

            JasperReport contentBlocksSubreport = loadContentBlocksSubreport();

            JasperReport taskReport = loadTaskJasperReport();
            Map<String, Object> taskParameters = buildTaskReportParameters(reportData);
            taskParameters.put("CONTENT_BLOCKS_SUBREPORT", contentBlocksSubreport);
            JasperPrint taskPrint = JasperFillManager.fillReport(taskReport, taskParameters, new JREmptyDataSource());

            List<JasperPrint> jasperPrints = new ArrayList<>();
            jasperPrints.add(taskPrint);

            if (!subTasks.isEmpty()) {
                JasperReport subTasksReport = loadSubTasksJasperReport();
                Map<String, Object> subTasksParameters = buildSubTasksReportParameters(reportData);
                subTasksParameters.put("CONTENT_BLOCKS_SUBREPORT", contentBlocksSubreport);
                JRBeanCollectionDataSource subTasksDataSource = new JRBeanCollectionDataSource(reportData.getSubTasks());
                JasperPrint subTasksPrint = JasperFillManager.fillReport(subTasksReport, subTasksParameters, subTasksDataSource);
                jasperPrints.add(subTasksPrint);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(SimpleExporterInput.getInstance(jasperPrints));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
            configuration.setCreatingBatchModeBookmarks(true);
            exporter.setConfiguration(configuration);
            exporter.exportReport();

            byte[] pdfBytes = outputStream.toByteArray();

            log.info("Relatorio PDF da tarefa gerado com sucesso - Tarefa: {} - {} subtarefas", task.getCode(), subTasks.size());

            return pdfBytes;

        } catch (Exception e) {
            log.error("Erro ao gerar relatorio PDF da tarefa", e);
            throw new RuntimeException("Erro ao gerar relatorio: " + e.getMessage(), e);
        }
    }

    private JasperReport loadContentBlocksSubreport() throws JRException {
        ClassPathResource jasperResource = new ClassPathResource("reports/content_blocks_subreport.jasper");
        if (jasperResource.exists()) {
            try {
                return (JasperReport) JRLoader.loadObject(jasperResource.getInputStream());
            } catch (Exception e) {
                log.warn("Erro ao carregar subreport de blocos de conteudo compilado, compilando .jrxml", e);
            }
        }

        try {
            ClassPathResource jrxmlResource = new ClassPathResource("reports/content_blocks_subreport.jrxml");
            return JasperCompileManager.compileReport(jrxmlResource.getInputStream());
        } catch (Exception ex) {
            log.error("Erro ao compilar subreport de blocos de conteudo", ex);
            throw new RuntimeException("Nao foi possivel carregar o subreport de blocos de conteudo", ex);
        }
    }

    private TaskReportData buildTaskReportData(Task task, List<SubTask> subTasks, boolean showValues) {
        boolean hasDelivery = deliveryRepository.existsByTaskId(task.getId());
        boolean hasQuoteInBilling = billingPeriodTaskRepository.existsByTaskId(task.getId());

        BigDecimal totalAmount = task.getAmount() != null ? task.getAmount() : BigDecimal.ZERO;
        BigDecimal subTasksTotalAmount = subTasks.stream()
                .map(SubTask::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ContentBlock> taskDescriptionBlocks = HtmlImageExtractor.parseHtmlToBlocks(task.getDescription(), fileStorageStrategy);
        boolean hasTaskDescriptionContent = !taskDescriptionBlocks.isEmpty();

        List<SubTaskReportRow> subTaskRows = new ArrayList<>();
        int order = 1;
        for (SubTask subTask : subTasks) {
            List<ContentBlock> subTaskBlocks = HtmlImageExtractor.parseHtmlToBlocks(subTask.getDescription(), fileStorageStrategy);
            boolean hasSubTaskContent = !subTaskBlocks.isEmpty();

            subTaskRows.add(SubTaskReportRow.builder()
                    .id(subTask.getId())
                    .title(subTask.getTitle())
                    .description(subTask.getDescription())
                    .descriptionBlocks(subTaskBlocks)
                    .hasDescriptionContent(hasSubTaskContent)
                    .amount(subTask.getAmount())
                    .amountFormatted(subTask.getAmount() != null ? CURRENCY_FORMATTER.format(subTask.getAmount()) : "-")
                    .order(order++)
                    .build());
        }

        return TaskReportData.builder()
                .id(task.getId())
                .code(task.getCode())
                .title(task.getTitle())
                .description(task.getDescription())
                .descriptionBlocks(taskDescriptionBlocks)
                .hasDescriptionContent(hasTaskDescriptionContent)
                .flowType(task.getFlowType() != null ? task.getFlowType().name() : null)
                .flowTypeLabel(getFlowTypeLabel(task.getFlowType() != null ? task.getFlowType().name() : null))
                .taskType(task.getTaskType())
                .taskTypeLabel(getTaskTypeLabel(task.getTaskType()))
                .environment(task.getEnvironment() != null ? task.getEnvironment().name() : null)
                .environmentLabel(getEnvironmentLabel(task.getEnvironment() != null ? task.getEnvironment().name() : null))
                .priority(task.getPriority())
                .priorityLabel(getPriorityLabel(task.getPriority()))
                .systemModule(task.getSystemModule())
                .serverOrigin(task.getServerOrigin())
                .link(task.getLink())
                .meetingLink(task.getMeetingLink())
                .requesterName(task.getRequester() != null ? task.getRequester().getName() : null)
                .requesterPhone(task.getRequester() != null ? task.getRequester().getPhone() : null)
                .requesterEmail(task.getRequester() != null ? task.getRequester().getEmail() : null)
                .hasDelivery(hasDelivery)
                .hasDeliveryLabel(hasDelivery ? "Sim" : "Nao")
                .hasQuoteInBilling(hasQuoteInBilling)
                .hasQuoteInBillingLabel(hasQuoteInBilling ? "Sim" : "Nao")
                .totalAmount(totalAmount)
                .totalAmountFormatted(CURRENCY_FORMATTER.format(totalAmount))
                .showValues(showValues)
                .subTasksCount(subTasks.size())
                .subTasks(subTaskRows)
                .subTasksTotalAmount(subTasksTotalAmount)
                .subTasksTotalAmountFormatted(CURRENCY_FORMATTER.format(subTasksTotalAmount))
                .createdAt(task.getCreatedAt())
                .createdAtFormatted(task.getCreatedAt() != null ? task.getCreatedAt().format(DATE_TIME_FORMATTER) : null)
                .updatedAt(task.getUpdatedAt())
                .updatedAtFormatted(task.getUpdatedAt() != null ? task.getUpdatedAt().format(DATE_TIME_FORMATTER) : null)
                .createdByUserName(task.getCreatedBy() != null ? task.getCreatedBy().getName() : null)
                .updatedByUserName(task.getUpdatedBy() != null ? task.getUpdatedBy().getName() : null)
                .dataGeracao(LocalDateTime.now())
                .build();
    }

    private JasperReport loadTaskJasperReport() throws JRException {
        ClassPathResource jasperResource = new ClassPathResource("reports/task_report.jasper");
        if (jasperResource.exists()) {
            try {
                return (JasperReport) JRLoader.loadObject(jasperResource.getInputStream());
            } catch (Exception e) {
                log.warn("Erro ao carregar template Jasper compilado, compilando .jrxml", e);
            }
        }

        try {
            ClassPathResource jrxmlResource = new ClassPathResource("reports/task_report.jrxml");
            return JasperCompileManager.compileReport(jrxmlResource.getInputStream());
        } catch (Exception ex) {
            log.error("Erro ao compilar template Jasper da tarefa", ex);
            throw new RuntimeException("Nao foi possivel carregar o template do relatorio de tarefa", ex);
        }
    }

    private Map<String, Object> buildTaskReportParameters(TaskReportData data) {
        Map<String, Object> parameters = new HashMap<>();

        parameters.put("id", data.getId());
        parameters.put("code", data.getCode());
        parameters.put("title", data.getTitle());
        parameters.put("descriptionBlocks", new JRBeanCollectionDataSource(
                data.getDescriptionBlocks() != null ? data.getDescriptionBlocks() : new ArrayList<>()));
        parameters.put("hasDescriptionContent", data.getHasDescriptionContent() != null && data.getHasDescriptionContent());
        parameters.put("flowTypeLabel", data.getFlowTypeLabel());
        parameters.put("taskTypeLabel", data.getTaskTypeLabel());
        parameters.put("environmentLabel", data.getEnvironmentLabel());
        parameters.put("priorityLabel", data.getPriorityLabel());
        parameters.put("systemModule", data.getSystemModule());
        parameters.put("serverOrigin", data.getServerOrigin());
        parameters.put("link", data.getLink());
        parameters.put("meetingLink", data.getMeetingLink());
        parameters.put("requesterName", data.getRequesterName());
        parameters.put("requesterPhone", data.getRequesterPhone());
        parameters.put("requesterEmail", data.getRequesterEmail());
        parameters.put("hasDeliveryLabel", data.getHasDeliveryLabel());
        parameters.put("hasQuoteInBillingLabel", data.getHasQuoteInBillingLabel());
        parameters.put("totalAmountFormatted", data.getTotalAmountFormatted());
        parameters.put("showValues", data.getShowValues());
        parameters.put("subTasksCount", data.getSubTasksCount());
        parameters.put("subTasksDataSource", new JRBeanCollectionDataSource(data.getSubTasks()));
        parameters.put("subTasksTotalAmountFormatted", data.getSubTasksTotalAmountFormatted());
        parameters.put("createdAtFormatted", data.getCreatedAtFormatted());
        parameters.put("updatedAtFormatted", data.getUpdatedAtFormatted());
        parameters.put("createdByUserName", data.getCreatedByUserName());
        parameters.put("updatedByUserName", data.getUpdatedByUserName());
        parameters.put("dataGeracao", data.getDataGeracao());
        parameters.put("copyright", data.getCopyright());
        parameters.put("sistemaTagline", data.getSistemaTagline());
        parameters.put("desenvolvedorEmail", data.getDesenvolvedorEmail());
        parameters.put("desenvolvedorTelefone", data.getDesenvolvedorTelefone());

        return parameters;
    }

    private JasperReport loadSubTasksJasperReport() throws JRException {
        ClassPathResource jasperResource = new ClassPathResource("reports/task_subtasks_report.jasper");
        if (jasperResource.exists()) {
            try {
                return (JasperReport) JRLoader.loadObject(jasperResource.getInputStream());
            } catch (Exception e) {
                log.warn("Erro ao carregar template Jasper de subtarefas compilado, compilando .jrxml", e);
            }
        }

        try {
            ClassPathResource jrxmlResource = new ClassPathResource("reports/task_subtasks_report.jrxml");
            return JasperCompileManager.compileReport(jrxmlResource.getInputStream());
        } catch (Exception ex) {
            log.error("Erro ao compilar template Jasper de subtarefas", ex);
            throw new RuntimeException("Nao foi possivel carregar o template do relatorio de subtarefas", ex);
        }
    }

    private Map<String, Object> buildSubTasksReportParameters(TaskReportData data) {
        Map<String, Object> parameters = new HashMap<>();

        parameters.put("taskCode", data.getCode());
        parameters.put("taskTitle", data.getTitle());
        parameters.put("subTasksCount", data.getSubTasksCount());
        parameters.put("subTasksTotalAmountFormatted", data.getSubTasksTotalAmountFormatted());
        parameters.put("showValues", data.getShowValues());
        parameters.put("copyright", data.getCopyright());
        parameters.put("desenvolvedorEmail", data.getDesenvolvedorEmail());
        parameters.put("desenvolvedorTelefone", data.getDesenvolvedorTelefone());

        return parameters;
    }

    private String getFlowTypeLabel(String flowType) {
        if (flowType == null) return "-";
        return switch (flowType) {
            case "OPERACIONAL" -> "Operacional";
            case "DESENVOLVIMENTO" -> "Desenvolvimento";
            default -> flowType;
        };
    }

    private String getTaskTypeLabel(String taskType) {
        if (taskType == null) return "-";
        return switch (taskType) {
            case "BUG" -> "Bug";
            case "ENHANCEMENT" -> "Melhoria";
            case "NEW_FEATURE" -> "Nova Funcionalidade";
            case "BACKUP" -> "Backup";
            case "DEPLOY" -> "Deploy";
            case "LOGS" -> "Logs";
            case "DATABASE_APPLICATION" -> "Aplicacao de Banco";
            case "NEW_SERVER" -> "Novo Servidor";
            case "MONITORING" -> "Monitoramento";
            case "SUPPORT" -> "Suporte";
            default -> taskType;
        };
    }

    private String getEnvironmentLabel(String environment) {
        if (environment == null) return null;
        return switch (environment) {
            case "DESENVOLVIMENTO" -> "Desenvolvimento";
            case "HOMOLOGACAO" -> "Homologacao";
            case "PRODUCAO" -> "Producao";
            default -> environment;
        };
    }

    private String getPriorityLabel(String priority) {
        if (priority == null) return "Media";
        return switch (priority) {
            case "LOW" -> "Baixa";
            case "MEDIUM" -> "Media";
            case "HIGH" -> "Alta";
            case "URGENT" -> "Urgente";
            default -> priority;
        };
    }

    @Override
    public byte[] generateDeliveryReportPdf(Long deliveryId) {
        try {
            log.info("Gerando relatorio PDF da entrega ID: {}", deliveryId);

            Delivery delivery = deliveryRepository.findById(deliveryId)
                    .orElseThrow(() -> new RuntimeException("Entrega nao encontrada: " + deliveryId));

            DeliveryReportData reportData = buildDeliveryReportData(delivery);

            JasperReport contentBlocksSubreport = loadContentBlocksSubreport();

            JasperReport deliveryReport = loadDeliveryJasperReport();
            Map<String, Object> deliveryParameters = buildDeliveryReportParameters(reportData);
            deliveryParameters.put("CONTENT_BLOCKS_SUBREPORT", contentBlocksSubreport);
            JasperPrint deliveryPrint = JasperFillManager.fillReport(deliveryReport, deliveryParameters, new JREmptyDataSource());

            List<JasperPrint> jasperPrints = new ArrayList<>();
            jasperPrints.add(deliveryPrint);

            if (!reportData.getItems().isEmpty()) {
                boolean isDesenvolvimento = "DESENVOLVIMENTO".equals(reportData.getFlowType());
                JasperReport itemsReport = isDesenvolvimento ? loadDeliveryItemsDevJasperReport() : loadDeliveryItemsOpJasperReport();
                Map<String, Object> itemsParameters = buildDeliveryItemsReportParameters(reportData);
                itemsParameters.put("CONTENT_BLOCKS_SUBREPORT", contentBlocksSubreport);
                JRBeanCollectionDataSource itemsDataSource = new JRBeanCollectionDataSource(reportData.getItems());
                JasperPrint itemsPrint = JasperFillManager.fillReport(itemsReport, itemsParameters, itemsDataSource);
                jasperPrints.add(itemsPrint);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(SimpleExporterInput.getInstance(jasperPrints));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
            configuration.setCreatingBatchModeBookmarks(true);
            exporter.setConfiguration(configuration);
            exporter.exportReport();

            byte[] pdfBytes = outputStream.toByteArray();

            log.info("Relatorio PDF da entrega gerado com sucesso - Entrega: {} - {} itens", delivery.getId(), reportData.getTotalItems());

            return pdfBytes;

        } catch (Exception e) {
            log.error("Erro ao gerar relatorio PDF da entrega", e);
            throw new RuntimeException("Erro ao gerar relatorio: " + e.getMessage(), e);
        }
    }

    private DeliveryReportData buildDeliveryReportData(Delivery delivery) {
        List<DeliveryItemReportRow> itemRows = new ArrayList<>();
        int order = 1;

        if (delivery.getItems() != null) {
            for (DeliveryItem item : delivery.getItems()) {
                List<ContentBlock> notesBlocks = HtmlImageExtractor.parseHtmlToBlocks(item.getNotes(), fileStorageStrategy);
                itemRows.add(DeliveryItemReportRow.builder()
                        .id(item.getId())
                        .order(order++)
                        .itemType("Desenvolvimento")
                        .projectName(item.getProject() != null ? item.getProject().getName() : null)
                        .title(null)
                        .description(null)
                        .status(item.getStatus() != null ? item.getStatus().name() : null)
                        .statusLabel(getDeliveryStatusLabel(item.getStatus() != null ? item.getStatus().name() : null))
                        .branch(item.getBranch())
                        .sourceBranch(item.getSourceBranch())
                        .pullRequest(item.getPullRequest())
                        .notes(item.getNotes())
                        .notesBlocks(notesBlocks)
                        .hasNotesContent(!notesBlocks.isEmpty())
                        .startedAtFormatted(item.getStartedAt() != null ? item.getStartedAt().format(DATE_TIME_FORMATTER) : null)
                        .finishedAtFormatted(item.getFinishedAt() != null ? item.getFinishedAt().format(DATE_TIME_FORMATTER) : null)
                        .build());
            }
        }

        if (delivery.getOperationalItems() != null) {
            for (DeliveryOperationalItem item : delivery.getOperationalItems()) {
                List<ContentBlock> descBlocks = HtmlImageExtractor.parseHtmlToBlocks(item.getDescription(), fileStorageStrategy);
                itemRows.add(DeliveryItemReportRow.builder()
                        .id(item.getId())
                        .order(order++)
                        .itemType("Operacional")
                        .projectName(null)
                        .title(item.getTitle())
                        .description(item.getDescription())
                        .descriptionBlocks(descBlocks)
                        .hasDescriptionContent(!descBlocks.isEmpty())
                        .status(item.getStatus() != null ? item.getStatus().name() : null)
                        .statusLabel(getOperationalStatusLabel(item.getStatus() != null ? item.getStatus().name() : null))
                        .branch(null)
                        .sourceBranch(null)
                        .pullRequest(null)
                        .notes(null)
                        .startedAtFormatted(item.getStartedAt() != null ? item.getStartedAt().format(DATE_TIME_FORMATTER) : null)
                        .finishedAtFormatted(item.getFinishedAt() != null ? item.getFinishedAt().format(DATE_TIME_FORMATTER) : null)
                        .build());
            }
        }

        Task task = delivery.getTask();

        List<ContentBlock> deliveryNotesBlocks = HtmlImageExtractor.parseHtmlToBlocks(delivery.getNotes(), fileStorageStrategy);

        return DeliveryReportData.builder()
                .id(delivery.getId())
                .taskId(task.getId())
                .taskCode(task.getCode())
                .taskTitle(task.getTitle())
                .flowType(delivery.getFlowType() != null ? delivery.getFlowType().name() : null)
                .flowTypeLabel(getFlowTypeLabel(delivery.getFlowType() != null ? delivery.getFlowType().name() : null))
                .environment(delivery.getEnvironment() != null ? delivery.getEnvironment().name() : null)
                .environmentLabel(getEnvironmentLabel(delivery.getEnvironment() != null ? delivery.getEnvironment().name() : null))
                .status(delivery.getStatus() != null ? delivery.getStatus().name() : null)
                .statusLabel(getDeliveryStatusLabel(delivery.getStatus() != null ? delivery.getStatus().name() : null))
                .notes(delivery.getNotes())
                .notesBlocks(deliveryNotesBlocks)
                .hasNotesContent(!deliveryNotesBlocks.isEmpty())
                .startedAt(delivery.getStartedAt())
                .startedAtFormatted(delivery.getStartedAt() != null ? delivery.getStartedAt().format(DATE_TIME_FORMATTER) : null)
                .finishedAt(delivery.getFinishedAt())
                .finishedAtFormatted(delivery.getFinishedAt() != null ? delivery.getFinishedAt().format(DATE_TIME_FORMATTER) : null)
                .totalItems(itemRows.size())
                .items(itemRows)
                .createdAt(delivery.getCreatedAt())
                .createdAtFormatted(delivery.getCreatedAt() != null ? delivery.getCreatedAt().format(DATE_TIME_FORMATTER) : null)
                .updatedAt(delivery.getUpdatedAt())
                .updatedAtFormatted(delivery.getUpdatedAt() != null ? delivery.getUpdatedAt().format(DATE_TIME_FORMATTER) : null)
                .dataGeracao(LocalDateTime.now())
                .build();
    }

    private JasperReport loadDeliveryJasperReport() throws JRException {
        ClassPathResource jasperResource = new ClassPathResource("reports/delivery_report.jasper");
        if (jasperResource.exists()) {
            try {
                return (JasperReport) JRLoader.loadObject(jasperResource.getInputStream());
            } catch (Exception e) {
                log.warn("Erro ao carregar template Jasper de entrega compilado, compilando .jrxml", e);
            }
        }

        try {
            ClassPathResource jrxmlResource = new ClassPathResource("reports/delivery_report.jrxml");
            return JasperCompileManager.compileReport(jrxmlResource.getInputStream());
        } catch (Exception ex) {
            log.error("Erro ao compilar template Jasper de entrega", ex);
            throw new RuntimeException("Nao foi possivel carregar o template do relatorio de entrega", ex);
        }
    }

    private JasperReport loadDeliveryItemsDevJasperReport() throws JRException {
        ClassPathResource jasperResource = new ClassPathResource("reports/delivery_items_dev_report.jasper");
        if (jasperResource.exists()) {
            try {
                return (JasperReport) JRLoader.loadObject(jasperResource.getInputStream());
            } catch (Exception e) {
                log.warn("Erro ao carregar template Jasper de itens dev compilado, compilando .jrxml", e);
            }
        }

        try {
            ClassPathResource jrxmlResource = new ClassPathResource("reports/delivery_items_dev_report.jrxml");
            return JasperCompileManager.compileReport(jrxmlResource.getInputStream());
        } catch (Exception ex) {
            log.error("Erro ao compilar template Jasper de itens dev", ex);
            throw new RuntimeException("Nao foi possivel carregar o template do relatorio de itens dev", ex);
        }
    }

    private JasperReport loadDeliveryItemsOpJasperReport() throws JRException {
        ClassPathResource jasperResource = new ClassPathResource("reports/delivery_items_op_report.jasper");
        if (jasperResource.exists()) {
            try {
                return (JasperReport) JRLoader.loadObject(jasperResource.getInputStream());
            } catch (Exception e) {
                log.warn("Erro ao carregar template Jasper de itens op compilado, compilando .jrxml", e);
            }
        }

        try {
            ClassPathResource jrxmlResource = new ClassPathResource("reports/delivery_items_op_report.jrxml");
            return JasperCompileManager.compileReport(jrxmlResource.getInputStream());
        } catch (Exception ex) {
            log.error("Erro ao compilar template Jasper de itens op", ex);
            throw new RuntimeException("Nao foi possivel carregar o template do relatorio de itens op", ex);
        }
    }

    private Map<String, Object> buildDeliveryReportParameters(DeliveryReportData data) {
        Map<String, Object> parameters = new HashMap<>();

        parameters.put("id", data.getId());
        parameters.put("taskId", data.getTaskId());
        parameters.put("taskCode", data.getTaskCode());
        parameters.put("taskTitle", data.getTaskTitle());
        parameters.put("flowTypeLabel", data.getFlowTypeLabel());
        parameters.put("environmentLabel", data.getEnvironmentLabel());
        parameters.put("statusLabel", data.getStatusLabel());
        parameters.put("notes", data.getNotes());
        parameters.put("hasNotesContent", data.isHasNotesContent());
        parameters.put("notesBlocksDataSource", new JRBeanCollectionDataSource(data.getNotesBlocks() != null ? data.getNotesBlocks() : new ArrayList<>()));
        parameters.put("startedAtFormatted", data.getStartedAtFormatted());
        parameters.put("finishedAtFormatted", data.getFinishedAtFormatted());
        parameters.put("totalItems", data.getTotalItems());
        parameters.put("createdAtFormatted", data.getCreatedAtFormatted());
        parameters.put("updatedAtFormatted", data.getUpdatedAtFormatted());
        parameters.put("dataGeracao", data.getDataGeracao());
        parameters.put("copyright", data.getCopyright());
        parameters.put("sistemaTagline", data.getSistemaTagline());
        parameters.put("desenvolvedorEmail", data.getDesenvolvedorEmail());
        parameters.put("desenvolvedorTelefone", data.getDesenvolvedorTelefone());

        return parameters;
    }

    private Map<String, Object> buildDeliveryItemsReportParameters(DeliveryReportData data) {
        Map<String, Object> parameters = new HashMap<>();

        parameters.put("taskCode", data.getTaskCode());
        parameters.put("taskTitle", data.getTaskTitle());
        parameters.put("totalItems", data.getTotalItems());
        parameters.put("flowTypeLabel", data.getFlowTypeLabel());
        parameters.put("copyright", data.getCopyright());
        parameters.put("desenvolvedorEmail", data.getDesenvolvedorEmail());
        parameters.put("desenvolvedorTelefone", data.getDesenvolvedorTelefone());

        return parameters;
    }

    private String getDeliveryStatusLabel(String status) {
        if (status == null) return "-";
        return switch (status) {
            case "PENDING" -> "Pendente";
            case "DEVELOPMENT" -> "Em Desenvolvimento";
            case "HOMOLOGATION" -> "Em Homologacao";
            case "APPROVED" -> "Aprovado";
            case "REJECTED" -> "Rejeitado";
            case "PRODUCTION" -> "Em Producao";
            case "DELIVERED" -> "Entregue";
            case "CANCELLED" -> "Cancelado";
            default -> status;
        };
    }

    private String getOperationalStatusLabel(String status) {
        if (status == null) return "-";
        return switch (status) {
            case "PENDING" -> "Pendente";
            case "DELIVERED" -> "Entregue";
            case "CANCELLED" -> "Cancelado";
            default -> status;
        };
    }
}

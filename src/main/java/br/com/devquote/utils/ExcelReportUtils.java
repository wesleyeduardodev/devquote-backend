package br.com.devquote.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class ExcelReportUtils {

    public byte[] generateTasksReport(List<Map<String, Object>> data, boolean canViewAmounts) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Relatório de Tarefas");

        // Criar estilos
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);

        // Definir cabeçalhos baseado no perfil do usuário
        String[] headers;
        if (canViewAmounts) {
            // ADMIN/MANAGER: Todas as colunas
            headers = new String[] {
                "ID", "Código", "Título", "Descrição", "Status", "Tipo",
                "Prioridade", "Solicitante", "Criado Por", "Atualizado Por",
                "Origem do Servidor", "Módulo do Sistema", "Link", "Link da Reunião",
                "Observações", "Valor da Tarefa", "Tem Subtarefas", "Tem Orçamento",
                "Orçamento no Faturamento", "Data de Criação", "Data de Atualização",
                "Subtarefa ID", "Subtarefa Título", "Subtarefa Descrição",
                "Subtarefa Status", "Subtarefa Valor"
            };
        } else {
            // USER: Remove colunas sensíveis (Valor da Tarefa, Tem Orçamento, Orçamento no Faturamento, Subtarefa Valor)
            headers = new String[] {
                "ID", "Código", "Título", "Descrição", "Status", "Tipo",
                "Prioridade", "Solicitante", "Criado Por", "Atualizado Por",
                "Origem do Servidor", "Módulo do Sistema", "Link", "Link da Reunião",
                "Observações", "Tem Subtarefas", "Data de Criação", "Data de Atualização",
                "Subtarefa ID", "Subtarefa Título", "Subtarefa Descrição",
                "Subtarefa Status"
            };
        }

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Adicionar dados baseado no perfil
        int rowNum = 1;
        for (Map<String, Object> taskData : data) {
            Row row = sheet.createRow(rowNum++);

            if (canViewAmounts) {
                // ADMIN/MANAGER: Todas as colunas
                setCellValue(row, 0, taskData.get("task_id"), dataStyle);
                setCellValue(row, 1, taskData.get("task_code"), dataStyle);
                setCellValue(row, 2, taskData.get("task_title"), dataStyle);
                setCellValue(row, 3, taskData.get("task_description"), dataStyle);
                setStatusCell(row, 4, taskData.get("task_status"), dataStyle);
                setCellValue(row, 5, taskData.get("task_type"), dataStyle);
                setPriorityCell(row, 6, taskData.get("task_priority"), dataStyle);
                setCellValue(row, 7, taskData.get("requester_name"), dataStyle);
                setCellValue(row, 8, taskData.get("created_by_user"), dataStyle);
                setCellValue(row, 9, taskData.get("updated_by_user"), dataStyle);
                setCellValue(row, 10, taskData.get("server_origin"), dataStyle);
                setCellValue(row, 11, taskData.get("system_module"), dataStyle);
                setCellValue(row, 12, taskData.get("task_link"), dataStyle);
                setCellValue(row, 13, taskData.get("meeting_link"), dataStyle);
                setCellValue(row, 14, taskData.get("task_notes"), dataStyle);
                setCellValue(row, 15, taskData.get("task_amount"), currencyStyle);
                setCellValue(row, 16, taskData.get("has_subtasks"), dataStyle);
                setCellValue(row, 17, taskData.get("has_quote"), dataStyle);
                setCellValue(row, 18, taskData.get("has_quote_in_billing"), dataStyle);
                setCellValue(row, 19, taskData.get("task_created_at"), dateStyle);
                setCellValue(row, 20, taskData.get("task_updated_at"), dateStyle);
                setCellValue(row, 21, taskData.get("subtask_id"), dataStyle);
                setCellValue(row, 22, taskData.get("subtask_title"), dataStyle);
                setCellValue(row, 23, taskData.get("subtask_description"), dataStyle);
                setStatusCell(row, 24, taskData.get("subtask_status"), dataStyle);
                setCellValue(row, 25, taskData.get("subtask_amount"), currencyStyle);
            } else {
                // USER: Remove colunas sensíveis
                setCellValue(row, 0, taskData.get("task_id"), dataStyle);
                setCellValue(row, 1, taskData.get("task_code"), dataStyle);
                setCellValue(row, 2, taskData.get("task_title"), dataStyle);
                setCellValue(row, 3, taskData.get("task_description"), dataStyle);
                setStatusCell(row, 4, taskData.get("task_status"), dataStyle);
                setCellValue(row, 5, taskData.get("task_type"), dataStyle);
                setPriorityCell(row, 6, taskData.get("task_priority"), dataStyle);
                setCellValue(row, 7, taskData.get("requester_name"), dataStyle);
                setCellValue(row, 8, taskData.get("created_by_user"), dataStyle);
                setCellValue(row, 9, taskData.get("updated_by_user"), dataStyle);
                setCellValue(row, 10, taskData.get("server_origin"), dataStyle);
                setCellValue(row, 11, taskData.get("system_module"), dataStyle);
                setCellValue(row, 12, taskData.get("task_link"), dataStyle);
                setCellValue(row, 13, taskData.get("meeting_link"), dataStyle);
                setCellValue(row, 14, taskData.get("task_notes"), dataStyle);
                setCellValue(row, 15, taskData.get("has_subtasks"), dataStyle);
                setCellValue(row, 16, taskData.get("task_created_at"), dateStyle);
                setCellValue(row, 17, taskData.get("task_updated_at"), dateStyle);
                setCellValue(row, 18, taskData.get("subtask_id"), dataStyle);
                setCellValue(row, 19, taskData.get("subtask_title"), dataStyle);
                setCellValue(row, 20, taskData.get("subtask_description"), dataStyle);
                setStatusCell(row, 21, taskData.get("subtask_status"), dataStyle);
            }
        }

        // Ajustar largura das colunas baseado no conteúdo
        if (canViewAmounts) {
            // ADMIN/MANAGER: 26 colunas
            setColumnWidths(sheet, new int[]{
                2500,  // ID
                3500,  // Código
                8000,  // Título (maior)
                10000, // Descrição (maior)
                3000,  // Status
                3500,  // Tipo
                3000,  // Prioridade
                6000,  // Solicitante
                4000,  // Criado Por
                4000,  // Atualizado Por
                4000,  // Origem do Servidor
                4000,  // Módulo do Sistema
                8000,  // Link (maior para URLs)
                8000,  // Link da Reunião (maior para URLs)
                6000,  // Observações
                3500,  // Valor da Tarefa
                3000,  // Tem Subtarefas
                3000,  // Tem Orçamento
                4000,  // Orçamento no Faturamento
                6000,  // Data de Criação (aumentada para dd/mm/yyyy hh:mm:ss)
                6000,  // Data de Atualização (aumentada para dd/mm/yyyy hh:mm:ss)
                2500,  // Subtarefa ID
                8000,  // Subtarefa Título (maior)
                10000, // Subtarefa Descrição (maior)
                3000,  // Subtarefa Status
                3500   // Subtarefa Valor
            });
        } else {
            // USER: 22 colunas (sem colunas de valores)
            setColumnWidths(sheet, new int[]{
                2500,  // ID
                3500,  // Código
                8000,  // Título (maior)
                10000, // Descrição (maior)
                3000,  // Status
                3500,  // Tipo
                3000,  // Prioridade
                6000,  // Solicitante
                4000,  // Criado Por
                4000,  // Atualizado Por
                4000,  // Origem do Servidor
                4000,  // Módulo do Sistema
                8000,  // Link (maior para URLs)
                8000,  // Link da Reunião (maior para URLs)
                6000,  // Observações
                3000,  // Tem Subtarefas
                6000,  // Data de Criação (aumentada para dd/mm/yyyy hh:mm:ss)
                6000,  // Data de Atualização (aumentada para dd/mm/yyyy hh:mm:ss)
                2500,  // Subtarefa ID
                8000,  // Subtarefa Título (maior)
                10000, // Subtarefa Descrição (maior)
                3000   // Subtarefa Status
            });
        }

        // Ajustar altura das linhas para acomodar texto longo
        for (int i = 1; i <= data.size(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                row.setHeightInPoints(30); // Altura maior para linhas de dados
            }
        }

        // Altura do cabeçalho
        headerRow.setHeightInPoints(35);

        // Aplicar filtros
        sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, data.size(), 0, headers.length - 1));

        // Congelar primeira linha (cabeçalho)
        sheet.createFreezePane(0, 1);

        // Converter para bytes
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    public byte[] generateQuotesReport(List<Map<String, Object>> data, boolean canViewAmounts) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Relatório de Orçamentos");

        // Criar estilos
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);

        // Cabeçalhos completos (ADMIN/MANAGER)
        String[] headers = {
            "ID Orçamento", "Status do Orçamento", "ID Tarefa", "Código da Tarefa", "Título da Tarefa",
            "Valor Total da Tarefa", "Status da Tarefa", "Prioridade da Tarefa", "Solicitante",
            "Vinculado ao Faturamento", "Valor Total do Orçamento", "Data de Criação", "Data de Atualização"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Adicionar dados (sempre versão completa)
        int rowNum = 1;
        for (Map<String, Object> quoteData : data) {
            Row row = sheet.createRow(rowNum++);

            setCellValue(row, 0, quoteData.get("quote_id"), dataStyle);
            setStatusCell(row, 1, quoteData.get("quote_status"), dataStyle);
            setCellValue(row, 2, quoteData.get("task_id"), dataStyle);
            setCellValue(row, 3, quoteData.get("task_code"), dataStyle);
            setCellValue(row, 4, quoteData.get("task_title"), dataStyle);
            setCellValue(row, 5, quoteData.get("task_amount"), currencyStyle);
            setStatusCell(row, 6, quoteData.get("task_status"), dataStyle);
            setPriorityCell(row, 7, quoteData.get("task_priority"), dataStyle);
            setCellValue(row, 8, quoteData.get("requester_name"), dataStyle);
            setCellValue(row, 9, quoteData.get("has_billing"), dataStyle);
            setCellValue(row, 10, quoteData.get("quote_total_amount"), currencyStyle);
            setCellValue(row, 11, quoteData.get("quote_created_at"), dateStyle);
            setCellValue(row, 12, quoteData.get("quote_updated_at"), dateStyle);
        }

        // Ajustar largura das colunas (13 colunas)
        setColumnWidths(sheet, new int[]{
            3000,  // ID Orçamento
            3500,  // Status do Orçamento
            2500,  // ID Tarefa
            3500,  // Código da Tarefa
            8000,  // Título da Tarefa (maior)
            4000,  // Valor Total da Tarefa
            3500,  // Status da Tarefa
            3000,  // Prioridade da Tarefa
            6000,  // Solicitante
            4000,  // Vinculado ao Faturamento
            4000,  // Valor Total do Orçamento
            6000,  // Data de Criação
            6000   // Data de Atualização
        });

        // Ajustar altura das linhas
        for (int i = 1; i <= data.size(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                row.setHeightInPoints(30);
            }
        }

        // Altura do cabeçalho
        headerRow.setHeightInPoints(35);

        // Aplicar filtros
        sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, data.size(), 0, headers.length - 1));

        // Congelar primeira linha (cabeçalho)
        sheet.createFreezePane(0, 1);

        // Converter para bytes
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    private void setColumnWidths(Sheet sheet, int[] widths) {
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i]);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();

        // Configurar fonte
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);

        // Configurar fundo
        style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Configurar bordas
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // Configurar alinhamento
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);

        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // Configurar bordas
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // Configurar alinhamento
        style.setVerticalAlignment(VerticalAlignment.TOP); // Alinhamento superior para textos longos
        style.setWrapText(true); // Quebra de linha automática

        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        CreationHelper helper = workbook.getCreationHelper();
        style.setDataFormat(helper.createDataFormat().getFormat("dd/mm/yyyy hh:mm:ss"));
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        CreationHelper helper = workbook.getCreationHelper();
        style.setDataFormat(helper.createDataFormat().getFormat("\"R$ \"#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private CellStyle createDateOnlyStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        CreationHelper helper = workbook.getCreationHelper();
        style.setDataFormat(helper.createDataFormat().getFormat("dd/mm/yyyy"));
        return style;
    }

    private void setCellValue(Row row, int columnIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellStyle(style);

        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof LocalDateTime) {
            // Converter LocalDateTime para Date para garantir formatação correta
            LocalDateTime dateTime = (LocalDateTime) value;
            java.util.Date date = java.sql.Timestamp.valueOf(dateTime);
            cell.setCellValue(date);
        } else if (value instanceof java.sql.Timestamp) {
            // Caso venha como Timestamp do banco
            cell.setCellValue((java.sql.Timestamp) value);
        } else if (value instanceof java.sql.Date) {
            // Caso venha como Date do banco (para datas só)
            cell.setCellValue((java.sql.Date) value);
        } else if (value instanceof java.util.Date) {
            // Caso venha como Date genérico
            cell.setCellValue((java.util.Date) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value ? "Sim" : "Não");
        } else {
            cell.setCellValue(value.toString());
        }
    }

    private void setStatusCell(Row row, int columnIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellStyle(style);

        if (value == null) {
            cell.setCellValue("");
        } else {
            String status = value.toString();
            String translatedStatus = translateStatus(status);
            cell.setCellValue(translatedStatus);
        }
    }

    private void setPriorityCell(Row row, int columnIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellStyle(style);

        if (value == null) {
            cell.setCellValue("");
        } else {
            String priority = value.toString();
            String translatedPriority = translatePriority(priority);
            cell.setCellValue(translatedPriority);
        }
    }

    private String translateStatus(String status) {
        if (status == null) return "";
        return switch (status.toUpperCase()) {
            case "PENDING" -> "Pendente";
            case "IN_PROGRESS" -> "Em Progresso";
            case "COMPLETED" -> "Concluída";
            case "CANCELLED" -> "Cancelada";
            case "ON_HOLD" -> "Em Espera";
            case "BLOCKED" -> "Bloqueada";
            case "REVIEWING" -> "Em Revisão";
            case "APPROVED" -> "Aprovada";
            case "REJECTED" -> "Rejeitada";
            case "DRAFT" -> "Rascunho";
            case "ACTIVE" -> "Ativa";
            case "INACTIVE" -> "Inativa";
            case "PAUSED" -> "Pausada";
            case "REOPENED" -> "Reaberta";
            default -> status;
        };
    }

    private String translatePriority(String priority) {
        if (priority == null) return "";
        return switch (priority.toUpperCase()) {
            case "LOW" -> "Baixa";
            case "MEDIUM" -> "Média";
            case "HIGH" -> "Alta";
            case "URGENT" -> "Urgente";
            case "CRITICAL" -> "Crítica";
            case "VERY_LOW" -> "Muito Baixa";
            case "VERY_HIGH" -> "Muito Alta";
            case "IMMEDIATE" -> "Imediata";
            case "NORMAL" -> "Normal";
            default -> priority;
        };
    }

    public byte[] generateDeliveriesReport(List<Map<String, Object>> data) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Relatório de Entregas");

        // Criar estilos
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);
        CellStyle dateOnlyStyle = createDateOnlyStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);

        // Cabeçalhos reorganizados: Dados da Tarefa primeiro, depois Dados da Entrega
        String[] headers = {
            "ID Tarefa", "Código da Tarefa", "Título da Tarefa", "Status da Tarefa",
            "Qtd. Subtarefas", "Solicitante", "ID Entrega",
            "Status da Entrega", "Projeto", "Link da entrega (Pull Request)", "Branch",
            "Script", "Observações", "Data de Início", "Data de Fim", "Data de Criação", "Data de Atualização"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Adicionar dados: Dados da Tarefa primeiro, depois Dados da Entrega
        int rowNum = 1;
        for (Map<String, Object> deliveryData : data) {
            Row row = sheet.createRow(rowNum++);

            // Dados da tarefa
            setCellValue(row, 0, deliveryData.get("task_id"), dataStyle);
            setCellValue(row, 1, deliveryData.get("task_code"), dataStyle);
            setCellValue(row, 2, deliveryData.get("task_title"), dataStyle);
            setStatusCell(row, 3, deliveryData.get("task_status"), dataStyle);
            setCellValue(row, 4, deliveryData.get("subtasks_count"), dataStyle);
            setCellValue(row, 5, deliveryData.get("requester_name"), dataStyle);

            // Dados da entrega
            setCellValue(row, 6, deliveryData.get("delivery_id"), dataStyle);
            setStatusCell(row, 7, deliveryData.get("delivery_status"), dataStyle);
            setCellValue(row, 8, deliveryData.get("project_name"), dataStyle);
            setCellValue(row, 9, deliveryData.get("pull_request"), dataStyle);
            setCellValue(row, 10, deliveryData.get("branch"), dataStyle);
            setCellValue(row, 11, deliveryData.get("script"), dataStyle);
            setCellValue(row, 12, deliveryData.get("notes"), dataStyle);

            // Datas da entrega (usando dateOnlyStyle para started_at e finished_at)
            setCellValue(row, 13, deliveryData.get("started_at"), dateOnlyStyle);
            setCellValue(row, 14, deliveryData.get("finished_at"), dateOnlyStyle);
            setCellValue(row, 15, deliveryData.get("delivery_created_at"), dateStyle);
            setCellValue(row, 16, deliveryData.get("delivery_updated_at"), dateStyle);
        }

        // Ajustar largura das colunas (17 colunas)
        setColumnWidths(sheet, new int[]{
            2500,  // ID Tarefa
            3500,  // Código da Tarefa
            8000,  // Título da Tarefa (maior)
            3500,  // Status da Tarefa
            3000,  // Qtd. Subtarefas
            6000,  // Solicitante
            3000,  // ID Entrega
            3500,  // Status da Entrega
            6000,  // Projeto
            8000,  // Link da entrega (Pull Request) - maior para URLs
            10000,  // Branch
            10000,  // Script (maior para texto)
            6000,  // Observações
            4000,  // Data de Início
            4000,  // Data de Fim
            6000,  // Data de Criação
            6000   // Data de Atualização
        });

        // Ajustar altura das linhas
        for (int i = 1; i <= data.size(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                row.setHeightInPoints(35); // Altura maior para acomodar script e URLs
            }
        }

        // Altura do cabeçalho
        headerRow.setHeightInPoints(40);

        // Aplicar filtros
        sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, data.size(), 0, headers.length - 1));

        // Congelar primeira linha (cabeçalho)
        sheet.createFreezePane(0, 1);

        // Converter para bytes
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }
}

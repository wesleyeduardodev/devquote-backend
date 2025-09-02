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
        
        // Estilos coloridos para cabeçalhos de tarefas e subtarefas
        CellStyle taskHeaderStyle = createColoredHeaderStyle(workbook, IndexedColors.PALE_BLUE.getIndex());
        CellStyle subtaskHeaderStyle = createColoredHeaderStyle(workbook, IndexedColors.LIGHT_GREEN.getIndex());

        // Definir cabeçalhos baseado no perfil do usuário
        String[] headers;
        if (canViewAmounts) {
            // ADMIN/MANAGER: Todas as colunas
            headers = new String[] {
                "ID", "Código", "Título", "Descrição", "Tipo",
                "Prioridade", "Solicitante", "Criado Por", "Atualizado Por",
                "Origem do Servidor", "Módulo do Sistema", "Link", "Link da Reunião",
                "Observações", "Valor da Tarefa", "Tem Subtarefas", "Tem Entrega",
                "Orçamento no Faturamento", "Data de Criação", "Data de Atualização",
                "Subtarefa ID", "Subtarefa Título", "Subtarefa Descrição",
                "Subtarefa Valor"
            };
        } else {
            // USER: Remove colunas sensíveis (Valor da Tarefa, Tem Orçamento, Orçamento no Faturamento, Subtarefa Valor)
            headers = new String[] {
                "ID", "Código", "Título", "Descrição", "Tipo",
                "Prioridade", "Solicitante", "Criado Por", "Atualizado Por",
                "Origem do Servidor", "Módulo do Sistema", "Link", "Link da Reunião",
                "Observações", "Tem Subtarefas", "Data de Criação", "Data de Atualização",
                "Subtarefa ID", "Subtarefa Título", "Subtarefa Descrição"
            };
        }

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            
            // Aplicar cores diferentes para colunas de tarefas vs subtarefas
            if (canViewAmounts) {
                // ADMIN/MANAGER: colunas 0-19 são tarefas (azul), 20-23 são subtarefas (verde)
                if (i <= 19) {
                    cell.setCellStyle(taskHeaderStyle);
                } else {
                    cell.setCellStyle(subtaskHeaderStyle);
                }
            } else {
                // USER: colunas 0-16 são tarefas (azul), 17-19 são subtarefas (verde)
                if (i <= 16) {
                    cell.setCellStyle(taskHeaderStyle);
                } else {
                    cell.setCellStyle(subtaskHeaderStyle);
                }
            }
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
                setCellValue(row, 4, taskData.get("task_type"), dataStyle);
                setPriorityCell(row, 5, taskData.get("task_priority"), dataStyle);
                setCellValue(row, 6, taskData.get("requester_name"), dataStyle);
                setCellValue(row, 7, taskData.get("created_by_user"), dataStyle);
                setCellValue(row, 8, taskData.get("updated_by_user"), dataStyle);
                setCellValue(row, 9, taskData.get("server_origin"), dataStyle);
                setCellValue(row, 10, taskData.get("system_module"), dataStyle);
                setCellValue(row, 11, taskData.get("task_link"), dataStyle);
                setCellValue(row, 12, taskData.get("meeting_link"), dataStyle);
                setCellValue(row, 13, taskData.get("task_notes"), dataStyle);
                setCellValue(row, 14, taskData.get("task_amount"), currencyStyle);
                setCellValue(row, 15, taskData.get("has_subtasks"), dataStyle);
                setCellValue(row, 16, taskData.get("has_delivery"), dataStyle);
                setCellValue(row, 17, taskData.get("has_quote_in_billing"), dataStyle);
                setCellValue(row, 18, taskData.get("task_created_at"), dateStyle);
                setCellValue(row, 19, taskData.get("task_updated_at"), dateStyle);
                setCellValue(row, 20, taskData.get("subtask_id"), dataStyle);
                setCellValue(row, 21, taskData.get("subtask_title"), dataStyle);
                setCellValue(row, 22, taskData.get("subtask_description"), dataStyle);
                setCellValue(row, 23, taskData.get("subtask_amount"), currencyStyle);
            } else {
                // USER: Remove colunas sensíveis
                setCellValue(row, 0, taskData.get("task_id"), dataStyle);
                setCellValue(row, 1, taskData.get("task_code"), dataStyle);
                setCellValue(row, 2, taskData.get("task_title"), dataStyle);
                setCellValue(row, 3, taskData.get("task_description"), dataStyle);
                setCellValue(row, 4, taskData.get("task_type"), dataStyle);
                setPriorityCell(row, 5, taskData.get("task_priority"), dataStyle);
                setCellValue(row, 6, taskData.get("requester_name"), dataStyle);
                setCellValue(row, 7, taskData.get("created_by_user"), dataStyle);
                setCellValue(row, 8, taskData.get("updated_by_user"), dataStyle);
                setCellValue(row, 9, taskData.get("server_origin"), dataStyle);
                setCellValue(row, 10, taskData.get("system_module"), dataStyle);
                setCellValue(row, 11, taskData.get("task_link"), dataStyle);
                setCellValue(row, 12, taskData.get("meeting_link"), dataStyle);
                setCellValue(row, 13, taskData.get("task_notes"), dataStyle);
                setCellValue(row, 14, taskData.get("has_subtasks"), dataStyle);
                setCellValue(row, 15, taskData.get("task_created_at"), dateStyle);
                setCellValue(row, 16, taskData.get("task_updated_at"), dateStyle);
                setCellValue(row, 17, taskData.get("subtask_id"), dataStyle);
                setCellValue(row, 18, taskData.get("subtask_title"), dataStyle);
                setCellValue(row, 19, taskData.get("subtask_description"), dataStyle);
            }
        }

        // Ajustar largura das colunas baseado no conteúdo
        if (canViewAmounts) {
            // ADMIN/MANAGER: 24 colunas
            setColumnWidths(sheet, new int[]{
                2500,  // ID
                3500,  // Código
                8000,  // Título (maior)
                10000, // Descrição (maior)
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
                3000,  // Tem Entrega
                4000,  // Orçamento no Faturamento
                6000,  // Data de Criação (aumentada para dd/mm/yyyy hh:mm:ss)
                6000,  // Data de Atualização (aumentada para dd/mm/yyyy hh:mm:ss)
                2500,  // Subtarefa ID
                8000,  // Subtarefa Título (maior)
                10000, // Subtarefa Descrição (maior)
                3500   // Subtarefa Valor
            });
        } else {
            // USER: 20 colunas (sem colunas de valores)
            setColumnWidths(sheet, new int[]{
                2500,  // ID
                3500,  // Código
                8000,  // Título (maior)
                10000, // Descrição (maior)
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
                10000  // Subtarefa Descrição (maior)
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
            "Valor Total da Tarefa", "Prioridade da Tarefa", "Solicitante",
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
            setPriorityCell(row, 6, quoteData.get("task_priority"), dataStyle);
            setCellValue(row, 7, quoteData.get("requester_name"), dataStyle);
            setCellValue(row, 8, quoteData.get("has_billing"), dataStyle);
            setCellValue(row, 9, quoteData.get("quote_total_amount"), currencyStyle);
            setCellValue(row, 10, quoteData.get("quote_created_at"), dateStyle);
            setCellValue(row, 11, quoteData.get("quote_updated_at"), dateStyle);
        }

        // Ajustar largura das colunas (12 colunas)
        setColumnWidths(sheet, new int[]{
            3000,  // ID Orçamento
            3500,  // Status do Orçamento
            2500,  // ID Tarefa
            3500,  // Código da Tarefa
            8000,  // Título da Tarefa (maior)
            4000,  // Valor Total da Tarefa
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

    private String translateDeliveryStatus(String status) {
        if (status == null) return "";
        return switch (status.toUpperCase()) {
            case "PENDING" -> "Pendente";
            case "DEVELOPMENT" -> "Desenvolvimento";
            case "DELIVERED" -> "Entregue";
            case "HOMOLOGATION" -> "Homologação";
            case "APPROVED" -> "Aprovado";
            case "REJECTED" -> "Rejeitado";
            case "PRODUCTION" -> "Produção";
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
        
        // Estilos coloridos para cabeçalhos de tarefas e entregas
        CellStyle taskHeaderStyle = createColoredHeaderStyle(workbook, IndexedColors.PALE_BLUE.getIndex());
        CellStyle deliveryHeaderStyle = createColoredHeaderStyle(workbook, IndexedColors.LIGHT_GREEN.getIndex());

        // Cabeçalhos reorganizados: Dados da Tarefa primeiro, depois Dados da Entrega (SEM ID Entrega e Qtd. Subtarefas)
        String[] headers = {
            "ID Tarefa", "Código da Tarefa", "Título da Tarefa", "Solicitante",
            "Status da Entrega", "Projeto", "Link da entrega (Pull Request)", "Branch",
            "Script", "Observações", "Data de Início", "Data de Fim", "Data de Criação", "Data de Atualização"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            
            // Aplicar cores diferentes: 0-3 são tarefas (azul), 4+ são entregas (verde)
            if (i <= 3) {
                cell.setCellStyle(taskHeaderStyle);
            } else {
                cell.setCellStyle(deliveryHeaderStyle);
            }
        }

        // Adicionar dados: Dados da Tarefa primeiro, depois Dados da Entrega
        int rowNum = 1;
        for (Map<String, Object> deliveryData : data) {
            Row row = sheet.createRow(rowNum++);

            // Dados da tarefa (SEM Qtd. Subtarefas)
            setCellValue(row, 0, deliveryData.get("task_id"), dataStyle);
            setCellValue(row, 1, deliveryData.get("task_code"), dataStyle);
            setCellValue(row, 2, deliveryData.get("task_title"), dataStyle);
            setCellValue(row, 3, deliveryData.get("requester_name"), dataStyle);

            // Dados da entrega - índices ajustados após remoção
            setStatusCell(row, 4, deliveryData.get("delivery_status"), dataStyle);
            setCellValue(row, 5, deliveryData.get("project_name"), dataStyle);
            setCellValue(row, 6, deliveryData.get("pull_request"), dataStyle);
            setCellValue(row, 7, deliveryData.get("branch"), dataStyle);
            setCellValue(row, 8, deliveryData.get("script"), dataStyle);
            setCellValue(row, 9, deliveryData.get("notes"), dataStyle);

            // Datas da entrega (usando dateOnlyStyle para started_at e finished_at)
            setCellValue(row, 10, deliveryData.get("started_at"), dateOnlyStyle);
            setCellValue(row, 11, deliveryData.get("finished_at"), dateOnlyStyle);
            setCellValue(row, 12, deliveryData.get("delivery_created_at"), dateStyle);
            setCellValue(row, 13, deliveryData.get("delivery_updated_at"), dateStyle);
        }

        // Ajustar largura das colunas (14 colunas - sem ID Entrega e Qtd. Subtarefas)
        setColumnWidths(sheet, new int[]{
            2500,  // ID Tarefa
            3500,  // Código da Tarefa
            8000,  // Título da Tarefa (maior)
            6000,  // Solicitante
            3500,  // Status da Entrega
            6000,  // Projeto
            8000,  // Link da entrega (Pull Request) - maior para URLs
            10000, // Branch
            10000, // Script (maior para texto)
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

    public byte[] generateBillingReport(List<Map<String, Object>> data) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Relatório de Faturamento");

        // Criar estilos
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);

        // Cabeçalhos: Faturamento -> Tarefa (sem orçamento e sem datas)
        String[] headers = {
            "Ano", "Mês", "Status do Faturamento", "ID Tarefa", "Código da Tarefa", 
            "Título da Tarefa", "Valor da Tarefa", "Qtd. Subtarefas", 
            "Solicitante"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Adicionar dados
        int rowNum = 1;
        for (Map<String, Object> billingData : data) {
            Row row = sheet.createRow(rowNum++);

            // Dados do faturamento
            setCellValue(row, 0, billingData.get("billing_year"), dataStyle);
            setCellValue(row, 1, billingData.get("billing_month"), dataStyle);
            setStatusCell(row, 2, billingData.get("billing_status"), dataStyle);

            // Dados da tarefa
            setCellValue(row, 3, billingData.get("task_id"), dataStyle);
            setCellValue(row, 4, billingData.get("task_code"), dataStyle);
            setCellValue(row, 5, billingData.get("task_title"), dataStyle);
            setCellValue(row, 6, billingData.get("task_amount"), currencyStyle);
            setCellValue(row, 7, billingData.get("subtasks_count"), dataStyle);
            setCellValue(row, 8, billingData.get("requester_name"), dataStyle);
        }

        // Ajustar largura das colunas (9 colunas)
        setColumnWidths(sheet, new int[]{
            2500,  // Ano
            2500,  // Mês
            4000,  // Status do Faturamento
            2500,  // ID Tarefa
            3500,  // Código da Tarefa
            8000,  // Título da Tarefa (maior)
            4000,  // Valor da Tarefa
            3000,  // Qtd. Subtarefas
            6000   // Solicitante
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

    public byte[] generateGeneralReport(List<Map<String, Object>> data) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Relatório Geral - Visão Completa");

        // Criar estilos com cores neutras e discretas para diferentes seções
        CellStyle taskHeaderStyle = createColoredHeaderStyle(workbook, IndexedColors.GREY_25_PERCENT.getIndex());
        CellStyle quoteHeaderStyle = createColoredHeaderStyle(workbook, IndexedColors.LAVENDER.getIndex());
        CellStyle deliveryHeaderStyle = createColoredHeaderStyle(workbook, IndexedColors.PALE_BLUE.getIndex());
        CellStyle billingHeaderStyle = createColoredHeaderStyle(workbook, IndexedColors.LEMON_CHIFFON.getIndex());

        // Estilos para dados
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);
        CellStyle dateOnlyStyle = createDateOnlyStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);

        // Headers organizados: Tarefas → Entregas → Faturamento
        String[] headers = {
            // TAREFAS (Cinza) - 15 colunas (inclui metadados da tarefa + status de entrega e faturamento)
            "ID Tarefa", "Código", "Título", "Descrição", "Prioridade", "Valor", "Solicitante", 
            "Data Criação", "Data Atualização", "Criado Por", "Atualizado Por", "Sistema Origem", "Módulo",
            "Tem Entrega", "Faturamento",
            
            // ENTREGAS (Azul Pálido) - 9 colunas - Pull Request reorganizado + notas
            "ID Entrega", "Status Entrega", "Projeto", "Link da entrega (Pull Request)", "Branch", "Script", "Notas", "Início Entrega", "Fim Entrega",
            
            // FATURAMENTO (Limão) - 3 colunas - No final
            "Ano Faturamento", "Mês Faturamento", "Status Faturamento"
        };

        Row headerRow = sheet.createRow(0);
        
        // Aplicar cores nos headers por seção - Estrutura corrigida (27 colunas)
        int colIndex = 0;
        
        // TAREFAS + METADADOS + STATUS (0-14) - Cinza
        for (int i = 0; i < 15; i++) {
            Cell cell = headerRow.createCell(colIndex++);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(taskHeaderStyle);
        }
        
        // ENTREGAS (15-23) - Azul Pálido
        for (int i = 15; i < 24; i++) {
            Cell cell = headerRow.createCell(colIndex++);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(deliveryHeaderStyle);
        }
        
        // FATURAMENTO (24-26) - Limão (no final)
        for (int i = 24; i < 27; i++) {
            Cell cell = headerRow.createCell(colIndex++);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(billingHeaderStyle);
        }

        // Adicionar dados
        int rowNum = 1;
        for (Map<String, Object> taskData : data) {
            Row row = sheet.createRow(rowNum++);

            colIndex = 0;
            
            // DADOS DA TAREFA + METADADOS + STATUS (0-14)
            setCellValue(row, colIndex++, taskData.get("task_id"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("task_code"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("task_title"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("task_description"), dataStyle);
            setPriorityCell(row, colIndex++, taskData.get("task_priority"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("task_amount"), currencyStyle);
            setCellValue(row, colIndex++, taskData.get("requester_name"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("task_created_at"), dateStyle);
            setCellValue(row, colIndex++, taskData.get("task_updated_at"), dateStyle);
            setCellValue(row, colIndex++, taskData.get("created_by_name"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("updated_by_name"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("task_server_origin"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("task_system_module"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("has_delivery"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("has_quote_in_billing"), dataStyle);
            
            // DADOS DE ENTREGAS (15-23) - Pull Request reorganizado + notas
            setCellValue(row, colIndex++, taskData.get("delivery_id"), dataStyle);
            setStatusCell(row, colIndex++, taskData.get("delivery_status"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("project_name"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("delivery_pull_request"), dataStyle); // Link da entrega
            setCellValue(row, colIndex++, taskData.get("delivery_branch"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("delivery_script"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("delivery_notes"), dataStyle); // Nova coluna de notas
            setCellValue(row, colIndex++, taskData.get("delivery_started_at"), dateOnlyStyle);
            setCellValue(row, colIndex++, taskData.get("delivery_finished_at"), dateOnlyStyle);
            
            // DADOS DE FATURAMENTO (24-26) - No final
            setCellValue(row, colIndex++, taskData.get("billing_year"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("billing_month"), dataStyle);
            setStatusCell(row, colIndex++, taskData.get("billing_status"), dataStyle);
        }

        // Ajustar larguras das colunas (27 colunas total) - Estrutura corrigida
        setColumnWidths(sheet, new int[]{
            // TAREFAS + METADADOS + STATUS (15 colunas)
            2500, 3500, 10000, 12000, 3000, 4000, 6000, 6000, 6000, 4000, 4000, 4000, 4000, 3000, 4000,
            // ENTREGAS (9 colunas) - Pull Request reorganizado + notas
            3000, 3500, 6000, 10000, 8000, 8000, 6000, 4000, 4000,
            // FATURAMENTO (3 colunas) - No final
            2500, 2500, 4000
        });

        // Ajustar altura das linhas
        for (int i = 1; i <= data.size(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                row.setHeightInPoints(40); // Altura maior para acomodar conteúdo extenso
            }
        }

        // Altura do cabeçalho
        headerRow.setHeightInPoints(45);

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

    public byte[] generateGeneralReportForUser(List<Map<String, Object>> data) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Relatório Geral - Visão User");

        // Criar estilos com cores neutras para diferentes seções (sem dados financeiros)
        CellStyle taskHeaderStyle = createColoredHeaderStyle(workbook, IndexedColors.GREY_25_PERCENT.getIndex());
        CellStyle deliveryHeaderStyle = createColoredHeaderStyle(workbook, IndexedColors.PALE_BLUE.getIndex());

        // Estilos para dados
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);
        CellStyle dateOnlyStyle = createDateOnlyStyle(workbook);

        // Headers organizados: Tarefas → Entregas (sem Orçamentos e Faturamento)
        String[] headers = {
            // TAREFAS (Cinza) - 14 colunas (SEM coluna Valor + status de entrega e faturamento)
            "ID Tarefa", "Código", "Título", "Descrição", "Prioridade", "Solicitante", 
            "Data Criação", "Data Atualização", "Criado Por", "Atualizado Por", "Sistema Origem", "Módulo",
            "Tem Entrega", "Faturamento",
            
            // ENTREGAS (Azul Pálido) - 9 colunas
            "ID Entrega", "Status Entrega", "Projeto", "Link da entrega (Pull Request)", "Branch", "Script", "Notas", "Início Entrega", "Fim Entrega"
        };

        Row headerRow = sheet.createRow(0);
        
        // Aplicar cores nos headers por seção - 23 colunas total
        int colIndex = 0;
        
        // TAREFAS (0-13) - Cinza - SEM Valor + Status de entrega e faturamento
        for (int i = 0; i < 14; i++) {
            Cell cell = headerRow.createCell(colIndex++);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(taskHeaderStyle);
        }
        
        // ENTREGAS (14-22) - Azul Pálido
        for (int i = 14; i < 23; i++) {
            Cell cell = headerRow.createCell(colIndex++);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(deliveryHeaderStyle);
        }

        // Adicionar dados
        int rowNum = 1;
        for (Map<String, Object> taskData : data) {
            Row row = sheet.createRow(rowNum++);

            colIndex = 0;
            
            // DADOS DA TAREFA (0-13) - SEM Valor + Status de entrega e faturamento
            setCellValue(row, colIndex++, taskData.get("task_id"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("task_code"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("task_title"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("task_description"), dataStyle);
            setPriorityCell(row, colIndex++, taskData.get("task_priority"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("requester_name"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("task_created_at"), dateStyle);
            setCellValue(row, colIndex++, taskData.get("task_updated_at"), dateStyle);
            setCellValue(row, colIndex++, taskData.get("created_by_name"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("updated_by_name"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("task_server_origin"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("task_system_module"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("has_delivery"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("has_quote_in_billing"), dataStyle);
            
            // DADOS DE ENTREGAS (14-22)
            setCellValue(row, colIndex++, taskData.get("delivery_id"), dataStyle);
            setStatusCell(row, colIndex++, taskData.get("delivery_status"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("project_name"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("delivery_pull_request"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("delivery_branch"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("delivery_script"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("delivery_notes"), dataStyle); // Nova coluna de notas
            setCellValue(row, colIndex++, taskData.get("delivery_started_at"), dateOnlyStyle);
            setCellValue(row, colIndex++, taskData.get("delivery_finished_at"), dateOnlyStyle);
        }

        // Ajustar larguras das colunas (23 colunas total)
        setColumnWidths(sheet, new int[]{
            // TAREFAS (14 colunas) - SEM Valor + Status de entrega e faturamento
            2500, 3500, 10000, 12000, 3000, 6000, 6000, 6000, 4000, 4000, 4000, 4000, 3000, 4000,
            // ENTREGAS (9 colunas)
            3000, 3500, 6000, 10000, 8000, 8000, 6000, 4000, 4000
        });

        // Ajustar altura das linhas
        for (int i = 1; i <= data.size(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                row.setHeightInPoints(40);
            }
        }

        // Altura do cabeçalho
        headerRow.setHeightInPoints(45);

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

    private CellStyle createColoredHeaderStyle(Workbook workbook, short colorIndex) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex()); // Texto preto para melhor contraste
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        
        // Fundo colorido suave
        style.setFillForegroundColor(colorIndex);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Bordas
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
        
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        
        return style;
    }
}

package br.com.devquote.utils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class ExcelReportUtils {

    public byte[] generateTasksOnlyReport(List<Map<String, Object>> data, boolean canViewAmounts) throws IOException {

        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Relatório de Tarefas");

        CellStyle dataStyle = createDataStyle(workbook);

        CellStyle currencyStyle = createCurrencyStyle(workbook);

        CellStyle taskHeaderStyle = createColoredHeaderStyle(workbook, IndexedColors.PALE_BLUE.getIndex());

        String[] headers;
        if (canViewAmounts) {
            headers = new String[]{
                    "ID", "Fluxo", "Código", "Título", "Descrição", "Tipo", "Ambiente",
                    "Prioridade", "Solicitante", "Criado Por", "Atualizado Por",
                    "Origem do Servidor", "Módulo do Sistema", "Link", "Link da Reunião",
                    "Valor da Tarefa", "Tem Subtarefas", "Tem Entrega", "Orçamento no Faturamento",
                    "Data Criação", "Data Atualização"
            };
        } else {
            headers = new String[]{
                    "ID", "Fluxo", "Código", "Título", "Descrição", "Tipo", "Ambiente",
                    "Prioridade", "Solicitante", "Criado Por", "Atualizado Por",
                    "Origem do Servidor", "Módulo do Sistema", "Link", "Link da Reunião",
                    "Tem Subtarefas", "Tem Entrega", "Orçamento no Faturamento",
                    "Data Criação", "Data Atualização"
            };
        }

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(taskHeaderStyle);
        }

        int rowNum = 1;
        for (Map<String, Object> taskData : data) {
            Row row = sheet.createRow(rowNum++);

            if (canViewAmounts) {
                setCellValue(row, 0, taskData.get("task_id"), dataStyle);
                setFlowTypeCell(row, 1, taskData.get("task_flow_type"), dataStyle);
                setCellValue(row, 2, taskData.get("task_code"), dataStyle);
                setCellValue(row, 3, taskData.get("task_title"), dataStyle);
                setCellValue(row, 4, taskData.get("task_description"), dataStyle);
                setTaskTypeCell(row, 5, taskData.get("task_type"), dataStyle);
                setCellValue(row, 6, taskData.get("task_environment"), dataStyle);
                setPriorityCell(row, 7, taskData.get("task_priority"), dataStyle);
                setCellValue(row, 8, taskData.get("requester_name"), dataStyle);
                setCellValue(row, 9, taskData.get("created_by_user"), dataStyle);
                setCellValue(row, 10, taskData.get("updated_by_user"), dataStyle);
                setCellValue(row, 11, taskData.get("server_origin"), dataStyle);
                setCellValue(row, 12, taskData.get("system_module"), dataStyle);
                setCellValue(row, 13, taskData.get("task_link"), dataStyle);
                setCellValue(row, 14, taskData.get("meeting_link"), dataStyle);
                setCellValue(row, 15, taskData.get("task_amount"), currencyStyle);
                setCellValue(row, 16, taskData.get("has_subtasks"), dataStyle);
                setCellValue(row, 17, taskData.get("has_delivery"), dataStyle);
                setCellValue(row, 18, taskData.get("has_quote_in_billing"), dataStyle);
                setDateTimeCell(row, 19, taskData.get("task_created_at"), dataStyle);
                setDateTimeCell(row, 20, taskData.get("task_updated_at"), dataStyle);
            } else {
                setCellValue(row, 0, taskData.get("task_id"), dataStyle);
                setFlowTypeCell(row, 1, taskData.get("task_flow_type"), dataStyle);
                setCellValue(row, 2, taskData.get("task_code"), dataStyle);
                setCellValue(row, 3, taskData.get("task_title"), dataStyle);
                setCellValue(row, 4, taskData.get("task_description"), dataStyle);
                setTaskTypeCell(row, 5, taskData.get("task_type"), dataStyle);
                setCellValue(row, 6, taskData.get("task_environment"), dataStyle);
                setPriorityCell(row, 7, taskData.get("task_priority"), dataStyle);
                setCellValue(row, 8, taskData.get("requester_name"), dataStyle);
                setCellValue(row, 9, taskData.get("created_by_user"), dataStyle);
                setCellValue(row, 10, taskData.get("updated_by_user"), dataStyle);
                setCellValue(row, 11, taskData.get("server_origin"), dataStyle);
                setCellValue(row, 12, taskData.get("system_module"), dataStyle);
                setCellValue(row, 13, taskData.get("task_link"), dataStyle);
                setCellValue(row, 14, taskData.get("meeting_link"), dataStyle);
                setCellValue(row, 15, taskData.get("has_subtasks"), dataStyle);
                setCellValue(row, 16, taskData.get("has_delivery"), dataStyle);
                setCellValue(row, 17, taskData.get("has_quote_in_billing"), dataStyle);
                setDateTimeCell(row, 18, taskData.get("task_created_at"), dataStyle);
                setDateTimeCell(row, 19, taskData.get("task_updated_at"), dataStyle);
            }
        }

        if (canViewAmounts) {
            setColumnWidths(sheet, new int[]{
                    2500, 4000, 3500, 8000, 10000, 3500, 4000, 3000, 6000, 4000, 4000,
                    4000, 4000, 8000, 8000, 3500, 3000, 3000, 4000, 4500, 4500
            });
        } else {
            setColumnWidths(sheet, new int[]{
                    2500, 4000, 3500, 8000, 10000, 3500, 4000, 3000, 6000, 4000, 4000,
                    4000, 4000, 8000, 8000, 3000, 3000, 4000, 4500, 4500
            });
        }

        for (int i = 1; i <= data.size(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                row.setHeightInPoints(30);
            }
        }

        headerRow.setHeightInPoints(35);

        sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, data.size(), 0, headers.length - 1));

        sheet.createFreezePane(0, 1);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    public byte[] generateTasksReport(List<Map<String, Object>> data, boolean canViewAmounts) throws IOException {

        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Relatório de Tarefas");

        CellStyle dataStyle = createDataStyle(workbook);

        CellStyle currencyStyle = createCurrencyStyle(workbook);

        CellStyle taskHeaderStyle = createColoredHeaderStyle(workbook, IndexedColors.PALE_BLUE.getIndex());

        CellStyle subtaskHeaderStyle = createColoredHeaderStyle(workbook, IndexedColors.LIGHT_GREEN.getIndex());

        String[] headers;
        if (canViewAmounts) {
            headers = new String[]{
                    "ID", "Fluxo", "Código", "Título", "Descrição", "Tipo", "Ambiente",
                    "Prioridade", "Solicitante", "Origem do Servidor", "Módulo do Sistema",
                    "Link", "Link da Reunião", "Valor da Tarefa", "Tem Subtarefas",
                    "Tem Entrega", "Orçamento no Faturamento",
                    "Subtarefa ID", "Subtarefa Título", "Subtarefa Descrição",
                    "Subtarefa Valor"
            };
        } else {
            headers = new String[]{
                    "ID", "Fluxo", "Código", "Título", "Descrição", "Tipo", "Ambiente",
                    "Prioridade", "Solicitante", "Origem do Servidor", "Módulo do Sistema",
                    "Link", "Link da Reunião", "Tem Subtarefas",
                    "Subtarefa ID", "Subtarefa Título", "Subtarefa Descrição"
            };
        }

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);

            if (canViewAmounts) {
                if (i <= 16) {
                    cell.setCellStyle(taskHeaderStyle);
                } else {
                    cell.setCellStyle(subtaskHeaderStyle);
                }
            } else {
                if (i <= 13) {
                    cell.setCellStyle(taskHeaderStyle);
                } else {
                    cell.setCellStyle(subtaskHeaderStyle);
                }
            }
        }

        int rowNum = 1;
        for (Map<String, Object> taskData : data) {
            Row row = sheet.createRow(rowNum++);

            if (canViewAmounts) {
                setCellValue(row, 0, taskData.get("task_id"), dataStyle);
                setFlowTypeCell(row, 1, taskData.get("task_flow_type"), dataStyle);
                setCellValue(row, 2, taskData.get("task_code"), dataStyle);
                setCellValue(row, 3, taskData.get("task_title"), dataStyle);
                setCellValue(row, 4, taskData.get("task_description"), dataStyle);
                setTaskTypeCell(row, 5, taskData.get("task_type"), dataStyle);
                setCellValue(row, 6, taskData.get("task_environment"), dataStyle);
                setPriorityCell(row, 7, taskData.get("task_priority"), dataStyle);
                setCellValue(row, 8, taskData.get("requester_name"), dataStyle);
                setCellValue(row, 9, taskData.get("server_origin"), dataStyle);
                setCellValue(row, 10, taskData.get("system_module"), dataStyle);
                setCellValue(row, 11, taskData.get("task_link"), dataStyle);
                setCellValue(row, 12, taskData.get("meeting_link"), dataStyle);
                setCellValue(row, 13, taskData.get("task_amount"), currencyStyle);
                setCellValue(row, 14, taskData.get("has_subtasks"), dataStyle);
                setCellValue(row, 15, taskData.get("has_delivery"), dataStyle);
                setCellValue(row, 16, taskData.get("has_quote_in_billing"), dataStyle);
                setCellValue(row, 17, taskData.get("subtask_id"), dataStyle);
                setCellValue(row, 18, taskData.get("subtask_title"), dataStyle);
                setCellValue(row, 19, taskData.get("subtask_description"), dataStyle);
                setCellValue(row, 20, taskData.get("subtask_amount"), currencyStyle);
            } else {
                setCellValue(row, 0, taskData.get("task_id"), dataStyle);
                setFlowTypeCell(row, 1, taskData.get("task_flow_type"), dataStyle);
                setCellValue(row, 2, taskData.get("task_code"), dataStyle);
                setCellValue(row, 3, taskData.get("task_title"), dataStyle);
                setCellValue(row, 4, taskData.get("task_description"), dataStyle);
                setTaskTypeCell(row, 5, taskData.get("task_type"), dataStyle);
                setCellValue(row, 6, taskData.get("task_environment"), dataStyle);
                setPriorityCell(row, 7, taskData.get("task_priority"), dataStyle);
                setCellValue(row, 8, taskData.get("requester_name"), dataStyle);
                setCellValue(row, 9, taskData.get("server_origin"), dataStyle);
                setCellValue(row, 10, taskData.get("system_module"), dataStyle);
                setCellValue(row, 11, taskData.get("task_link"), dataStyle);
                setCellValue(row, 12, taskData.get("meeting_link"), dataStyle);
                setCellValue(row, 13, taskData.get("has_subtasks"), dataStyle);
                setCellValue(row, 14, taskData.get("subtask_id"), dataStyle);
                setCellValue(row, 15, taskData.get("subtask_title"), dataStyle);
                setCellValue(row, 16, taskData.get("subtask_description"), dataStyle);
            }
        }

        if (canViewAmounts) {
            setColumnWidths(sheet, new int[]{
                    2500, 4000, 3500, 8000, 10000, 3500, 4000, 3000, 6000, 4000, 4000,
                    8000, 8000, 3500, 3000, 3000, 4000, 2500, 8000, 10000, 3500
            });
        } else {
            setColumnWidths(sheet, new int[]{
                    2500, 4000, 3500, 8000, 10000, 3500, 4000, 3000, 6000, 4000, 4000,
                    8000, 8000, 3000, 2500, 8000, 10000
            });
        }

        for (int i = 1; i <= data.size(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                row.setHeightInPoints(30);
            }
        }

        headerRow.setHeightInPoints(35);

        sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, data.size(), 0, headers.length - 1));

        sheet.createFreezePane(0, 1);

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

        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);

        style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);

        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setWrapText(true);

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
            LocalDateTime dateTime = (LocalDateTime) value;
            java.util.Date date = java.sql.Timestamp.valueOf(dateTime);
            cell.setCellValue(date);
        } else if (value instanceof java.sql.Timestamp) {
            cell.setCellValue((java.sql.Timestamp) value);
        } else if (value instanceof java.sql.Date) {
            cell.setCellValue((java.sql.Date) value);
        } else if (value instanceof java.util.Date) {
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

    private void setTaskTypeCell(Row row, int columnIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellStyle(style);

        if (value == null) {
            cell.setCellValue("");
        } else {
            String taskType = value.toString();
            String translatedTaskType = translateTaskType(taskType);
            cell.setCellValue(translatedTaskType);
        }
    }

    private void setFlowTypeCell(Row row, int columnIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellStyle(style);

        if (value == null) {
            cell.setCellValue("");
        } else {
            String flowType = value.toString();
            String translatedFlowType = translateFlowType(flowType);
            cell.setCellValue(translatedFlowType);
        }
    }

    private void setEnvironmentCell(Row row, int columnIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellStyle(style);

        if (value == null) {
            cell.setCellValue("");
        } else {
            String environment = value.toString();
            String translatedEnvironment = translateEnvironment(environment);
            cell.setCellValue(translatedEnvironment);
        }
    }

    private void setDateTimeCell(Row row, int columnIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellStyle(style);

        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof java.sql.Timestamp) {
            java.sql.Timestamp timestamp = (java.sql.Timestamp) value;
            LocalDateTime dateTime = timestamp.toLocalDateTime();
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            cell.setCellValue(dateTime.format(formatter));
        } else if (value instanceof LocalDateTime) {
            LocalDateTime dateTime = (LocalDateTime) value;
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            cell.setCellValue(dateTime.format(formatter));
        } else {
            cell.setCellValue(value.toString());
        }
    }

    private void setMonthNameCell(Row row, int columnIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellStyle(style);

        if (value == null) {
            cell.setCellValue("");
        } else {
            int month = Integer.parseInt(value.toString());
            String monthName = translateMonth(month);
            cell.setCellValue(monthName);
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
            case "PENDENTE" -> "Pendente";
            case "FATURADO" -> "Faturado";
            case "PAGO" -> "Pago";
            case "ATRASADO" -> "Atrasado";
            case "CANCELADO" -> "Cancelado";
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
            case "CANCELLED" -> "Cancelado";
            default -> status;
        };
    }

    private void setDeliveryStatusCell(Row row, int cellIndex, Object status, CellStyle style) {
        Cell cell = row.createCell(cellIndex);
        String translatedStatus = translateDeliveryStatus(status != null ? status.toString() : "");
        cell.setCellValue(translatedStatus);
        cell.setCellStyle(style);
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

    private String translateTaskType(String taskType) {
        if (taskType == null) return "";
        return switch (taskType.toUpperCase()) {
            case "BACKUP" -> "Backup";
            case "DEPLOY" -> "Deploy";
            case "LOGS" -> "Logs";
            case "DATABASE_APPLICATION" -> "Aplicação de Banco";
            case "NEW_SERVER" -> "Novo Servidor";
            case "MONITORING" -> "Monitoramento";
            case "SUPPORT" -> "Suporte";
            case "BUG" -> "Bug";
            case "ENHANCEMENT" -> "Melhoria";
            case "NEW_FEATURE" -> "Nova Funcionalidade";
            default -> taskType;
        };
    }

    private String translateFlowType(String flowType) {
        if (flowType == null) return "";
        return switch (flowType.toUpperCase()) {
            case "DESENVOLVIMENTO" -> "Desenvolvimento";
            case "OPERACIONAL" -> "Operacional";
            default -> flowType;
        };
    }

    private String translateEnvironment(String environment) {
        if (environment == null) return "";
        return switch (environment.toUpperCase()) {
            case "DESENVOLVIMENTO" -> "Desenvolvimento";
            case "HOMOLOGACAO" -> "Homologação";
            case "PRODUCAO" -> "Produção";
            default -> environment;
        };
    }

    private String translateMonth(int month) {
        return switch (month) {
            case 1 -> "Janeiro";
            case 2 -> "Fevereiro";
            case 3 -> "Março";
            case 4 -> "Abril";
            case 5 -> "Maio";
            case 6 -> "Junho";
            case 7 -> "Julho";
            case 8 -> "Agosto";
            case 9 -> "Setembro";
            case 10 -> "Outubro";
            case 11 -> "Novembro";
            case 12 -> "Dezembro";
            default -> "";
        };
    }

    public byte[] generateDeliveriesReport(List<Map<String, Object>> data, boolean canViewAmounts) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Relatório de Entregas");

        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        CellStyle taskHeaderStyle = createColoredHeaderStyle(workbook, IndexedColors.PALE_BLUE.getIndex());
        CellStyle deliveryHeaderStyle = createColoredHeaderStyle(workbook, IndexedColors.LIGHT_GREEN.getIndex());
        CellStyle itemHeaderStyle = createColoredHeaderStyle(workbook, IndexedColors.LIGHT_YELLOW.getIndex());

        String[] headers;
        if (canViewAmounts) {
            headers = new String[]{
                    "ID Tarefa", "Código da Tarefa", "Título da Tarefa", "Valor da Tarefa", "Tipo de Tarefa", "Ambiente", "Qtd. Subtarefas", "Solicitante",
                    "Status Geral da Entrega", "Observações da Entrega", "Data Início Entrega", "Data Fim Entrega",
                    "Projeto/Repositório", "Status do Item", "Branch", "Branch Origem",
                    "Pull Request", "Observações do Item", "Data Início Item", "Data Fim Item"
            };
        } else {
            headers = new String[]{
                    "ID Tarefa", "Código da Tarefa", "Título da Tarefa", "Tipo de Tarefa", "Ambiente", "Qtd. Subtarefas", "Solicitante",
                    "Status Geral da Entrega", "Observações da Entrega", "Data Início Entrega", "Data Fim Entrega",
                    "Projeto/Repositório", "Status do Item", "Branch", "Branch Origem",
                    "Pull Request", "Observações do Item", "Data Início Item", "Data Fim Item"
            };
        }

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);

            if (canViewAmounts) {
                if (i <= 7) {
                    cell.setCellStyle(taskHeaderStyle);
                } else if (i >= 8 && i <= 11) {
                    cell.setCellStyle(deliveryHeaderStyle);
                } else {
                    cell.setCellStyle(itemHeaderStyle);
                }
            } else {
                if (i <= 6) {
                    cell.setCellStyle(taskHeaderStyle);
                } else if (i >= 7 && i <= 10) {
                    cell.setCellStyle(deliveryHeaderStyle);
                } else {
                    cell.setCellStyle(itemHeaderStyle);
                }
            }
        }

        int rowNum = 1;
        for (Map<String, Object> deliveryData : data) {
            Row row = sheet.createRow(rowNum++);

            if (canViewAmounts) {
                setCellValue(row, 0, deliveryData.get("task_id"), dataStyle);
                setCellValue(row, 1, deliveryData.get("task_code"), dataStyle);
                setCellValue(row, 2, deliveryData.get("task_title"), dataStyle);
                setCellValue(row, 3, deliveryData.get("task_amount"), currencyStyle);
                setTaskTypeCell(row, 4, deliveryData.get("task_type"), dataStyle);
                setCellValue(row, 5, deliveryData.get("task_environment"), dataStyle);
                setCellValue(row, 6, deliveryData.get("subtasks_count"), dataStyle);
                setCellValue(row, 7, deliveryData.get("requester_name"), dataStyle);

                setDeliveryStatusCell(row, 8, deliveryData.get("delivery_status"), dataStyle);
                setCellValue(row, 9, deliveryData.get("delivery_notes"), dataStyle);
                setCellValue(row, 10, deliveryData.get("delivery_started_at"), dateStyle);
                setCellValue(row, 11, deliveryData.get("delivery_finished_at"), dateStyle);

                setCellValue(row, 12, deliveryData.get("project_name"), dataStyle);
                setDeliveryStatusCell(row, 13, deliveryData.get("item_status"), dataStyle);
                setCellValue(row, 14, deliveryData.get("item_branch"), dataStyle);
                setCellValue(row, 15, deliveryData.get("item_source_branch"), dataStyle);
                setCellValue(row, 16, deliveryData.get("item_pull_request"), dataStyle);
                setCellValue(row, 17, deliveryData.get("item_notes"), dataStyle);
                setCellValue(row, 18, deliveryData.get("item_started_at"), dateStyle);
                setCellValue(row, 19, deliveryData.get("item_finished_at"), dateStyle);
            } else {
                setCellValue(row, 0, deliveryData.get("task_id"), dataStyle);
                setCellValue(row, 1, deliveryData.get("task_code"), dataStyle);
                setCellValue(row, 2, deliveryData.get("task_title"), dataStyle);
                setTaskTypeCell(row, 3, deliveryData.get("task_type"), dataStyle);
                setCellValue(row, 4, deliveryData.get("task_environment"), dataStyle);
                setCellValue(row, 5, deliveryData.get("subtasks_count"), dataStyle);
                setCellValue(row, 6, deliveryData.get("requester_name"), dataStyle);

                setDeliveryStatusCell(row, 7, deliveryData.get("delivery_status"), dataStyle);
                setCellValue(row, 8, deliveryData.get("delivery_notes"), dataStyle);
                setCellValue(row, 9, deliveryData.get("delivery_started_at"), dateStyle);
                setCellValue(row, 10, deliveryData.get("delivery_finished_at"), dateStyle);

                setCellValue(row, 11, deliveryData.get("project_name"), dataStyle);
                setDeliveryStatusCell(row, 12, deliveryData.get("item_status"), dataStyle);
                setCellValue(row, 13, deliveryData.get("item_branch"), dataStyle);
                setCellValue(row, 14, deliveryData.get("item_source_branch"), dataStyle);
                setCellValue(row, 15, deliveryData.get("item_pull_request"), dataStyle);
                setCellValue(row, 16, deliveryData.get("item_notes"), dataStyle);
                setCellValue(row, 17, deliveryData.get("item_started_at"), dateStyle);
                setCellValue(row, 18, deliveryData.get("item_finished_at"), dateStyle);
            }
        }

        if (canViewAmounts) {
            setColumnWidths(sheet, new int[]{
                    2500, 3500, 8000, 3500, 4000, 4000, 3000, 6000, 4000, 7000, 6500, 6500, 6000, 3500, 5000, 5000, 8000, 6000, 6500, 6500
            });
        } else {
            setColumnWidths(sheet, new int[]{
                    2500, 3500, 8000, 4000, 4000, 3000, 6000, 4000, 7000, 6500, 6500, 6000, 3500, 5000, 5000, 8000, 6000, 6500, 6500
            });
        }

        for (int i = 1; i <= data.size(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                row.setHeightInPoints(35);
            }
        }

        headerRow.setHeightInPoints(40);

        sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, data.size(), 0, headers.length - 1));

        sheet.createFreezePane(0, 1);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    public byte[] generateOperationalDeliveriesReport(List<Map<String, Object>> data, boolean canViewAmounts) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Relatório de Entregas Operacionais");

        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        CellStyle taskHeaderStyle = createColoredHeaderStyle(workbook, IndexedColors.PALE_BLUE.getIndex());
        CellStyle deliveryHeaderStyle = createColoredHeaderStyle(workbook, IndexedColors.LIGHT_GREEN.getIndex());
        CellStyle itemHeaderStyle = createColoredHeaderStyle(workbook, IndexedColors.LIGHT_ORANGE.getIndex());

        String[] headers;
        if (canViewAmounts) {
            headers = new String[]{
                    "ID Tarefa", "Código da Tarefa", "Título da Tarefa", "Valor da Tarefa", "Tipo de Tarefa", "Ambiente", "Qtd. Subtarefas", "Solicitante",
                    "Status Geral da Entrega", "Observações da Entrega", "Data Início Entrega", "Data Fim Entrega",
                    "Título do Item", "Descrição do Item", "Status do Item",
                    "Data Início Item", "Data Fim Item"
            };
        } else {
            headers = new String[]{
                    "ID Tarefa", "Código da Tarefa", "Título da Tarefa", "Tipo de Tarefa", "Ambiente", "Qtd. Subtarefas", "Solicitante",
                    "Status Geral da Entrega", "Observações da Entrega", "Data Início Entrega", "Data Fim Entrega",
                    "Título do Item", "Descrição do Item", "Status do Item",
                    "Data Início Item", "Data Fim Item"
            };
        }

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);

            if (canViewAmounts) {
                if (i <= 7) {
                    cell.setCellStyle(taskHeaderStyle);
                } else if (i >= 8 && i <= 11) {
                    cell.setCellStyle(deliveryHeaderStyle);
                } else {
                    cell.setCellStyle(itemHeaderStyle);
                }
            } else {
                if (i <= 6) {
                    cell.setCellStyle(taskHeaderStyle);
                } else if (i >= 7 && i <= 10) {
                    cell.setCellStyle(deliveryHeaderStyle);
                } else {
                    cell.setCellStyle(itemHeaderStyle);
                }
            }
        }

        int rowNum = 1;
        for (Map<String, Object> deliveryData : data) {
            Row row = sheet.createRow(rowNum++);

            if (canViewAmounts) {
                setCellValue(row, 0, deliveryData.get("task_id"), dataStyle);
                setCellValue(row, 1, deliveryData.get("task_code"), dataStyle);
                setCellValue(row, 2, deliveryData.get("task_title"), dataStyle);
                setCellValue(row, 3, deliveryData.get("task_amount"), currencyStyle);
                setTaskTypeCell(row, 4, deliveryData.get("task_type"), dataStyle);
                setCellValue(row, 5, deliveryData.get("task_environment"), dataStyle);
                setCellValue(row, 6, deliveryData.get("subtasks_count"), dataStyle);
                setCellValue(row, 7, deliveryData.get("requester_name"), dataStyle);

                setDeliveryStatusCell(row, 8, deliveryData.get("delivery_status"), dataStyle);
                setCellValue(row, 9, deliveryData.get("delivery_notes"), dataStyle);
                setCellValue(row, 10, deliveryData.get("delivery_started_at"), dateStyle);
                setCellValue(row, 11, deliveryData.get("delivery_finished_at"), dateStyle);

                setCellValue(row, 12, deliveryData.get("item_title"), dataStyle);
                setCellValue(row, 13, deliveryData.get("item_description"), dataStyle);
                setOperationalItemStatusCell(row, 14, deliveryData.get("item_status"), dataStyle);
                setCellValue(row, 15, deliveryData.get("item_started_at"), dateStyle);
                setCellValue(row, 16, deliveryData.get("item_finished_at"), dateStyle);
            } else {
                setCellValue(row, 0, deliveryData.get("task_id"), dataStyle);
                setCellValue(row, 1, deliveryData.get("task_code"), dataStyle);
                setCellValue(row, 2, deliveryData.get("task_title"), dataStyle);
                setTaskTypeCell(row, 3, deliveryData.get("task_type"), dataStyle);
                setCellValue(row, 4, deliveryData.get("task_environment"), dataStyle);
                setCellValue(row, 5, deliveryData.get("subtasks_count"), dataStyle);
                setCellValue(row, 6, deliveryData.get("requester_name"), dataStyle);

                setDeliveryStatusCell(row, 7, deliveryData.get("delivery_status"), dataStyle);
                setCellValue(row, 8, deliveryData.get("delivery_notes"), dataStyle);
                setCellValue(row, 9, deliveryData.get("delivery_started_at"), dateStyle);
                setCellValue(row, 10, deliveryData.get("delivery_finished_at"), dateStyle);

                setCellValue(row, 11, deliveryData.get("item_title"), dataStyle);
                setCellValue(row, 12, deliveryData.get("item_description"), dataStyle);
                setOperationalItemStatusCell(row, 13, deliveryData.get("item_status"), dataStyle);
                setCellValue(row, 14, deliveryData.get("item_started_at"), dateStyle);
                setCellValue(row, 15, deliveryData.get("item_finished_at"), dateStyle);
            }
        }

        if (canViewAmounts) {
            setColumnWidths(sheet, new int[]{
                    2500, 3500, 8000, 3500, 4000, 4000, 3000, 6000, 4000, 7000, 6500, 6500, 7000, 9000, 3500, 6500, 6500
            });
        } else {
            setColumnWidths(sheet, new int[]{
                    2500, 3500, 8000, 4000, 4000, 3000, 6000, 4000, 7000, 6500, 6500, 7000, 9000, 3500, 6500, 6500
            });
        }

        for (int i = 1; i <= data.size(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                row.setHeightInPoints(35);
            }
        }

        headerRow.setHeightInPoints(40);

        sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, data.size(), 0, headers.length - 1));

        sheet.createFreezePane(0, 1);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    private void setOperationalItemStatusCell(Row row, int cellIndex, Object value, CellStyle defaultStyle) {
        if (value == null) {
            Cell cell = row.createCell(cellIndex);
            cell.setCellValue("");
            cell.setCellStyle(defaultStyle);
            return;
        }

        String status = value.toString();
        Cell cell = row.createCell(cellIndex);
        cell.setCellValue(status.equals("PENDING") ? "Pendente" : "Entregue");
        cell.setCellStyle(defaultStyle);
    }

    public byte[] generateBillingReport(List<Map<String, Object>> data) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Relatório de Faturamento");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);

        String[] headers = {
                "Ano", "Mês", "Nome do Mês", "Status do Faturamento", "ID Tarefa", "Código da Tarefa",
                "Fluxo", "Título da Tarefa", "Tipo da Tarefa", "Ambiente", "Valor da Tarefa", "Qtd. Subtarefas",
                "Solicitante"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Map<String, Object> billingData : data) {
            Row row = sheet.createRow(rowNum++);

            setCellValue(row, 0, billingData.get("billing_year"), dataStyle);
            setCellValue(row, 1, billingData.get("billing_month"), dataStyle);
            setMonthNameCell(row, 2, billingData.get("billing_month"), dataStyle);
            setStatusCell(row, 3, billingData.get("billing_status"), dataStyle);

            setCellValue(row, 4, billingData.get("task_id"), dataStyle);
            setCellValue(row, 5, billingData.get("task_code"), dataStyle);
            setFlowTypeCell(row, 6, billingData.get("task_flow_type"), dataStyle);
            setCellValue(row, 7, billingData.get("task_title"), dataStyle);
            setTaskTypeCell(row, 8, billingData.get("task_type"), dataStyle);
            setEnvironmentCell(row, 9, billingData.get("task_environment"), dataStyle);
            setCellValue(row, 10, billingData.get("task_amount"), currencyStyle);
            setCellValue(row, 11, billingData.get("subtasks_count"), dataStyle);
            setCellValue(row, 12, billingData.get("requester_name"), dataStyle);
        }

        setColumnWidths(sheet, new int[]{
                2500, 2500, 3500, 4000, 2500, 3500, 4000, 8000, 4000, 4000, 4000, 3000, 6000
        });

        for (int i = 1; i <= data.size(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                row.setHeightInPoints(30);
            }
        }

        headerRow.setHeightInPoints(35);

        sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, data.size(), 0, headers.length - 1));

        sheet.createFreezePane(0, 1);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    public byte[] generateGeneralReport(List<Map<String, Object>> data) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Relatório Geral - Visão Completa");

        CellStyle taskHeaderStyle = createColoredHeaderStyle(workbook, IndexedColors.GREY_25_PERCENT.getIndex());
        CellStyle deliveryHeaderStyle = createColoredHeaderStyle(workbook, IndexedColors.PALE_BLUE.getIndex());
        CellStyle billingHeaderStyle = createColoredHeaderStyle(workbook, IndexedColors.LEMON_CHIFFON.getIndex());

        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);

        String[] headers = {
                "ID Tarefa", "Código", "Título", "Descrição", "Prioridade", "Valor", "Solicitante",
                "Criado Por", "Atualizado Por", "Sistema Origem", "Módulo",
                "Tem Entrega", "Faturamento",
                "ID Entrega", "Status Entrega", "Projeto", "Link da entrega (Pull Request)", "Branch", "Notas", "Início Entrega", "Fim Entrega",
                "Ano Faturamento", "Mês Faturamento", "Status Faturamento"
        };

        Row headerRow = sheet.createRow(0);

        int colIndex = 0;

        for (int i = 0; i < 13; i++) {
            Cell cell = headerRow.createCell(colIndex++);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(taskHeaderStyle);
        }

        for (int i = 13; i < 21; i++) {
            Cell cell = headerRow.createCell(colIndex++);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(deliveryHeaderStyle);
        }

        for (int i = 21; i < 24; i++) {
            Cell cell = headerRow.createCell(colIndex++);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(billingHeaderStyle);
        }

        int rowNum = 1;
        for (Map<String, Object> taskData : data) {
            Row row = sheet.createRow(rowNum++);

            colIndex = 0;

            setCellValue(row, colIndex++, taskData.get("task_id"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("task_code"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("task_title"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("task_description"), dataStyle);
            setPriorityCell(row, colIndex++, taskData.get("task_priority"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("task_amount"), currencyStyle);
            setCellValue(row, colIndex++, taskData.get("requester_name"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("created_by_name"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("updated_by_name"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("task_server_origin"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("task_system_module"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("has_delivery"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("has_quote_in_billing"), dataStyle);

            setCellValue(row, colIndex++, taskData.get("delivery_id"), dataStyle);
            setStatusCell(row, colIndex++, taskData.get("delivery_status"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("project_name"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("delivery_pull_request"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("delivery_branch"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("delivery_notes"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("delivery_started_at"), dateStyle);
            setCellValue(row, colIndex++, taskData.get("delivery_finished_at"), dateStyle);

            setCellValue(row, colIndex++, taskData.get("billing_year"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("billing_month"), dataStyle);
            setStatusCell(row, colIndex++, taskData.get("billing_status"), dataStyle);
        }

        setColumnWidths(sheet, new int[]{
                2500, 3500, 10000, 12000, 3000, 4000, 6000, 4000, 4000, 4000, 4000, 3000, 4000,
                3000, 3500, 6000, 10000, 8000, 6000, 6500, 6500,
                2500, 2500, 4000
        });

        for (int i = 1; i <= data.size(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                row.setHeightInPoints(40);
            }
        }

        headerRow.setHeightInPoints(45);

        sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, data.size(), 0, headers.length - 1));

        sheet.createFreezePane(0, 1);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    public byte[] generateGeneralReportForUser(List<Map<String, Object>> data) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Relatório Geral - Visão User");

        CellStyle taskHeaderStyle = createColoredHeaderStyle(workbook, IndexedColors.GREY_25_PERCENT.getIndex());
        CellStyle deliveryHeaderStyle = createColoredHeaderStyle(workbook, IndexedColors.PALE_BLUE.getIndex());

        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);

        String[] headers = {
                "ID Tarefa", "Código", "Título", "Descrição", "Prioridade", "Solicitante",
                "Criado Por", "Atualizado Por", "Sistema Origem", "Módulo",
                "Tem Entrega",
                "ID Entrega", "Status Entrega", "Projeto", "Link da entrega (Pull Request)", "Branch", "Notas", "Início Entrega", "Fim Entrega"
        };

        Row headerRow = sheet.createRow(0);

        int colIndex = 0;

        for (int i = 0; i < 11; i++) {
            Cell cell = headerRow.createCell(colIndex++);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(taskHeaderStyle);
        }

        for (int i = 11; i < 19; i++) {
            Cell cell = headerRow.createCell(colIndex++);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(deliveryHeaderStyle);
        }

        int rowNum = 1;
        for (Map<String, Object> taskData : data) {
            Row row = sheet.createRow(rowNum++);

            colIndex = 0;

            setCellValue(row, colIndex++, taskData.get("task_id"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("task_code"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("task_title"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("task_description"), dataStyle);
            setPriorityCell(row, colIndex++, taskData.get("task_priority"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("requester_name"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("created_by_name"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("updated_by_name"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("task_server_origin"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("task_system_module"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("has_delivery"), dataStyle);

            setCellValue(row, colIndex++, taskData.get("delivery_id"), dataStyle);
            setStatusCell(row, colIndex++, taskData.get("delivery_status"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("project_name"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("delivery_pull_request"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("delivery_branch"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("delivery_notes"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("delivery_started_at"), dateStyle);
            setCellValue(row, colIndex++, taskData.get("delivery_finished_at"), dateStyle);
        }

        setColumnWidths(sheet, new int[]{
                2500, 3500, 10000, 12000, 3000, 6000, 4000, 4000, 4000, 4000, 3000,
                3000, 3500, 6000, 10000, 8000, 8000, 6000, 6500, 6500
        });

        for (int i = 1; i <= data.size(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                row.setHeightInPoints(40);
            }
        }

        headerRow.setHeightInPoints(45);

        sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, data.size(), 0, headers.length - 1));

        sheet.createFreezePane(0, 1);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    private CellStyle createColoredHeaderStyle(Workbook workbook, short colorIndex) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);

        style.setFillForegroundColor(colorIndex);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

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

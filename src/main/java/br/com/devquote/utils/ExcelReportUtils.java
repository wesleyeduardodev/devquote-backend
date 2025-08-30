package br.com.devquote.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
                setCellValue(row, 4, taskData.get("task_status"), dataStyle);
                setCellValue(row, 5, taskData.get("task_type"), dataStyle);
                setCellValue(row, 6, taskData.get("task_priority"), dataStyle);
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
                setCellValue(row, 24, taskData.get("subtask_status"), dataStyle);
                setCellValue(row, 25, taskData.get("subtask_amount"), currencyStyle);
            } else {
                // USER: Remove colunas sensíveis
                setCellValue(row, 0, taskData.get("task_id"), dataStyle);
                setCellValue(row, 1, taskData.get("task_code"), dataStyle);
                setCellValue(row, 2, taskData.get("task_title"), dataStyle);
                setCellValue(row, 3, taskData.get("task_description"), dataStyle);
                setCellValue(row, 4, taskData.get("task_status"), dataStyle);
                setCellValue(row, 5, taskData.get("task_type"), dataStyle);
                setCellValue(row, 6, taskData.get("task_priority"), dataStyle);
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
                setCellValue(row, 21, taskData.get("subtask_status"), dataStyle);
            }
        }

        // Ajustar largura das colunas automaticamente
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            // Definir largura mínima e máxima
            int currentWidth = sheet.getColumnWidth(i);
            if (currentWidth < 2000) {
                sheet.setColumnWidth(i, 2000); // Mínimo
            } else if (currentWidth > 8000) {
                sheet.setColumnWidth(i, 8000); // Máximo
            }
        }

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
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        CreationHelper helper = workbook.getCreationHelper();
        style.setDataFormat(helper.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        CreationHelper helper = workbook.getCreationHelper();
        style.setDataFormat(helper.createDataFormat().getFormat("\"R$ \"#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
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
            cell.setCellValue((LocalDateTime) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value ? "Sim" : "Não");
        } else {
            cell.setCellValue(value.toString());
        }
    }
}
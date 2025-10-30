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
                "ID", "Fluxo", "Código", "Título", "Descrição", "Tipo",
                "Prioridade", "Solicitante", "Criado Por", "Atualizado Por",
                "Origem do Servidor", "Módulo do Sistema", "Link", "Link da Reunião",
                "Valor da Tarefa", "Tem Subtarefas", "Tem Entrega",
                "Orçamento no Faturamento", "Data de Criação", "Data de Atualização",
                "Subtarefa ID", "Subtarefa Título", "Subtarefa Descrição",
                "Subtarefa Valor"
            };
        } else {
            // USER: Remove colunas sensíveis (Valor da Tarefa, Tem Orçamento, Orçamento no Faturamento, Subtarefa Valor)
            headers = new String[] {
                "ID", "Fluxo", "Código", "Título", "Descrição", "Tipo",
                "Prioridade", "Solicitante", "Criado Por", "Atualizado Por",
                "Origem do Servidor", "Módulo do Sistema", "Link", "Link da Reunião",
                "Tem Subtarefas", "Data de Criação", "Data de Atualização",
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
                // USER: colunas 0-14 são tarefas (azul), 15-18 são subtarefas (verde)
                if (i <= 14) {
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
                setFlowTypeCell(row, 1, taskData.get("task_flow_type"), dataStyle);
                setCellValue(row, 2, taskData.get("task_code"), dataStyle);
                setCellValue(row, 3, taskData.get("task_title"), dataStyle);
                setCellValue(row, 4, taskData.get("task_description"), dataStyle);
                setTaskTypeCell(row, 5, taskData.get("task_type"), dataStyle);
                setPriorityCell(row, 6, taskData.get("task_priority"), dataStyle);
                setCellValue(row, 7, taskData.get("requester_name"), dataStyle);
                setCellValue(row, 8, taskData.get("created_by_user"), dataStyle);
                setCellValue(row, 9, taskData.get("updated_by_user"), dataStyle);
                setCellValue(row, 10, taskData.get("server_origin"), dataStyle);
                setCellValue(row, 11, taskData.get("system_module"), dataStyle);
                setCellValue(row, 12, taskData.get("task_link"), dataStyle);
                setCellValue(row, 13, taskData.get("meeting_link"), dataStyle);
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
                setFlowTypeCell(row, 1, taskData.get("task_flow_type"), dataStyle);
                setCellValue(row, 2, taskData.get("task_code"), dataStyle);
                setCellValue(row, 3, taskData.get("task_title"), dataStyle);
                setCellValue(row, 4, taskData.get("task_description"), dataStyle);
                setTaskTypeCell(row, 5, taskData.get("task_type"), dataStyle);
                setPriorityCell(row, 6, taskData.get("task_priority"), dataStyle);
                setCellValue(row, 7, taskData.get("requester_name"), dataStyle);
                setCellValue(row, 8, taskData.get("created_by_user"), dataStyle);
                setCellValue(row, 9, taskData.get("updated_by_user"), dataStyle);
                setCellValue(row, 10, taskData.get("server_origin"), dataStyle);
                setCellValue(row, 11, taskData.get("system_module"), dataStyle);
                setCellValue(row, 12, taskData.get("task_link"), dataStyle);
                setCellValue(row, 13, taskData.get("meeting_link"), dataStyle);
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
                4000,  // Fluxo
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
                4000,  // Fluxo
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
            // Status de Faturamento (conforme interface)
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
            case "BUG" -> "Erro";
            case "ENHANCEMENT" -> "Melhoria";
            case "NEW_FEATURE" -> "Nova Funcionalidade";
            case "FEATURE" -> "Funcionalidade";
            case "REFACTOR" -> "Refatoração";
            case "DOCUMENTATION" -> "Documentação";
            case "MAINTENANCE" -> "Manutenção";
            case "HOTFIX" -> "Correção Urgente";
            case "RESEARCH" -> "Pesquisa";
            case "TASK" -> "Tarefa";
            case "MONITORING" -> "Monitoramento";
            case "SUPPORT" -> "Suporte";
            default -> taskType;
        };
    }

    private String translateFlowType(String flowType) {
        if (flowType == null) return "";
        return switch (flowType.toUpperCase()) {
            case "DESENVOLVIMENTO" -> "💻 Desenvolvimento";
            case "OPERACIONAL" -> "Operacional";
            default -> flowType;
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
        
        // Estilos coloridos para cabeçalhos agrupados
        CellStyle taskHeaderStyle = createColoredHeaderStyle(workbook, IndexedColors.PALE_BLUE.getIndex());
        CellStyle deliveryHeaderStyle = createColoredHeaderStyle(workbook, IndexedColors.LIGHT_GREEN.getIndex());
        CellStyle itemHeaderStyle = createColoredHeaderStyle(workbook, IndexedColors.LIGHT_YELLOW.getIndex());

        // Cabeçalhos reorganizados: Tarefa -> Entrega -> Item (sem IDs, valor e datas da entrega, script no final)
        String[] headers = {
            // Dados da Tarefa
            "ID Tarefa", "Código da Tarefa", "Título da Tarefa", "Qtd. Subtarefas", "Solicitante",
            // Dados da Entrega
            "Status Geral da Entrega",
            // Dados do Item de Entrega
            "Projeto/Repositório", "Status do Item", "Branch", "Branch Origem", 
            "Pull Request", "Observações", "Data Início", "Data Fim"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            
            // Aplicar cores diferentes por grupo
            if (i <= 4) {
                cell.setCellStyle(taskHeaderStyle); // Azul para tarefas
            } else if (i == 5) {
                cell.setCellStyle(deliveryHeaderStyle); // Verde para entrega
            } else {
                cell.setCellStyle(itemHeaderStyle); // Amarelo para itens
            }
        }

        // Adicionar dados
        int rowNum = 1;
        for (Map<String, Object> deliveryData : data) {
            Row row = sheet.createRow(rowNum++);

            // Dados da tarefa
            setCellValue(row, 0, deliveryData.get("task_id"), dataStyle);
            setCellValue(row, 1, deliveryData.get("task_code"), dataStyle);
            setCellValue(row, 2, deliveryData.get("task_title"), dataStyle);
            setCellValue(row, 3, deliveryData.get("subtasks_count"), dataStyle);
            setCellValue(row, 4, deliveryData.get("requester_name"), dataStyle);

            // Dados da entrega (sem datas)
            setDeliveryStatusCell(row, 5, deliveryData.get("delivery_status"), dataStyle);

            // Dados do item
            setCellValue(row, 6, deliveryData.get("project_name"), dataStyle);
            setDeliveryStatusCell(row, 7, deliveryData.get("item_status"), dataStyle);
            setCellValue(row, 8, deliveryData.get("item_branch"), dataStyle);
            setCellValue(row, 9, deliveryData.get("item_source_branch"), dataStyle);
            setCellValue(row, 10, deliveryData.get("item_pull_request"), dataStyle);
            setCellValue(row, 11, deliveryData.get("item_notes"), dataStyle);
            setCellValue(row, 12, deliveryData.get("item_started_at"), dateOnlyStyle);
            setCellValue(row, 13, deliveryData.get("item_finished_at"), dateOnlyStyle);
        }

        // Ajustar largura das colunas (14 colunas no total - sem IDs, valor e datas da entrega)
        setColumnWidths(sheet, new int[]{
            // Dados da Tarefa
            2500,  // ID Tarefa
            3500,  // Código da Tarefa
            8000,  // Título da Tarefa (maior)
            3000,  // Qtd. Subtarefas
            6000,  // Solicitante
            // Dados da Entrega
            4000,  // Status Geral da Entrega
            // Dados do Item de Entrega
            6000,  // Projeto/Repositório
            3500,  // Status do Item
            5000,  // Branch
            5000,  // Branch Origem
            8000,  // Pull Request (maior para URLs)
            6000,  // Observações
            4000,  // Data Início
            4000   // Data Fim
        });

        // Ajustar altura das linhas
        for (int i = 1; i <= data.size(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                row.setHeightInPoints(35); // Altura maior para acomodar URLs
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
            // TAREFAS (Cinza) - 13 colunas (inclui metadados da tarefa + status de entrega e faturamento - sem datas)
            "ID Tarefa", "Código", "Título", "Descrição", "Prioridade", "Valor", "Solicitante", 
            "Criado Por", "Atualizado Por", "Sistema Origem", "Módulo",
            "Tem Entrega", "Faturamento",
            
            // ENTREGAS (Azul Pálido) - 9 colunas - Pull Request reorganizado + notas
            "ID Entrega", "Status Entrega", "Projeto", "Link da entrega (Pull Request)", "Branch", "Notas", "Início Entrega", "Fim Entrega",
            
            // FATURAMENTO (Limão) - 3 colunas - No final
            "Ano Faturamento", "Mês Faturamento", "Status Faturamento"
        };

        Row headerRow = sheet.createRow(0);
        
        // Aplicar cores nos headers por seção - Estrutura corrigida (24 colunas)
        int colIndex = 0;
        
        // TAREFAS + METADADOS + STATUS (0-12) - Cinza
        for (int i = 0; i < 13; i++) {
            Cell cell = headerRow.createCell(colIndex++);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(taskHeaderStyle);
        }
        
        // ENTREGAS (13-20) - Azul Pálido
        for (int i = 13; i < 21; i++) {
            Cell cell = headerRow.createCell(colIndex++);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(deliveryHeaderStyle);
        }
        
        // FATURAMENTO (21-23) - Limão (no final)
        for (int i = 21; i < 24; i++) {
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
            setCellValue(row, colIndex++, taskData.get("delivery_notes"), dataStyle); // Nova coluna de notas
            setCellValue(row, colIndex++, taskData.get("delivery_started_at"), dateOnlyStyle);
            setCellValue(row, colIndex++, taskData.get("delivery_finished_at"), dateOnlyStyle);
            
            // DADOS DE FATURAMENTO (21-23) - No final
            setCellValue(row, colIndex++, taskData.get("billing_year"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("billing_month"), dataStyle);
            setStatusCell(row, colIndex++, taskData.get("billing_status"), dataStyle);
        }

        // Ajustar larguras das colunas (24 colunas total) - sem datas
        setColumnWidths(sheet, new int[]{
            // TAREFAS + METADADOS + STATUS (13 colunas - removidas Data Criação e Data Atualização)
            2500, 3500, 10000, 12000, 3000, 4000, 6000, 4000, 4000, 4000, 4000, 3000, 4000,
            // ENTREGAS (8 colunas) - Pull Request reorganizado + notas
            3000, 3500, 6000, 10000, 8000, 6000, 4000, 4000,
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
            // TAREFAS (Cinza) - 11 colunas (SEM coluna Valor, sem datas, sem faturamento)
            "ID Tarefa", "Código", "Título", "Descrição", "Prioridade", "Solicitante", 
            "Criado Por", "Atualizado Por", "Sistema Origem", "Módulo",
            "Tem Entrega",
            
            // ENTREGAS (Azul Pálido) - 8 colunas
            "ID Entrega", "Status Entrega", "Projeto", "Link da entrega (Pull Request)", "Branch", "Notas", "Início Entrega", "Fim Entrega"
        };

        Row headerRow = sheet.createRow(0);
        
        // Aplicar cores nos headers por seção - 19 colunas total
        int colIndex = 0;
        
        // TAREFAS (0-10) - Cinza - SEM Valor, sem datas, sem faturamento
        for (int i = 0; i < 11; i++) {
            Cell cell = headerRow.createCell(colIndex++);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(taskHeaderStyle);
        }
        
        // ENTREGAS (11-18) - Azul Pálido
        for (int i = 11; i < 19; i++) {
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
            setCellValue(row, colIndex++, taskData.get("created_by_name"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("updated_by_name"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("task_server_origin"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("task_system_module"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("has_delivery"), dataStyle);
            
            // DADOS DE ENTREGAS (11-19)
            setCellValue(row, colIndex++, taskData.get("delivery_id"), dataStyle);
            setStatusCell(row, colIndex++, taskData.get("delivery_status"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("project_name"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("delivery_pull_request"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("delivery_branch"), dataStyle);
            setCellValue(row, colIndex++, taskData.get("delivery_notes"), dataStyle); // Nova coluna de notas
            setCellValue(row, colIndex++, taskData.get("delivery_started_at"), dateOnlyStyle);
            setCellValue(row, colIndex++, taskData.get("delivery_finished_at"), dateOnlyStyle);
        }

        // Ajustar larguras das colunas (20 colunas total - sem datas, sem faturamento)
        setColumnWidths(sheet, new int[]{
            // TAREFAS (11 colunas) - SEM Valor, sem datas, sem faturamento
            2500, 3500, 10000, 12000, 3000, 6000, 4000, 4000, 4000, 4000, 3000,
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

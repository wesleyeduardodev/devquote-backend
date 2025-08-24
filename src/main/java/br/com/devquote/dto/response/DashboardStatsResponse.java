package br.com.devquote.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsResponse {
    
    // Estatísticas gerais
    private GeneralStats general;
    
    // Estatísticas por módulo (baseado nas permissões do usuário)
    private ModuleStats requesters;
    private ModuleStats tasks;
    private ModuleStats quotes;
    private ModuleStats projects;
    private ModuleStats deliveries;
    private ModuleStats billing;
    
    // Tendências e gráficos
    private List<ChartData> tasksChart;
    private List<ChartData> quotesChart;
    private List<StatusCount> tasksByStatus;
    private List<StatusCount> quotesByStatus;
    private List<StatusCount> deliveriesByStatus;
    
    // Atividade recente
    private List<RecentActivity> recentActivities;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GeneralStats {
        private int totalUsers;
        private BigDecimal totalRevenue;
        private int completedTasks;
        private double completionRate;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ModuleStats {
        private int total;
        private int active;
        private int completed;
        private BigDecimal totalValue;
        private BigDecimal averageValue;
        private int thisMonth;
        private int lastMonth;
        private double growthPercentage;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChartData {
        private String label; // Data ou período
        private BigDecimal value;
        private int count;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StatusCount {
        private String status;
        private int count;
        private double percentage;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecentActivity {
        private String type; // TASK_CREATED, QUOTE_APPROVED, etc.
        private String description;
        private String user;
        private String timestamp;
        private String entityId;
    }
}
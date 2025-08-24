package br.com.devquote.service.impl;

import br.com.devquote.dto.response.DashboardStatsResponse;
import br.com.devquote.entity.User;
import br.com.devquote.repository.*;
import br.com.devquote.entity.SubTask;
import br.com.devquote.service.DashboardService;
import br.com.devquote.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final SubTaskRepository subTaskRepository;
    private final QuoteRepository quoteRepository;
    private final ProjectRepository projectRepository;
    private final DeliveryRepository deliveryRepository;
    private final RequesterRepository requesterRepository;
    private final PermissionService permissionService;

    @Override
    public DashboardStatsResponse getDashboardStats(Authentication authentication) {
        try {
            log.debug("Generating dashboard stats for user: {}", authentication.getName());

            // Buscar usuário atual
            String username = authentication.getName();
            User currentUser = userRepository.findByUsernameWithProfiles(username)
                    .or(() -> userRepository.findByEmailWithProfiles(username))
                    .orElseThrow(() -> new RuntimeException("User not found"));

            log.debug("User found: {}, ID: {}", currentUser.getUsername(), currentUser.getId());

            // Obter permissões do usuário
            var userPermissions = permissionService.getUserPermissions(currentUser.getId());
            Set<String> allowedScreens = userPermissions.getResourcePermissions().keySet();
            
            log.debug("Allowed screens for user {}: {}", username, allowedScreens);

            // Construir resposta baseada nas permissões
            DashboardStatsResponse.DashboardStatsResponseBuilder builder = DashboardStatsResponse.builder();

            // Estatísticas gerais (sempre incluídas)
            builder.general(buildGeneralStats(allowedScreens));

            // Estatísticas por módulo (baseadas nas permissões)
            if (allowedScreens.contains("users")) {
                builder.requesters(buildRequestersStats());
            }

            if (allowedScreens.contains("tasks")) {
                builder.tasks(buildTasksStats());
                builder.tasksChart(buildTasksChart());
                builder.tasksByStatus(buildTasksByStatus());
            }

            if (allowedScreens.contains("quotes")) {
                builder.quotes(buildQuotesStats());
                builder.quotesChart(buildQuotesChart());
                builder.quotesByStatus(buildQuotesByStatus());
            }

            if (allowedScreens.contains("projects")) {
                builder.projects(buildProjectsStats());
            }

            if (allowedScreens.contains("deliveries")) {
                builder.deliveries(buildDeliveriesStats());
                builder.deliveriesByStatus(buildDeliveriesByStatus());
            }

            if (allowedScreens.contains("billing")) {
                builder.billing(buildBillingStats());
            }

            // Atividades recentes (baseadas nas permissões)
            builder.recentActivities(buildRecentActivities(allowedScreens));

            log.debug("Dashboard stats generated successfully for user: {}", username);
            return builder.build();
            
        } catch (Exception e) {
            log.error("Error generating dashboard stats for user: {}", authentication.getName(), e);
            throw new RuntimeException("Error generating dashboard statistics: " + e.getMessage(), e);
        }
    }

    private DashboardStatsResponse.GeneralStats buildGeneralStats(Set<String> allowedScreens) {
        int totalUsers = allowedScreens.contains("users") ? (int) requesterRepository.count() : 0;
        
        BigDecimal totalRevenue = BigDecimal.ZERO;
        int completedTasks = 0;
        double completionRate = 0.0;

        if (allowedScreens.contains("tasks")) {
            // Buscar TODAS as tarefas sem paginação
            var allTasks = taskRepository.findAll();
            completedTasks = (int) allTasks.stream()
                    .filter(task -> "COMPLETED".equals(task.getStatus()))
                    .count();
            
            completionRate = allTasks.isEmpty() ? 0.0 : 
                (double) completedTasks / allTasks.size() * 100.0;
        }

        if (allowedScreens.contains("quotes")) {
            // Buscar TODOS os orçamentos sem paginação
            var allQuotes = quoteRepository.findAll();
            totalRevenue = allQuotes.stream()
                    .map(quote -> quote.getTotalAmount() != null ? quote.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        return DashboardStatsResponse.GeneralStats.builder()
                .totalUsers(totalUsers)
                .totalRevenue(totalRevenue)
                .completedTasks(completedTasks)
                .completionRate(Math.round(completionRate * 100.0) / 100.0)
                .build();
    }

    private DashboardStatsResponse.ModuleStats buildRequestersStats() {
        var allRequesters = requesterRepository.findAll();
        int total = allRequesters.size();
        // Requesters não têm campo 'active', consideramos todos como ativos
        int active = total;

        return DashboardStatsResponse.ModuleStats.builder()
                .total(total)
                .active(active)
                .completed(0) // Não se aplica a solicitantes
                .totalValue(BigDecimal.ZERO)
                .averageValue(BigDecimal.ZERO)
                .thisMonth(0) // TODO: Implementar contagem mensal
                .lastMonth(0)
                .growthPercentage(0.0)
                .build();
    }

    private DashboardStatsResponse.ModuleStats buildTasksStats() {
        var allTasks = taskRepository.findAll();
        int total = allTasks.size();
        int completed = (int) allTasks.stream()
                .filter(task -> "COMPLETED".equals(task.getStatus()))
                .count();
        int active = total - completed;

        // Calcular valor total das tarefas através das subtarefas
        var allSubTasks = subTaskRepository.findAll();
        BigDecimal totalValue = allSubTasks.stream()
                .map(subTask -> subTask.getAmount() != null ? subTask.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageValue = total > 0 && totalValue.compareTo(BigDecimal.ZERO) > 0 ? 
                totalValue.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;

        return DashboardStatsResponse.ModuleStats.builder()
                .total(total)
                .active(active)
                .completed(completed)
                .totalValue(totalValue)
                .averageValue(averageValue)
                .thisMonth(0) // TODO: Implementar
                .lastMonth(0)
                .growthPercentage(0.0)
                .build();
    }

    private DashboardStatsResponse.ModuleStats buildQuotesStats() {
        var allQuotes = quoteRepository.findAll();
        int total = allQuotes.size();
        int completed = (int) allQuotes.stream()
                .filter(quote -> "APPROVED".equals(quote.getStatus()))
                .count();
        int active = total - completed;

        BigDecimal totalValue = allQuotes.stream()
                .map(quote -> quote.getTotalAmount() != null ? quote.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageValue = total > 0 && totalValue.compareTo(BigDecimal.ZERO) > 0 ? 
                totalValue.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;

        return DashboardStatsResponse.ModuleStats.builder()
                .total(total)
                .active(active)
                .completed(completed)
                .totalValue(totalValue)
                .averageValue(averageValue)
                .thisMonth(0) // TODO: Implementar
                .lastMonth(0)
                .growthPercentage(0.0)
                .build();
    }

    private DashboardStatsResponse.ModuleStats buildProjectsStats() {
        var allProjects = projectRepository.findAll();
        int total = allProjects.size();
        // Projects não têm campo status, consideramos todos como ativos
        int active = total;
        int completed = 0; // Sem campo status, não podemos determinar projetos concluídos

        return DashboardStatsResponse.ModuleStats.builder()
                .total(total)
                .active(active)
                .completed(completed)
                .totalValue(BigDecimal.ZERO) // Projetos não têm valor direto
                .averageValue(BigDecimal.ZERO)
                .thisMonth(0)
                .lastMonth(0)
                .growthPercentage(0.0)
                .build();
    }

    private DashboardStatsResponse.ModuleStats buildDeliveriesStats() {
        var allDeliveries = deliveryRepository.findAll();
        int total = allDeliveries.size();
        int completed = (int) allDeliveries.stream()
                .filter(delivery -> "APPROVED".equals(delivery.getStatus()))
                .count();
        int active = total - completed;

        return DashboardStatsResponse.ModuleStats.builder()
                .total(total)
                .active(active)
                .completed(completed)
                .totalValue(BigDecimal.ZERO)
                .averageValue(BigDecimal.ZERO)
                .thisMonth(0)
                .lastMonth(0)
                .growthPercentage(0.0)
                .build();
    }

    private DashboardStatsResponse.ModuleStats buildBillingStats() {
        // TODO: Implementar estatísticas de faturamento quando estiver disponível
        return DashboardStatsResponse.ModuleStats.builder()
                .total(0)
                .active(0)
                .completed(0)
                .totalValue(BigDecimal.ZERO)
                .averageValue(BigDecimal.ZERO)
                .thisMonth(0)
                .lastMonth(0)
                .growthPercentage(0.0)
                .build();
    }

    private List<DashboardStatsResponse.ChartData> buildTasksChart() {
        // TODO: Implementar dados de gráfico para tarefas (últimos 7 dias, por exemplo)
        List<DashboardStatsResponse.ChartData> chartData = new ArrayList<>();
        
        for (int i = 6; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            chartData.add(DashboardStatsResponse.ChartData.builder()
                    .label(date.format(DateTimeFormatter.ofPattern("dd/MM")))
                    .value(BigDecimal.valueOf(Math.random() * 1000)) // Dados mock por enquanto
                    .count((int) (Math.random() * 10))
                    .build());
        }
        
        return chartData;
    }

    private List<DashboardStatsResponse.ChartData> buildQuotesChart() {
        // TODO: Implementar dados de gráfico para orçamentos
        List<DashboardStatsResponse.ChartData> chartData = new ArrayList<>();
        
        for (int i = 6; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            chartData.add(DashboardStatsResponse.ChartData.builder()
                    .label(date.format(DateTimeFormatter.ofPattern("dd/MM")))
                    .value(BigDecimal.valueOf(Math.random() * 5000))
                    .count((int) (Math.random() * 5))
                    .build());
        }
        
        return chartData;
    }

    private List<DashboardStatsResponse.StatusCount> buildTasksByStatus() {
        var allTasks = taskRepository.findAll();
        long total = allTasks.size();
        
        List<DashboardStatsResponse.StatusCount> statusCounts = new ArrayList<>();
        
        var statusGroups = allTasks.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    task -> task.getStatus() != null ? task.getStatus() : "UNKNOWN",
                    java.util.stream.Collectors.counting()
                ));

        statusGroups.forEach((status, count) -> {
            double percentage = total > 0 ? (double) count / total * 100.0 : 0.0;
            statusCounts.add(DashboardStatsResponse.StatusCount.builder()
                    .status(status)
                    .count(count.intValue())
                    .percentage(Math.round(percentage * 100.0) / 100.0)
                    .build());
        });

        return statusCounts;
    }

    private List<DashboardStatsResponse.StatusCount> buildQuotesByStatus() {
        var allQuotes = quoteRepository.findAll();
        long total = allQuotes.size();
        
        List<DashboardStatsResponse.StatusCount> statusCounts = new ArrayList<>();
        
        var statusGroups = allQuotes.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    quote -> quote.getStatus() != null ? quote.getStatus() : "UNKNOWN",
                    java.util.stream.Collectors.counting()
                ));

        statusGroups.forEach((status, count) -> {
            double percentage = total > 0 ? (double) count / total * 100.0 : 0.0;
            statusCounts.add(DashboardStatsResponse.StatusCount.builder()
                    .status(status)
                    .count(count.intValue())
                    .percentage(Math.round(percentage * 100.0) / 100.0)
                    .build());
        });

        return statusCounts;
    }

    private List<DashboardStatsResponse.StatusCount> buildDeliveriesByStatus() {
        var allDeliveries = deliveryRepository.findAll();
        long total = allDeliveries.size();
        
        List<DashboardStatsResponse.StatusCount> statusCounts = new ArrayList<>();
        
        var statusGroups = allDeliveries.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    delivery -> delivery.getStatus() != null ? delivery.getStatus() : "UNKNOWN",
                    java.util.stream.Collectors.counting()
                ));

        statusGroups.forEach((status, count) -> {
            double percentage = total > 0 ? (double) count / total * 100.0 : 0.0;
            statusCounts.add(DashboardStatsResponse.StatusCount.builder()
                    .status(status)
                    .count(count.intValue())
                    .percentage(Math.round(percentage * 100.0) / 100.0)
                    .build());
        });

        return statusCounts;
    }

    private List<DashboardStatsResponse.RecentActivity> buildRecentActivities(Set<String> allowedScreens) {
        List<DashboardStatsResponse.RecentActivity> activities = new ArrayList<>();
        
        // TODO: Implementar atividades recentes baseadas em auditoria/logs
        // Por enquanto, retornando lista vazia
        
        return activities;
    }
}
package br.com.devquote.service.impl;

import br.com.devquote.dto.response.DashboardStatsResponse;
import br.com.devquote.entity.User;
import br.com.devquote.entity.SubTask;
import br.com.devquote.enums.DeliveryStatus;
import br.com.devquote.repository.*;
import br.com.devquote.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final SubTaskRepository subTaskRepository;
    private final ProjectRepository projectRepository;
    private final DeliveryRepository deliveryRepository;
    private final RequesterRepository requesterRepository;

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

            log.debug("Generating dashboard stats focused on tasks and deliveries for all users");

            // Construir resposta focada em tarefas e entregas para todos os usuários
            DashboardStatsResponse.DashboardStatsResponseBuilder builder = DashboardStatsResponse.builder();

            // Estatísticas gerais baseadas em tarefas e entregas
            builder.general(buildTasksAndDeliveriesGeneralStats());

            // Sempre incluir dados de tarefas e entregas para todos os usuários
            builder.tasks(buildTasksStats());
            builder.tasksChart(buildTasksChart());
            builder.tasksByStatus(buildMonthlyTasksStats());
            
            builder.deliveries(buildDeliveriesStats());
            builder.deliveriesByStatus(buildMonthlyDeliveriesStats());

            // Atividades recentes focadas em tarefas e entregas
            builder.recentActivities(buildTasksAndDeliveriesRecentActivities());

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
            // Without status field, consider all tasks for completion rate
            completedTasks = allTasks.size();
            
            completionRate = allTasks.isEmpty() ? 0.0 : 
                (double) completedTasks / allTasks.size() * 100.0;
        }

        // Não há mais orçamentos - usar valor das tarefas
        if (allowedScreens.contains("tasks")) {
            var allTasks = taskRepository.findAll();
            totalRevenue = allTasks.stream()
                    .map(task -> task.getAmount() != null ? task.getAmount() : BigDecimal.ZERO)
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
                // No status filter - include all tasks
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
        
        log.debug("Total deliveries found: {}", total);
        
        // Listar todos os status encontrados para debug
        allDeliveries.forEach(delivery -> 
            log.debug("Delivery ID: {}, Status: '{}'", delivery.getId(), delivery.getStatus()));
        
        // Status aprovados: APPROVED e PRODUCTION
        int completed = (int) allDeliveries.stream()
                .filter(delivery -> {
                    DeliveryStatus status = delivery.getStatus();
                    boolean isApproved = DeliveryStatus.APPROVED.equals(status) || DeliveryStatus.PRODUCTION.equals(status);
                    log.debug("Delivery ID: {}, Status: '{}', IsApproved: {}", delivery.getId(), status, isApproved);
                    return isApproved;
                })
                .count();
        
        // Status em andamento: PENDING, DEVELOPMENT, DELIVERED, HOMOLOGATION, REJECTED
        int active = (int) allDeliveries.stream()
                .filter(delivery -> {
                    DeliveryStatus status = delivery.getStatus();
                    return DeliveryStatus.PENDING.equals(status) || 
                           DeliveryStatus.DEVELOPMENT.equals(status) ||
                           DeliveryStatus.DELIVERED.equals(status) ||
                           DeliveryStatus.HOMOLOGATION.equals(status) ||
                           DeliveryStatus.REJECTED.equals(status);
                })
                .count();

        log.debug("Delivery stats - Total: {}, Completed: {}, Active: {}", total, completed, active);

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


    private List<DashboardStatsResponse.StatusCount> buildMonthlyTasksStats() {
        var now = LocalDateTime.now();
        var startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        var endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
        
        var allTasks = taskRepository.findAll();
        
        // Tarefas criadas no mês corrente
        long tasksCreatedThisMonth = allTasks.stream()
                .filter(task -> task.getCreatedAt().isAfter(startOfMonth) && task.getCreatedAt().isBefore(endOfMonth))
                .count();
        
        // Tarefas concluídas no mês corrente
        long tasksCompletedThisMonth = allTasks.stream()
                .filter(task -> 
                               task.getUpdatedAt() != null &&
                               task.getUpdatedAt().isAfter(startOfMonth) && 
                               task.getUpdatedAt().isBefore(endOfMonth))
                .count();
        
        // Tarefas em progresso no mês
        long tasksInProgressThisMonth = allTasks.stream()
                .filter(task -> 
                               task.getUpdatedAt() != null &&
                               task.getUpdatedAt().isAfter(startOfMonth) && 
                               task.getUpdatedAt().isBefore(endOfMonth))
                .count();
        
        List<DashboardStatsResponse.StatusCount> monthlyStats = new ArrayList<>();
        
        monthlyStats.add(DashboardStatsResponse.StatusCount.builder()
                .status("Criadas este mês")
                .count((int) tasksCreatedThisMonth)
                .percentage(0.0) // Não aplicável para dados mensais
                .build());
                
        monthlyStats.add(DashboardStatsResponse.StatusCount.builder()
                .status("Concluídas este mês")
                .count((int) tasksCompletedThisMonth)
                .percentage(0.0)
                .build());
                
        monthlyStats.add(DashboardStatsResponse.StatusCount.builder()
                .status("Em progresso este mês")
                .count((int) tasksInProgressThisMonth)
                .percentage(0.0)
                .build());

        return monthlyStats;
    }


    private List<DashboardStatsResponse.StatusCount> buildMonthlyDeliveriesStats() {
        var allDeliveries = deliveryRepository.findAll();
        int totalDeliveries = allDeliveries.size();
        
        if (totalDeliveries == 0) {
            return new ArrayList<>();
        }
        
        List<DashboardStatsResponse.StatusCount> statusStats = new ArrayList<>();
        
        // Contar entregas por cada status dos 7 status existentes
        DeliveryStatus[] statuses = {DeliveryStatus.PENDING, DeliveryStatus.DEVELOPMENT, DeliveryStatus.DELIVERED, 
                                    DeliveryStatus.HOMOLOGATION, DeliveryStatus.APPROVED, DeliveryStatus.REJECTED, DeliveryStatus.PRODUCTION};
        String[] statusLabels = {"Pendente", "Em Desenvolvimento", "Entregue", "Homologação", "Aprovado", "Rejeitado", "Produção"};
        
        for (int i = 0; i < statuses.length; i++) {
            DeliveryStatus status = statuses[i];
            String label = statusLabels[i];
            
            long count = allDeliveries.stream()
                    .filter(delivery -> status.equals(delivery.getStatus()))
                    .count();
                    
            double percentage = (double) count / totalDeliveries * 100.0;
            
            if (count > 0) { // Só adiciona se houver entregas com esse status
                statusStats.add(DashboardStatsResponse.StatusCount.builder()
                        .status(label)
                        .count((int) count)
                        .percentage(Math.round(percentage * 100.0) / 100.0)
                        .build());
            }
        }

        return statusStats;
    }

    private List<DashboardStatsResponse.RecentActivity> buildRecentActivities(Set<String> allowedScreens) {
        List<DashboardStatsResponse.RecentActivity> activities = new ArrayList<>();
        
        // TODO: Implementar atividades recentes baseadas em auditoria/logs
        // Por enquanto, retornando lista vazia
        
        return activities;
    }

    private DashboardStatsResponse.GeneralStats buildTasksAndDeliveriesGeneralStats() {
        // Estatísticas baseadas apenas em tarefas e entregas
        var allTasks = taskRepository.findAll();
        var allDeliveries = deliveryRepository.findAll();
        
        int totalTasks = allTasks.size();
        int completedTasks = (int) allTasks.stream()
                // No status filter - include all tasks
                .count();
        
        int totalDeliveries = allDeliveries.size();
        int completedDeliveries = (int) allDeliveries.stream()
                .filter(delivery -> DeliveryStatus.APPROVED.equals(delivery.getStatus()) || DeliveryStatus.PRODUCTION.equals(delivery.getStatus()))
                .count();
        
        double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100.0 : 0.0;
        
        return DashboardStatsResponse.GeneralStats.builder()
                .totalUsers(0) // Não mostrar usuários
                .totalRevenue(BigDecimal.ZERO) // Não mostrar receita
                .completedTasks(completedTasks)
                .completionRate(Math.round(completionRate * 100.0) / 100.0)
                .build();
    }

    private List<DashboardStatsResponse.RecentActivity> buildTasksAndDeliveriesRecentActivities() {
        List<DashboardStatsResponse.RecentActivity> activities = new ArrayList<>();
        
        // Buscar tarefas recentes (últimas 5)
        var recentTasks = taskRepository.findAll().stream()
                .sorted((t1, t2) -> {
                    LocalDateTime time1 = t1.getUpdatedAt() != null ? t1.getUpdatedAt() : t1.getCreatedAt();
                    LocalDateTime time2 = t2.getUpdatedAt() != null ? t2.getUpdatedAt() : t2.getCreatedAt();
                    return time2.compareTo(time1);
                })
                .limit(5)
                .toList();
                
        for (var task : recentTasks) {
            LocalDateTime activityTime = task.getUpdatedAt() != null ? task.getUpdatedAt() : task.getCreatedAt();
            String userName = task.getUpdatedBy() != null ? task.getUpdatedBy().getUsername() : 
                             task.getCreatedBy() != null ? task.getCreatedBy().getUsername() : "Sistema";
            
            activities.add(DashboardStatsResponse.RecentActivity.builder()
                    .type("TASK")
                    .description("Tarefa: " + task.getTitle())
                    .user(userName)
                    .timestamp(activityTime.toString())
                    .entityId(task.getId().toString())
                    .build());
        }
        
        // Buscar entregas recentes (últimas 5)  
        var recentDeliveries = deliveryRepository.findAll().stream()
                .sorted((d1, d2) -> {
                    LocalDateTime time1 = d1.getUpdatedAt() != null ? d1.getUpdatedAt() : d1.getCreatedAt();
                    LocalDateTime time2 = d2.getUpdatedAt() != null ? d2.getUpdatedAt() : d2.getCreatedAt();
                    return time2.compareTo(time1);
                })
                .limit(5)
                .toList();
                
        for (var delivery : recentDeliveries) {
            LocalDateTime activityTime = delivery.getUpdatedAt() != null ? delivery.getUpdatedAt() : delivery.getCreatedAt();
            // Na nova arquitetura, usar "Sistema" como usuário padrão para deliveries
            String userName = "Sistema";
            
            // Na nova arquitetura, usar o nome da tarefa como título
            String deliveryTitle = delivery.getTask() != null ? delivery.getTask().getTitle() : "Entrega #" + delivery.getId();
            
            activities.add(DashboardStatsResponse.RecentActivity.builder()
                    .type("DELIVERY")
                    .description("Entrega: " + deliveryTitle + " - Status: " + delivery.getStatus())
                    .user(userName)
                    .timestamp(activityTime.toString())
                    .entityId(delivery.getId().toString())
                    .build());
        }
        
        // Ordenar por timestamp decrescente e limitar a 10
        return activities.stream()
                .sorted((a1, a2) -> a2.getTimestamp().compareTo(a1.getTimestamp()))
                .limit(10)
                .toList();
    }
}
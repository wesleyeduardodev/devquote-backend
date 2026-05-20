package br.com.devquote.service.impl;
import br.com.devquote.dto.response.DashboardStatsResponse;
import br.com.devquote.repository.DeliveryRepository;
import br.com.devquote.repository.TaskRepository;
import br.com.devquote.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final TaskRepository taskRepository;
    private final DeliveryRepository deliveryRepository;

    @Override
    public DashboardStatsResponse getDashboardStats(Authentication authentication) {
        return DashboardStatsResponse.builder()
                .recentActivities(buildRecentActivities())
                .build();
    }

    private List<DashboardStatsResponse.RecentActivity> buildRecentActivities() {
        List<DashboardStatsResponse.RecentActivity> activities = new ArrayList<>();

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
            String userName = task.getUpdatedBy() != null ? task.getUpdatedBy().getUsername()
                    : task.getCreatedBy() != null ? task.getCreatedBy().getUsername() : "Sistema";

            activities.add(DashboardStatsResponse.RecentActivity.builder()
                    .type("TASK")
                    .description("Tarefa: " + task.getTitle())
                    .user(userName)
                    .timestamp(activityTime.toString())
                    .entityId(task.getId().toString())
                    .build());
        }

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
            String deliveryTitle = delivery.getTask() != null ? delivery.getTask().getTitle() : "Entrega #" + delivery.getId();

            activities.add(DashboardStatsResponse.RecentActivity.builder()
                    .type("DELIVERY")
                    .description("Entrega: " + deliveryTitle + " - Status: " + delivery.getStatus())
                    .user("Sistema")
                    .timestamp(activityTime.toString())
                    .entityId(delivery.getId().toString())
                    .build());
        }

        return activities.stream()
                .sorted((a1, a2) -> a2.getTimestamp().compareTo(a1.getTimestamp()))
                .limit(10)
                .toList();
    }
}

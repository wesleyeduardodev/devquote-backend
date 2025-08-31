package br.com.devquote.adapter;

import br.com.devquote.dto.request.BillingPeriodTaskRequest;
import br.com.devquote.dto.response.BillingPeriodTaskResponse;
import br.com.devquote.entity.BillingPeriod;
import br.com.devquote.entity.BillingPeriodTask;
import br.com.devquote.entity.Task;

public class BillingPeriodTaskAdapter {

    public static BillingPeriodTask toEntity(BillingPeriodTaskRequest dto, BillingPeriod billingPeriod, Task task) {
        if (dto == null) {
            return null;
        }

        return BillingPeriodTask.builder()
                .billingPeriod(billingPeriod)
                .task(task)
                .build();
    }

    public static BillingPeriodTaskResponse toResponseDTO(BillingPeriodTask entity) {
        if (entity == null) {
            return null;
        }

        return BillingPeriodTaskResponse.builder()
                .id(entity.getId())
                .billingPeriodId(entity.getBillingPeriod() != null ? entity.getBillingPeriod().getId() : null)
                .taskId(entity.getTask() != null ? entity.getTask().getId() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
package org.example.apiresource.batch.processor;

import lombok.extern.slf4j.Slf4j;
import org.example.apiresource.domain.model.ResourceStatus;
import org.example.apiresource.domain.model.entity.Resources;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
public class ResourceItemProcessor implements ItemProcessor<Resources, Resources> {

    @Override
    public Resources process(Resources resource) {
        LocalDate today = LocalDate.now();

        LocalDate start = resource.getStartDate();
        LocalDate end = resource.getEndDate();

        // 날짜가 없으면 상태 변경 X
        if (start == null || end == null) {
            return resource;
        }

        // 상태 계산
        ResourceStatus newStatus;
        if (today.isBefore(start)) {
            newStatus = ResourceStatus.PROGRESS_BEFORE;
        } else if ((today.isEqual(start) || today.isAfter(start)) && today.isBefore(end.plusDays(1))) {
            newStatus = ResourceStatus.IN_PROGRESS;
        } else {
            newStatus = ResourceStatus.CLOSED;
        }

        // 변경 필요할 때만 변경
        if (resource.getStatus() != newStatus) {
            log.info("리소스 ID {} 상태 변경: {} -> {}", resource.getId(), resource.getStatus(), newStatus);
            resource.setStatus(newStatus);
        }

        return resource;
    }
}
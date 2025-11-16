package org.example.apiresource.usecase.port.out;

import org.example.apiresource.domain.model.DayOfWeek;
import org.example.apiresource.domain.model.entity.ResourceTimeSlots;

import java.util.List;
import java.util.Map;

public interface ResourceTimeSlotPersistencePort {

    void saveAll(List<ResourceTimeSlots> timeSlots);

    List<ResourceTimeSlots> findByResourceId(Long resourceId);

    List<ResourceTimeSlots> findByResourceIdOrderByDayOfWeekAscStartTimeAsc(Long resourceId);

    List<ResourceTimeSlots> findByResource_IdAndDayOfWeekAndIsActiveTrue(Long resourceId, DayOfWeek day);

    Map<DayOfWeek, Integer> countActiveSlotsByResource(Long id);
}

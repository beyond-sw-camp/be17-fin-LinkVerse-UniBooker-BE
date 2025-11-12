package org.example.apiresource.usecase.port.out;

import org.example.apiresource.domain.model.DayOfWeek;
import org.example.apiresource.domain.model.entity.ResourceTimeSlots;

import java.util.List;

public interface ResourceTimeSlotPersistencePort {

    void saveAll(List<ResourceTimeSlots> timeSlots);

    List<ResourceTimeSlots> findByResourceId(Long resourceId);

    List<ResourceTimeSlots> findByResourcesIdOrderByDayOfWeekAscStartTimeAsc(Long resourceId);

    List<ResourceTimeSlots> findByResources_IdAndDayOfWeekAndIsActiveTrue(Long resourceId, DayOfWeek day);
}

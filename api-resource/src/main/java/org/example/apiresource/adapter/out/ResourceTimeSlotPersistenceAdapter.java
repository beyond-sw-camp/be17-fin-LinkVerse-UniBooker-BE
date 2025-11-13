package org.example.apiresource.adapter.out;

import lombok.RequiredArgsConstructor;
import org.example.apiresource.adapter.out.repository.ResourceTimeSlotRepository;
import org.example.apiresource.domain.model.DayOfWeek;
import org.example.apiresource.domain.model.entity.ResourceTimeSlots;
import org.example.apiresource.usecase.port.out.ResourceTimeSlotPersistencePort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ResourceTimeSlotPersistenceAdapter implements ResourceTimeSlotPersistencePort {

    private final ResourceTimeSlotRepository resourceTimeSlotRepository;

    @Override
    @Transactional
    public void saveAll(List<ResourceTimeSlots> timeSlots) {
        resourceTimeSlotRepository.saveAll(timeSlots);
    }


    @Override
    @Transactional
    public List<ResourceTimeSlots> findByResourceId(Long resourceId) {
        return resourceTimeSlotRepository.findByResource_Id(resourceId);
    }


    @Override
    @Transactional
    public List<ResourceTimeSlots> findByResourceIdOrderByDayOfWeekAscStartTimeAsc(Long resourceId) {
        return resourceTimeSlotRepository.findByResourceIdOrderByDayOfWeekAscStartTimeAsc(resourceId);
    }


    @Override
    @Transactional
    public List<ResourceTimeSlots> findByResource_IdAndDayOfWeekAndIsActiveTrue(Long resourceId, DayOfWeek day) {
        return resourceTimeSlotRepository.findByResource_IdAndDayOfWeekAndIsActiveTrue(resourceId, day);
    }
}

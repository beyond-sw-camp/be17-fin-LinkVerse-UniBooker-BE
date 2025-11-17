package org.example.apiresource.adapter.out;

import lombok.RequiredArgsConstructor;
import org.example.apiresource.adapter.out.repository.ResourceTimeSlotExceptionRepository;
import org.example.apiresource.domain.model.entity.ResourceTimeSlotExceptions;
import org.example.apiresource.usecase.port.out.ResourceTimeSlotExceptionPersistencePort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ResourceTimeSlotExceptionPersistenceAdapter implements ResourceTimeSlotExceptionPersistencePort {

    private final ResourceTimeSlotExceptionRepository resourceTimeSlotExceptionRepository;

    @Override
    @Transactional
    public void saveAll(List<ResourceTimeSlotExceptions> timeSlotExceptions) {
        resourceTimeSlotExceptionRepository.saveAll(timeSlotExceptions);
    }


    @Override
    @Transactional
    public List<ResourceTimeSlotExceptions> findByResourceId(Long resourceId) {
        return resourceTimeSlotExceptionRepository.findByResource_Id(resourceId);
    }


    @Override
    @Transactional
    public List<ResourceTimeSlotExceptions> findByResource_IdAndDateBetweenAndDeletedAtIsNull(Long resourceId, LocalDate pageStart, LocalDate pageEnd) {
        return resourceTimeSlotExceptionRepository.findByResource_IdAndDateBetweenAndDeletedAtIsNull(resourceId, pageStart, pageEnd);
    }


    @Override
    public List<ResourceTimeSlotExceptions> findByResource_IdOrderByDateAscStartTimeAsc(Long resourceId) {
        return resourceTimeSlotExceptionRepository.findByResource_IdOrderByDateAscStartTimeAsc(resourceId);
    }
}
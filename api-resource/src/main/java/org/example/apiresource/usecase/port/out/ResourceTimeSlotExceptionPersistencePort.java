package org.example.apiresource.usecase.port.out;

import org.example.apiresource.domain.model.entity.ResourceTimeSlotExceptions;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ResourceTimeSlotExceptionPersistencePort {

    void saveAll(List<ResourceTimeSlotExceptions> timeSlotExceptions);

    List<ResourceTimeSlotExceptions> findByResourceId(Long resourceId);

    List<ResourceTimeSlotExceptions> findByResource_IdAndDateBetweenAndDeletedAtIsNull(Long resourceId, LocalDate pageStart, LocalDate pageEnd);

    List<ResourceTimeSlotExceptions> findByResource_IdOrderByDateAscStartTimeAsc(Long resourceId);
}
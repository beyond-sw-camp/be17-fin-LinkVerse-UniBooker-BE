package org.example.apiresource.usecase.port.out;

import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ViewCountPersistencePort {

    long sumViewsByResourceGroupIdAndDate(Long resourceGroupId, LocalDateTime start, LocalDateTime end);

    List<Object[]> getTodayHourlyViews(Long resourceGroupId, LocalDateTime startOfDay, LocalDateTime endOfDay);
}
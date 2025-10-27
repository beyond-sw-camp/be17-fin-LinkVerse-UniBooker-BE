package org.example.unibooker.domain.resource.repository;

import org.example.unibooker.domain.resource.model.ResourceTimeSlotExceptions;
import org.example.unibooker.domain.resource.model.Resources;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ResourceTimeSlotExceptionRepository extends JpaRepository<ResourceTimeSlotExceptions, Long> {

    // 특정 리소스 예외시간 슬롯 조회
    List<ResourceTimeSlotExceptions> findByResources_Id(Long resourceId);

    List<ResourceTimeSlotExceptions> findByResources_IdAndDateBetweenAndDeletedAtIsNull(Long resourceId, LocalDate pageStart, LocalDate pageEnd);
}

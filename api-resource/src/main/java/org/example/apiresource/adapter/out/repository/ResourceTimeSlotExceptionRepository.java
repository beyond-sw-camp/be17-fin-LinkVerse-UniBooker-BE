package org.example.apiresource.adapter.out.repository;

import org.example.apiresource.domain.model.entity.ResourceTimeSlotExceptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ResourceTimeSlotExceptionRepository extends JpaRepository<ResourceTimeSlotExceptions, Long> {

    // 특정 리소스 예외시간 슬롯 조회
    List<ResourceTimeSlotExceptions> findByResource_Id(Long resourceId);

    List<ResourceTimeSlotExceptions> findByResource_IdAndDateBetweenAndDeletedAtIsNull(Long resourceId, LocalDate pageStart, LocalDate pageEnd);

    List<ResourceTimeSlotExceptions> findByResource_IdOrderByDateAscStartTimeAsc(Long resourceId);
}

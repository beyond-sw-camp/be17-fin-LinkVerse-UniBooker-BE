package org.example.unibooker.domain.resource.repository;

import org.example.unibooker.domain.resource.model.DayOfWeek;
import org.example.unibooker.domain.resource.model.ResourceTimeSlots;
import org.example.unibooker.domain.resource.model.Resources;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceTimeSlotRepository extends JpaRepository<ResourceTimeSlots, Long> {

    // 특정 요일의 활성화된 시간 슬롯 조회
    List<ResourceTimeSlots> findByResources_IdAndDayOfWeekAndIsActiveTrue(Long resourceId, DayOfWeek dayOfWeek);

    // 특정 리소스의 모든 활성화된 시간 슬롯 조회
    List<ResourceTimeSlots> findByResources_IdAndIsActiveTrue(Long resourceId);

    List<ResourceTimeSlots> findByResources_Id(Long resourceId);
}
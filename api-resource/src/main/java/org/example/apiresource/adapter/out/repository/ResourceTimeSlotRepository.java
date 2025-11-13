package org.example.apiresource.adapter.out.repository;

import org.example.apiresource.domain.model.DayOfWeek;
import org.example.apiresource.domain.model.entity.ResourceTimeSlots;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceTimeSlotRepository extends JpaRepository<ResourceTimeSlots, Long> {

    // 특정 요일의 활성화된 시간 슬롯 조회
    List<ResourceTimeSlots> findByResource_IdAndDayOfWeekAndIsActiveTrue(Long resourceId, DayOfWeek dayOfWeek);

    // 특정 리소스의 모든 활성화된 시간 슬롯 조회
    List<ResourceTimeSlots> findByResource_IdAndIsActiveTrue(Long resourceId);

    List<ResourceTimeSlots> findByResource_Id(Long resourceId);

    List<ResourceTimeSlots> findByResourceIdOrderByDayOfWeekAscStartTimeAsc(Long resourceId);
}
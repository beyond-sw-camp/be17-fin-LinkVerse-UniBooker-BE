package org.example.unibooker.domain.resource.repository;

import org.example.unibooker.domain.resource.model.DayOfWeek;
import org.example.unibooker.domain.resource.model.ResourceTimeSlots;
import org.example.unibooker.domain.resource.model.Resources;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceTimeSlotRepository extends JpaRepository<ResourceTimeSlots, Long> {

    // resources.id와 isActive = true인 값 조회
    List<ResourceTimeSlots> findByResources_IdAndIsActiveTrue(Long resourceId);
}

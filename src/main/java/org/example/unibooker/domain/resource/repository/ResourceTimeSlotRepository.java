package org.example.unibooker.domain.resource.repository;

import org.example.unibooker.domain.resource.model.DayOfWeek;
import org.example.unibooker.domain.resource.model.ResourceTimeSlots;
import org.example.unibooker.domain.resource.model.Resources;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceTimeSlotRepository extends JpaRepository<ResourceTimeSlots, Long> {
    List<ResourceTimeSlots> findAllByResourcesIdAndIsActiveTrue(Long resourceId);
}

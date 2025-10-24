package org.example.unibooker.domain.resource.repository;

import org.example.unibooker.domain.resource.model.ResourceTimeSlotExceptions;
import org.example.unibooker.domain.resource.model.Resources;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ResourceTimeSlotExceptionRepository extends JpaRepository<ResourceTimeSlotExceptions, Long> {
    List<ResourceTimeSlotExceptions> findAllByResourcesId(Long resourceId);
}

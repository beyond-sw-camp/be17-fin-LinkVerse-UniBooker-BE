package org.example.unibooker.domain.resource.repository;

import org.example.unibooker.domain.resource.model.Resources;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResourceRepository extends JpaRepository<Resources, Long> {
    List<Resources> findAllByResourceGroupIdAndIsActiveTrueAndDeletedAtIsNull(Long resourceGroupId);

    Optional<Resources> findByIdAndIsActiveTrueAndDeletedAtIsNull(Long resourceId);
}

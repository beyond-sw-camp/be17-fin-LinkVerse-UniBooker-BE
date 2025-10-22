package org.example.unibooker.domain.resource.repository;

import org.example.unibooker.domain.resource.model.ResourceCustomFieldValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceCustomFieldValueRepository extends JpaRepository<ResourceCustomFieldValues, Long> {
    List<ResourceCustomFieldValues> findByResourceIdAndDeletedAtIsNull(Long resourceId);

    Optional<ResourceCustomFieldValues> findByIdAndDeletedAtIsNull(Long id);
}

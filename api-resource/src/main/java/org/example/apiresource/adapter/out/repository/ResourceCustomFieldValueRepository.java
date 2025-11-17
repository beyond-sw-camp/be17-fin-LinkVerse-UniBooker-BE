package org.example.apiresource.adapter.out.repository;

import org.example.apiresource.domain.model.entity.ResourceCustomFieldValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceCustomFieldValueRepository extends JpaRepository<ResourceCustomFieldValues, Long> {
    List<ResourceCustomFieldValues> findByResourceIdAndDeletedAtIsNull(Long resourceId);

    Optional<ResourceCustomFieldValues> findByIdAndDeletedAtIsNull(Long id);
}
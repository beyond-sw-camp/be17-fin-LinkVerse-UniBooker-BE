package org.example.unibooker.domain.resource.repository;

import org.example.unibooker.domain.resource.model.ResourceCustomFieldValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomFieldValueRepository extends JpaRepository<ResourceCustomFieldValues, Long> {
}

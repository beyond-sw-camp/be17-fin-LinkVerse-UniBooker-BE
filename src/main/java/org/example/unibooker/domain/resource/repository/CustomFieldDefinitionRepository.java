package org.example.unibooker.domain.resource.repository;

import org.example.unibooker.domain.resource.model.CustomFieldDefinitions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomFieldDefinitionRepository extends JpaRepository<CustomFieldDefinitions, Long> {
}

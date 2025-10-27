package org.example.unibooker.domain.resource.repository;

import org.example.unibooker.domain.resource.model.CustomFieldDefinitions;
import org.example.unibooker.domain.resource.model.CustomFieldSelectDefinitions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomFieldSelectRepository extends JpaRepository<CustomFieldSelectDefinitions, Long> {
    List<CustomFieldSelectDefinitions> findByCustomFieldDefinitionAndDeletedAtIsNull(CustomFieldDefinitions field);
}

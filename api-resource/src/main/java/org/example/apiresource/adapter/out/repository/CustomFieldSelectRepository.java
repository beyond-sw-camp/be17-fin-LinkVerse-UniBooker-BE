package org.example.apiresource.adapter.out.repository;

import org.example.apiresource.domain.model.entity.CustomFieldDefinitions;
import org.example.apiresource.domain.model.entity.CustomFieldSelectDefinitions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomFieldSelectRepository extends JpaRepository<CustomFieldSelectDefinitions, Long> {
    List<CustomFieldSelectDefinitions> findByCustomFieldDefinitionAndDeletedAtIsNull(CustomFieldDefinitions field);
}
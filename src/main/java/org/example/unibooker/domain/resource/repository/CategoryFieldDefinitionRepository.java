package org.example.unibooker.domain.resource.repository;

import org.example.unibooker.domain.resource.model.CategoryFieldDefinitions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryFieldDefinitionRepository extends JpaRepository<CategoryFieldDefinitions, Long> {

}

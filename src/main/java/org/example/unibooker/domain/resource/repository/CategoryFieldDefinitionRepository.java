package org.example.unibooker.domain.resource.repository;

import org.example.unibooker.domain.resource.model.CategoryFieldDefinitions;
import org.example.unibooker.domain.resource.model.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryFieldDefinitionRepository extends JpaRepository<CategoryFieldDefinitions, Long> {
    List<CategoryFieldDefinitions> findByCategory(ServiceCategory category);
}

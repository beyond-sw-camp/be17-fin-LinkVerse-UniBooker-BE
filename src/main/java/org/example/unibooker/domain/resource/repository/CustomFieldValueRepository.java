package org.example.unibooker.domain.resource.repository;

import org.example.unibooker.domain.resource.model.CustomFieldValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomFieldValueRepository extends JpaRepository<CustomFieldValues, Long> {
}

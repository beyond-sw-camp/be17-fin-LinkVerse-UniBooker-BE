package org.example.unibooker.domain.resource.repository;

import org.example.unibooker.domain.resource.model.Resources;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<Resources, Long> {
}

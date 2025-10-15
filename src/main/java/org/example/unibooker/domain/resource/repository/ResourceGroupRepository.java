package org.example.unibooker.domain.resource.repository;

import org.example.unibooker.domain.resource.model.ResourceGroups;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceGroupRepository extends JpaRepository<ResourceGroups, Long> {

}

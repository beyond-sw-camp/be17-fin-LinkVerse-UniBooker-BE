package org.example.unibooker.domain.resource.model;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.unibooker.common.BaseEntity;

@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class CategoryFieldDefinitions extends BaseEntity {
    private String fieldName;
    private String description;
    private CustomDataType dataType;
    private ServiceCategory category;
}

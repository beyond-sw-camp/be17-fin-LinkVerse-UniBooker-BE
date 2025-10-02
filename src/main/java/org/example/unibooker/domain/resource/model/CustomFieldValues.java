package org.example.unibooker.domain.resource.model;

import jakarta.persistence.*;
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
public class CustomFieldValues extends BaseEntity {
    private Long targetId;
    private String fieldValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id")
    private CustomFieldDefinitions customFieldDefinition;
}

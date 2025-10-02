package org.example.unibooker.domain.resource.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class CustomFieldValues {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long targetId;
    private String fieldValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id")
    private CustomFieldDefinitions customFieldDefinition;
}

package org.example.unibooker.domain.resource.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class CustomFieldDefinitions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private CustomeTargetType targetType;
    private Long targetId;
    private String fieldName;
    private CustomDataType dataType;
    private Boolean isRequired;

    @OneToMany(mappedBy = "customFieldDefinition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomFieldValues> customFieldValues = new ArrayList<>();
}

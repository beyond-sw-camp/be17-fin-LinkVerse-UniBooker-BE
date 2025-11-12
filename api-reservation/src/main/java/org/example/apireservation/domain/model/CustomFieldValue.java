package org.example.apireservation.domain.model;

import lombok.*;

import java.util.List;

@Getter
@Builder
public class CustomFieldValue {

    private Long customFieldId;
    private String fieldName;
    private List<String> values;
}

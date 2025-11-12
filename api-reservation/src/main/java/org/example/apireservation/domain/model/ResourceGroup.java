package org.example.apireservation.domain.model;

import lombok.*;

@Getter
@Builder
public class ResourceGroup {
    private Long id;
    private ServiceCategory serviceCategory;
}

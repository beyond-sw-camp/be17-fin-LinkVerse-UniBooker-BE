package org.example.apireservation.adapter.out.external;

import lombok.*;
import org.example.apireservation.domain.model.ServiceCategory;

@Getter
@Setter
public class ResourceGroupInfo {
    private Long id;
    private ServiceCategory serviceCategory;
}

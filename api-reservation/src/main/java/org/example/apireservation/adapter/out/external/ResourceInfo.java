package org.example.apireservation.adapter.out.external;

import lombok.*;
import org.example.apireservation.domain.model.ServiceCategory;

import java.time.LocalDate;

@Getter
@Setter
public class ResourceInfo {
    private Long id;
    private ServiceCategory serviceCategory;
    private LocalDate startDate;
    private LocalDate endDate;
//    private Integer row;
//    private Integer col;
    private Integer timeInterval;
    private Integer capacity;
    private boolean isActive;
}
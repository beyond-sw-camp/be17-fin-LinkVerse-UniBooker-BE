package org.example.apireservation.domain.model;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
public class Resource {
    private Long id;
    private String resourceName;
    private ServiceCategory serviceCategory;
    private LocalDate startDate;
    private LocalDate endDate;
//    private Integer row;
//    private Integer col;
    private Integer timeInterval;
    private Integer capacity;
    private boolean isActive;
}
package org.example.apireservation.domain.model;

import lombok.*;

import java.time.LocalDate;

@Getter
@Builder
public class Resource {
    private Long id;
    private String name;
    private ServiceCategory category;
    private LocalDate startDate;
    private LocalDate endDate;
//    private Integer row;
//    private Integer col;
    private Integer timeInterval;
    private Integer capacity;
    private boolean isActive;

    private String status;
    private String resourceImage;
    private String description;
    private Long resourceGroupId;
    private String resourceGroupName;
}
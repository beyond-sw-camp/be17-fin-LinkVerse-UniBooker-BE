package org.example.unibooker.domain.resource.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.unibooker.common.BaseEntity;
import org.example.unibooker.domain.user.model.Users;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Resources extends BaseEntity {
    private String name;
    private String description;
    private Boolean isActive;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer capacity;
    private Integer row;
    private Integer column;
    private ResourceStatus status;

    // 리소스 그룹
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_group_id")
    private ResourceGroups resourceGroup;

    // 리소스 이미지
    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResourceImages> resourceImages = new ArrayList<>();

    // 생성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Users createdBy;

    // 수정자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private Users updatedBy;
}

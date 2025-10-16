package org.example.unibooker.domain.resource.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.unibooker.common.BaseEntity;
import org.example.unibooker.domain.company.model.Companies;
import org.example.unibooker.domain.user.model.Users;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ResourceGroups extends BaseEntity {
    private String name;
    private String description;
    private String thumbnail;
    private ServiceCategory category;
    private Boolean isAlwaysAvailable; // 상시모집 여부
    private Boolean isActive; // 활성화 여부

    // 리소스
    @OneToMany(mappedBy = "resourceGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Resources> resources = new ArrayList<>();

    // 기업키
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Companies company;

    // 생성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Users createdBy;

    // 수정자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private Users updatedBy;

    // 커스텀 필드
    @OneToMany(mappedBy = "resourceGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomFieldDefinitions> customFieldDefinitions = new ArrayList<>();
}

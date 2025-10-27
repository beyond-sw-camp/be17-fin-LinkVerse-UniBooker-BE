package org.example.unibooker.domain.resource.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.unibooker.common.BaseEntity;
import org.example.unibooker.domain.company.model.entity.Companies;
import org.example.unibooker.domain.user.model.entity.Users;

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
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ServiceCategory category;
    private Boolean isAlwaysAvailable; // 상시모집 여부
    private Boolean isActive; // 활성화 여부

    // 낙관적 락 버전 관리 필드
    @Version
    @Column(nullable = false)
    private Long version = 0L;

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

    // 조회수
    @JoinColumn(name= "view_count")
    private int viewCount;


    // 서비스 그룹 수정 함수
    public void update(String name, String description, String thumbnail, String category, Boolean isAlwaysAvailable, Users updatedBy) {
        if (name != null) this.name = name;
        if (description != null) this.description = description;
        if (thumbnail != null) this.thumbnail = thumbnail;
        if (category != null) this.category = ServiceCategory.valueOf(category);
        if (isAlwaysAvailable != null) this.isAlwaysAvailable = isAlwaysAvailable;
        this.updatedBy = updatedBy;
    }

    public void setUpdatedBy(Users user) {
        this.updatedBy = user;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}

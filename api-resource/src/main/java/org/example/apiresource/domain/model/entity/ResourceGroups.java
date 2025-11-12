package org.example.apiresource.domain.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.apiresource.domain.model.ServiceCategory;
import org.example.common.base.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ResourceGroups extends BaseEntity {
    private String name;
    private String groupCode;  // 서비스 그룹 목록 프론트 구조에 맞춘 추가사항
    private String description;
    private String thumbnail;
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ServiceCategory category;

    @Column(name = "is_always_available")
    private Boolean isAlwaysAvailable; // 상시모집 여부

    @Column(name = "is_active")
    private Boolean isActive; // 활성화 여부

    // 조회수
    @JoinColumn(name= "view_count")
    private int viewCount;

    // 낙관적 락 버전 관리 필드
    @Version
    @Column(nullable = false)
    private Long version = 0L;

    private Long companyId;
    private Long userId;
    private Long createdBy;
    private Long updatedBy;

    // 리소스
    @OneToMany(mappedBy = "resourceGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Resources> resources = new ArrayList<>();

    // 커스텀 필드
    @OneToMany(mappedBy = "resourceGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomFieldDefinitions> customFieldDefinitions = new ArrayList<>();

    // 서비스 그룹 수정 함수
    public void update(String name, String groupCode, String description, String thumbnail, String category, Boolean isAlwaysAvailable, Long userId) {
        if (name != null) this.name = name;
        if (groupCode != null) this.groupCode = groupCode;  // 서비스 그룹 목록 프론트 구조에 맞춘 추가사항
        if (description != null) this.description = description;
        if (thumbnail != null) this.thumbnail = thumbnail;
        if (category != null) this.category = ServiceCategory.valueOf(category);
        if (isAlwaysAvailable != null) this.isAlwaysAvailable = isAlwaysAvailable;
        this.updatedBy = updatedBy;
    }

    public void setUpdatedBy(Long userId) {
        this.updatedBy = userId;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}

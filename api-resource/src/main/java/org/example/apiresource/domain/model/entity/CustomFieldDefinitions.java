package org.example.apiresource.domain.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.apiresource.domain.model.CustomDataType;
import org.example.apiresource.domain.model.CustomTargetType;
import org.example.apiresource.domain.model.dto.CustomFieldDto;
import org.example.common.base.BaseEntity;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class CustomFieldDefinitions extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private CustomTargetType targetType;
    private String fieldName;
    private String description;
    @Enumerated(EnumType.STRING)
    private CustomDataType dataType;
    private Boolean isRequired;

    // 커스텀 필드 선택 항목
    @OneToMany(mappedBy = "customFieldDefinition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomFieldSelectDefinitions> customFieldSelectDefinitions = new ArrayList<>();

    // 서비스 커스텀 필드 값
    @OneToMany(mappedBy = "customFieldDefinition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResourceCustomFieldValues> resourceCustomFieldValues = new ArrayList<>();

    // 사용자 커스텀 필드 값
    @OneToMany(mappedBy = "customFieldDefinition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserCustomFieldValues> userCustomFieldValues = new ArrayList<>();

    // 리소스 그룹
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_group_id")
    private ResourceGroups resourceGroup;

    public void setResourceGroup(ResourceGroups group) {
        resourceGroup = group;
    }

    public void update(CustomFieldDto.CustomFieldReq dto) {
        this.fieldName = dto.getFieldName();
        this.description = dto.getDescription();
        this.dataType = dto.getDataType();
        this.targetType = dto.getTargetType();
        this.isRequired = dto.getRequired();
    }
}
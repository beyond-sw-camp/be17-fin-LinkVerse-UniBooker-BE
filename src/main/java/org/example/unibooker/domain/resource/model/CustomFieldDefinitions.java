package org.example.unibooker.domain.resource.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.unibooker.common.BaseEntity;
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
}

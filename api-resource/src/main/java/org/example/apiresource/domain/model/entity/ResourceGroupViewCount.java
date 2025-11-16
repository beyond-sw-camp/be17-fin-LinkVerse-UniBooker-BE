package org.example.apiresource.domain.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.common.base.BaseEntity;

@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ResourceGroupViewCount extends BaseEntity {
    Long viewCount;

    @ManyToOne
    @JoinColumn(name = "resource_group_id")
    private ResourceGroups resourceGroup;
}
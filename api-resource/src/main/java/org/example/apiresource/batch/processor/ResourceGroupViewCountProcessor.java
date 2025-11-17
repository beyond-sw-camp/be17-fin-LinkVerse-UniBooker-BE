package org.example.apiresource.batch.processor;

import org.example.apiresource.domain.model.entity.ResourceGroupViewCount;
import org.example.apiresource.domain.model.entity.ResourceGroups;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class ResourceGroupViewCountProcessor implements ItemProcessor<ResourceGroups, ResourceGroupViewCount> {

    @Override
    public ResourceGroupViewCount process(ResourceGroups resourceGroup) {

        ResourceGroupViewCount history = ResourceGroupViewCount.builder()
                .resourceGroup(resourceGroup)
                .viewCount((long) resourceGroup.getViewCount())
                .build();

        // viewCount 초기화
        resourceGroup.setViewCount(0);

        return history;
    }
}
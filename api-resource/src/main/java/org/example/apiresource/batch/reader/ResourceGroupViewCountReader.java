package org.example.apiresource.batch.reader;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.example.apiresource.domain.model.entity.ResourceGroups;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResourceGroupViewCountReader {

    private final EntityManagerFactory entityManagerFactory;

    public JpaPagingItemReader<ResourceGroups> resourceGroupViewCountReader() {
        return new JpaPagingItemReaderBuilder<ResourceGroups>()
                .name("resourceGroupViewCountReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT rg FROM ResourceGroups rg WHERE rg.deletedAt IS NULL AND rg.isActive = true")
                .pageSize(100)
                .build();
    }
}
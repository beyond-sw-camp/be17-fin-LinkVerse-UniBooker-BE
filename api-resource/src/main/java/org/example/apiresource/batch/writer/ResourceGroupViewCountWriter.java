package org.example.apiresource.batch.writer;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.example.apiresource.domain.model.entity.ResourceGroupViewCount;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResourceGroupViewCountWriter {

    private final EntityManagerFactory entityManagerFactory;

    public ItemWriter<ResourceGroupViewCount> resourceGroupViewCountWriter() {
        return new JpaItemWriterBuilder<ResourceGroupViewCount>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}
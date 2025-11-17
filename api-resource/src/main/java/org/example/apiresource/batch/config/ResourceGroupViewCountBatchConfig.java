package org.example.apiresource.batch.config;

import lombok.RequiredArgsConstructor;
import org.example.apiresource.batch.processor.ResourceGroupViewCountProcessor;
import org.example.apiresource.batch.reader.ResourceGroupViewCountReader;
import org.example.apiresource.batch.writer.ResourceGroupViewCountWriter;
import org.example.apiresource.domain.model.entity.ResourceGroupViewCount;
import org.example.apiresource.domain.model.entity.ResourceGroups;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class ResourceGroupViewCountBatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final ResourceGroupViewCountReader reader;
    private final ResourceGroupViewCountProcessor processor;
    private final ResourceGroupViewCountWriter writer;


    @Bean
    public Step resourceGroupViewCountStep() {
        return new StepBuilder("resourceGroupViewCountStep", jobRepository)
                .<ResourceGroups, ResourceGroupViewCount>chunk(100, transactionManager)
                .reader(reader.resourceGroupViewCountReader())
                .processor(processor)
                .writer(writer.resourceGroupViewCountWriter())
                .build();
    }

    @Bean
    public Job resourceGroupViewCountJob() {
        return new JobBuilder("resourceGroupViewCountJob", jobRepository)
                .start(resourceGroupViewCountStep())
                .build();
    }
}
package org.example.unibooker.batch.config;

import lombok.RequiredArgsConstructor;
import org.example.unibooker.domain.resource.model.Resources;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

//    Job 정의
//    Job = 배치 단위 작업
//    "resourceStatusJob -> Job 이름"
//    jobRepository -> 배치 실행 정보(DB에 상태 저장 등)를 관리
//    .start(resourceStatusStep) -> Job 시작 시 실행할 Step 지정
//    지금 코드는 하나의 Step으로 구성되어 음.
    @Bean
    public Job resourceStatusJob(Step resourceStatusStep) {
        return new JobBuilder("resourceStatusJob", jobRepository)
                .start(resourceStatusStep)
                .build();
    }


    // Step = Job 안에서 수행되는 실제 처리 단위
    // resourceStatusStep -> Step 이름
    // jobRepository -> Job/Step 상태 관리
    // .chunk(50, transactionManager) -> 천크 단위 처리
    // transactionManager -> 트랜잭션 관리
    // .reader(reader) -> 데이터를 읽는 로직
    // .processor(processor) -> 데이터를 가공/변환하는 로직
    // .writer(writer) -> 데이터를 저장하는 로직
    // Step = Reader -> Processor -> Writer
    @Bean
    public Step resourceStatusStep(ItemReader<Resources> reader,
                                   ItemProcessor<Resources, Resources> processor,
                                   ItemWriter<Resources> writer) {

        return new StepBuilder("resourceStatusStep", jobRepository)
                .<Resources, Resources>chunk(50, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
}


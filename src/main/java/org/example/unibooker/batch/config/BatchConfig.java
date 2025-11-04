package org.example.unibooker.batch.config;

import lombok.RequiredArgsConstructor;
import org.example.unibooker.batch.processor.InactiveAccountProcessor;
import org.example.unibooker.batch.processor.RejectedCompanyProcessor;
import org.example.unibooker.batch.reader.InactiveAccountReader;
import org.example.unibooker.batch.reader.RejectedCompanyReader;
import org.example.unibooker.batch.writer.InactiveAccountWriter;
import org.example.unibooker.batch.writer.RejectedCompanyWriter;
import org.example.unibooker.domain.company.model.entity.Companies;
import org.example.unibooker.domain.resource.model.Resources;
import org.example.unibooker.domain.user.model.entity.Users;
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

/**
 * 배치 처리 설정 (Job, Step 정의)
 * - 스케줄링은 batch/scheduler 패키지에서 관리
 * - 실행 환경은 application.yml에서 설정
 */
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    // ========== Resource Status Job ==========

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

    // ========== Inactive Account Cleanup Job ==========

    /**
     * 미활성 계정 자동 삭제 Job
     * - ADMIN/MANAGER 중 첫 로그인 미완료 계정
     * - 설정된 시간 이상 미활성 계정
     */
    @Bean
    public Job inactiveAccountCleanupJob(Step inactiveAccountCleanupStep) {
        return new JobBuilder("inactiveAccountCleanupJob", jobRepository)
                .start(inactiveAccountCleanupStep)
                .build();
    }

    /**
     * 미활성 계정 삭제 Step
     */
    @Bean
    public Step inactiveAccountCleanupStep(
            InactiveAccountReader inactiveAccountReader,
            InactiveAccountProcessor inactiveAccountProcessor,
            InactiveAccountWriter inactiveAccountWriter) {
        return new StepBuilder("inactiveAccountCleanupStep", jobRepository)
                .<Users, Users>chunk(50, transactionManager)
                .reader(inactiveAccountReader)
                .processor(inactiveAccountProcessor)
                .writer(inactiveAccountWriter)
                .build();
    }

    // ========== Rejected Company Cleanup Job ==========

    /**
     * 거절된 기업 자동 정리 Job
     * - REJECTED 상태 기업
     * - 생성 후 설정된 일수 경과
     * - 연결된 사용자도 함께 삭제
     */
    @Bean
    public Job rejectedCompanyCleanupJob(Step rejectedCompanyCleanupStep) {
        return new JobBuilder("rejectedCompanyCleanupJob", jobRepository)
                .start(rejectedCompanyCleanupStep)
                .build();
    }

    /**
     * 거절된 기업 정리 Step
     */
    @Bean
    public Step rejectedCompanyCleanupStep(
            RejectedCompanyReader rejectedCompanyReader,
            RejectedCompanyProcessor rejectedCompanyProcessor,
            RejectedCompanyWriter rejectedCompanyWriter) {
        return new StepBuilder("rejectedCompanyCleanupStep", jobRepository)
                .<Companies, Companies>chunk(50, transactionManager)
                .reader(rejectedCompanyReader)
                .processor(rejectedCompanyProcessor)
                .writer(rejectedCompanyWriter)
                .build();
    }
}


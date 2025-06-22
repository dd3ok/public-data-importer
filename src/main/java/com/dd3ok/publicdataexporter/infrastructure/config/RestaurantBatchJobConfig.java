package com.dd3ok.publicdataexporter.infrastructure.config;

import com.dd3ok.publicdataexporter.domain.model.Restaurant;
import com.dd3ok.publicdataexporter.infrastructure.adapter.in.file.RestaurantCsvDto;
import com.dd3ok.publicdataexporter.infrastructure.batch.LineRangePartitioner;
import com.dd3ok.publicdataexporter.infrastructure.batch.RestaurantCsvItemReader;
import com.dd3ok.publicdataexporter.infrastructure.batch.RestaurantJdbcItemWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 일반음식점 현황 CSV 파일을 데이터베이스로 이관하는 Spring Batch Job 설정
 *
 * 아키텍처:
 * - Manager-Worker 패턴을 사용한 파티셔닝으로 대용량 데이터 병렬 처리
 * - 멀티스레드 환경에서 안전한 배치 처리
 * - 중복 데이터에 대한 유연한 오류 처리
 *
 * 처리 흐름:
 * 1. LineRangePartitioner가 CSV 파일을 여러 구간으로 분할
 * 2. 각 파티션이 독립적인 스레드에서 Reader -> Processor -> Writer 실행
 * 3. 중복 키 오류 발생 시 해당 레코드를 스킵하고 계속 진행
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RestaurantBatchJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final RestaurantCsvItemReader restaurantCsvItemReader;
    private final RestaurantJdbcItemWriter restaurantJdbcItemWriter;

    /**
     * 청크 크기: 한 번의 트랜잭션으로 처리할 아이템 수
     * 메모리 사용량과 트랜잭션 크기의 균형을 위해 2000으로 설정
     */
    private static final int CHUNK_SIZE = 2000;

    /**
     * 파티션 수: 사용 가능한 CPU 코어 수만큼 병렬 처리
     * 시스템 리소스를 최대한 활용하여 처리 속도 향상
     */
    private static final int GRID_SIZE = Runtime.getRuntime().availableProcessors();

    /**
     * 메인 Job 정의: 전체 배치 작업의 진입점
     */
    @Bean
    public Job restaurantCsvToDbJob(Step managerStep) {
        return new JobBuilder("restaurantCsvToDbJob", jobRepository)
                .start(managerStep)
                .build();
    }

    /**
     * Manager Step: 파티셔닝을 관리하고 Worker Step들을 조율
     */
    @Bean
    public Step managerStep(TaskExecutorPartitionHandler partitionHandler, LineRangePartitioner partitioner) {
        return new StepBuilder("restaurantManagerStep", jobRepository)
                .partitioner("restaurantWorkerStep", partitioner)
                .partitionHandler(partitionHandler)
                .build();
    }

    /**
     * 파티션 핸들러: 각 파티션을 별도 스레드에서 실행하도록 관리
     */
    @Bean
    public TaskExecutorPartitionHandler partitionHandler(Step workerStep, TaskExecutor taskExecutor) {
        TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();
        partitionHandler.setStep(workerStep);
        partitionHandler.setTaskExecutor(taskExecutor);
        partitionHandler.setGridSize(GRID_SIZE);
        return partitionHandler;
    }

    /**
     * 라인 범위 파티셔너: CSV 파일을 라인 단위로 분할
     */
    @Bean
    public LineRangePartitioner lineRangePartitioner() {
        LineRangePartitioner partitioner = new LineRangePartitioner();
        partitioner.setResource(new ClassPathResource("data/restaurant_data.csv"));
        return partitioner;
    }

    /**
     * Worker Step: 실제 데이터 처리를 담당하는 스텝
     * 각 파티션에서 독립적으로 실행되며, Reader -> Processor -> Writer 순서로 처리
     */
    @Bean
    public Step workerStep(
            FlatFileItemReader<RestaurantCsvDto> reader,
            ItemProcessor<RestaurantCsvDto, Restaurant> processor,
            ItemWriter<Restaurant> writer
    ) {
        return new StepBuilder("restaurantWorkerStep", jobRepository)
                .<RestaurantCsvDto, Restaurant>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skip(DuplicateKeyException.class) // 중복 키 오류 시 해당 레코드 스킵
                .skipLimit(1000) // 최대 1000개까지 스킵 허용
                .build();
    }

    /**
     * CSV 파일 Reader Bean
     * StepScope을 통해 각 파티션마다 독립적인 인스턴스 생성
     */
    @Bean
    @StepScope
    public FlatFileItemReader<RestaurantCsvDto> csvFileReader(
            @Value("#{stepExecutionContext['startLine']}") Long startLine,
            @Value("#{stepExecutionContext['endLine']}") Long endLine
    ) {
        return restaurantCsvItemReader.createReader(startLine, endLine);
    }

    /**
     * 데이터베이스 Writer Bean
     * 배치 INSERT로 성능 최적화
     */
    @Bean
    public ItemWriter<Restaurant> restaurantItemWriter() {
        return restaurantJdbcItemWriter.createWriter();
    }

    /**
     * 스레드 풀 설정: 파티션별 병렬 처리를 위한 스레드 관리
     * 코어 수만큼 스레드를 생성
     */
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(GRID_SIZE);
        executor.setMaxPoolSize(GRID_SIZE);
        executor.setThreadNamePrefix("restaurant-batch-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }
}

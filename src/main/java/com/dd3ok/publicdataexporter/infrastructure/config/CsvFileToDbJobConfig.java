package com.dd3ok.publicdataexporter.infrastructure.config;

import com.dd3ok.publicdataexporter.domain.model.Restaurant;
import com.dd3ok.publicdataexporter.infrastructure.adapter.in.file.RestaurantCsvDto;
import com.dd3ok.publicdataexporter.infrastructure.batch.LineRangePartitioner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CsvFileToDbJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;

    private static final int CHUNK_SIZE = 2000;
    private static final int GRID_SIZE = Runtime.getRuntime().availableProcessors(); // 258v cpu = 8c 8t

    /**
     * 전체 배치 작업을 정의하는 최상위 Job Bean 입니다.
     * 파티셔닝을 관리하는 매니저 스텝(managerStep)을 시작점으로 설정합니다.
     */
    @Bean
    public Job csvToDbJob(Step managerStep) {
        return new JobBuilder("csvToDbJob", jobRepository)
                .start(managerStep)
                .build();
    }

    /**
     * Partitioner를 통해 작업을 분할하고,
     * PartitionHandler를 통해 분할된 작업을 병렬로 실행시킴
     */
    @Bean
    public Step managerStep(JobRepository jobRepository, TaskExecutorPartitionHandler partitionHandler, Partitioner partitioner) {
        return new StepBuilder("managerStep", jobRepository)
                .partitioner("workerStep", partitioner)
                .partitionHandler(partitionHandler)
                .build();
    }

    /**
     * 분할된 파티션을 실제로 실행시키는 PartitionHandler
     * 워커 스텝과 스레드 풀을 설정하여 파티션들을 병렬 처리함
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
     * 작업을 분할하는 Partitioner를 정의합니다.
     * @StepScope를 사용하여 Step 실행 시점에 Bean을 생성합니다.
     */
    @Bean
    @StepScope
    public LineRangePartitioner partitioner() {
        LineRangePartitioner partitioner = new LineRangePartitioner();
        partitioner.setResource(new ClassPathResource("data/restaurant_data.csv"));
        return partitioner;
    }

    @Bean
    public Step workerStep(
            FlatFileItemReader<RestaurantCsvDto> reader,
            ItemProcessor<RestaurantCsvDto, Restaurant> processor,
            ItemWriter<Restaurant> writer
    ) {
        return new StepBuilder("workerStep", jobRepository)
                .<RestaurantCsvDto, Restaurant>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant() // 예외 처리 기능 활성화
                .skip(DuplicateKeyException.class) // DuplicateKeyException 발생 시 건너뛰기
                .skipLimit(1000) // 건너뛸 최대 횟수
                .build();
    }

    /**
     * 각 워커 스텝에서 사용할 ItemReader를 정의합니다.
     * @StepScope: 각 스레드(파티션)가 자신만의 독립적인 ItemReader 인스턴스를 갖도록 보장합니다.
     */
    @Bean
    @StepScope
    public FlatFileItemReader<RestaurantCsvDto> csvFileReader(
            @Value("#{stepExecutionContext['startLine']}") Long startLine,
            @Value("#{stepExecutionContext['endLine']}") Long endLine
    ) {
        log.info("Worker: Reading from line {} to {}", startLine, endLine);

        String[] allCsvHeaders = new String[]{
                "번호","개방서비스명","개방서비스아이디","개방자치단체코드","관리번호","인허가일자","인허가취소일자",
                "영업상태구분코드","영업상태명","상세영업상태코드","상세영업상태명","폐업일자","휴업시작일자","휴업종료일자",
                "재개업일자","소재지전화","소재지면적","소재지우편번호","소재지전체주소","도로명전체주소","도로명우편번호",
                "사업장명","최종수정시점","데이터갱신구분","데이터갱신일자","업태구분명","좌표정보(X)","좌표정보(Y)",
                "위생업태명","남성종사자수","여성종사자수","영업장주변구분명","등급구분명","급수시설구분명","총종업원수",
                "본사종업원수","공장사무직종업원수","공장판매직종업원수","공장생산직종업원수","건물소유구분명",
                "보증액","월세액","다중이용업소여부","시설총규모","전통업소지정번호","전통업소주된음식","홈페이지",
                "dummy"
        };

        return new FlatFileItemReaderBuilder<RestaurantCsvDto>()
                .name("csvFileReader")
                .resource(new ClassPathResource("data/restaurant_data.csv"))
                .linesToSkip(startLine.intValue())
                .maxItemCount((int) (endLine - startLine + 1))
                .encoding("EUC-KR")
                .delimited().quoteCharacter('"').names(allCsvHeaders)
                .fieldSetMapper(new CustomRestaurantFieldSetMapper()) // Custom Mapper 사용
                .build();
    }

    @Bean
    public ItemWriter<Restaurant> restaurantItemWriter() {
        String sql = "INSERT INTO restaurant (management_number, licensing_date, close_date, location_phone_number, " +
                "location_area, location_zip_code, full_address, road_name_address, road_name_zip_code, " +
                "business_name, last_modified_at, data_update_type, data_updated_at, industry_type, " +
                "coordinate_x, coordinate_y, " +
                "open_auth_code, male_worker_count, female_worker_count, surrounding_area_type, grade_type, " +
                "water_facility_type, building_ownership_type, monthly_rent, multi_use_business_yn, " +
                "total_facility_size, traditional_business_number, traditional_business_main_food) " +
                "VALUES (:managementNumber, :licensingDate, :closeDate, :locationPhoneNumber, " +
                ":locationArea, :locationZipCode, :fullAddress, :roadNameAddress, :roadNameZipCode, " +
                ":businessName, :lastModifiedAt, :dataUpdateType, :dataUpdatedAt, :industryType, " +
                ":coordinateX, :coordinateY, " +
                ":openAuthCode, :maleWorkerCount, :femaleWorkerCount, :surroundingAreaType, :gradeType, " +
                ":waterFacilityType, :buildingOwnershipType, :monthlyRent, :multiUseBusinessYn, " +
                ":totalFacilitySize, :traditionalBusinessNumber, :traditionalBusinessMainFood)";

        return new JdbcBatchItemWriterBuilder<Restaurant>()
                .dataSource(dataSource)
                .sql(sql)
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .build();
    }

    /**
     * 병렬 처리에 사용할 스레드 풀 설정
     */
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(GRID_SIZE);
        executor.setMaxPoolSize(GRID_SIZE);
        executor.setThreadNamePrefix("partition-thread-");
        executor.initialize();
        return executor;
    }

    /**
     * 한글 CSV 헤더와 영문 DTO Mapper
     */
    private static class CustomRestaurantFieldSetMapper implements FieldSetMapper<RestaurantCsvDto> {
        @Override
        public RestaurantCsvDto mapFieldSet(FieldSet fieldSet) {
            RestaurantCsvDto dto = new RestaurantCsvDto();
            dto.setRecordNumber(fieldSet.readString("번호"));
            dto.setOpenServiceName(fieldSet.readString("개방서비스명"));
            dto.setOpenServiceId(fieldSet.readString("개방서비스아이디"));
            dto.setOpenAuthCode(fieldSet.readString("개방자치단체코드"));
            dto.setManagementNumber(fieldSet.readString("관리번호"));
            dto.setLicenseDate(fieldSet.readString("인허가일자"));
            dto.setLicenseCancelDate(fieldSet.readString("인허가취소일자"));
            dto.setBusinessStatusCode(fieldSet.readString("영업상태구분코드"));
            dto.setBusinessStatusName(fieldSet.readString("영업상태명"));
            dto.setDetailedBusinessStatusCode(fieldSet.readString("상세영업상태코드"));
            dto.setDetailedBusinessStatusName(fieldSet.readString("상세영업상태명"));
            dto.setCloseDate(fieldSet.readString("폐업일자"));
            dto.setSuspensionStartDate(fieldSet.readString("휴업시작일자"));
            dto.setSuspensionEndDate(fieldSet.readString("휴업종료일자"));
            dto.setReopenDate(fieldSet.readString("재개업일자"));
            dto.setLocationPhoneNumber(fieldSet.readString("소재지전화"));
            dto.setLocationArea(fieldSet.readString("소재지면적"));
            dto.setLocationZipCode(fieldSet.readString("소재지우편번호"));
            dto.setFullAddress(fieldSet.readString("소재지전체주소"));
            dto.setRoadNameAddress(fieldSet.readString("도로명전체주소"));
            dto.setRoadNameZipCode(fieldSet.readString("도로명우편번호"));
            dto.setBusinessName(fieldSet.readString("사업장명"));
            dto.setLastModifiedAt(fieldSet.readString("최종수정시점"));
            dto.setDataUpdateType(fieldSet.readString("데이터갱신구분"));
            dto.setDataUpdatedAt(fieldSet.readString("데이터갱신일자"));
            dto.setIndustryType(fieldSet.readString("업태구분명"));
            dto.setCoordinateX(fieldSet.readString("좌표정보(X)"));
            dto.setCoordinateY(fieldSet.readString("좌표정보(Y)"));
            dto.setSanitationIndustryType(fieldSet.readString("위생업태명"));
            dto.setMaleWorkerCount(fieldSet.readString("남성종사자수"));
            dto.setFemaleWorkerCount(fieldSet.readString("여성종사자수"));
            dto.setSurroundingAreaType(fieldSet.readString("영업장주변구분명"));
            dto.setGradeType(fieldSet.readString("등급구분명"));
            dto.setWaterFacilityType(fieldSet.readString("급수시설구분명"));
            dto.setHeadOfficeWorkerCount(fieldSet.readString("본사종업원수"));
            dto.setFactoryOfficeWorkerCount(fieldSet.readString("공장사무직종업원수"));
            dto.setFactorySalesWorkerCount(fieldSet.readString("공장판매직종업원수"));
            dto.setFactoryProductionWorkerCount(fieldSet.readString("공장생산직종업원수"));
            dto.setBuildingOwnershipType(fieldSet.readString("건물소유구분명"));
            dto.setDepositAmount(fieldSet.readString("보증액"));
            dto.setMonthlyRent(fieldSet.readString("월세액"));
            dto.setMultiUseBusinessYn(fieldSet.readString("다중이용업소여부"));
            dto.setTotalFacilitySize(fieldSet.readString("시설총규모"));
            dto.setTraditionalBusinessNumber(fieldSet.readString("전통업소지정번호"));
            dto.setTraditionalBusinessMainFood(fieldSet.readString("전통업소주된음식"));
            dto.setHomepage(fieldSet.readString("홈페이지"));
//             dto.setDummy(fieldSet.readString("dummy"));
            return dto;
        }
    }
}

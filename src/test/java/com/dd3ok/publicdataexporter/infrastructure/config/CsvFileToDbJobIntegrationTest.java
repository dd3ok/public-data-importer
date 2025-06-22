package com.dd3ok.publicdataexporter.infrastructure.config;

import com.dd3ok.publicdataexporter.infrastructure.adapter.in.file.RestaurantCsvDto;
import com.dd3ok.publicdataexporter.infrastructure.batch.LineRangePartitioner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.validation.BindException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
class CsvFileToDbJobIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("restaurantCsvToDbJob")
    private Job csvToDbJob;

    @TestConfiguration
    static class TestJobConfiguration {

        @Bean
        @Primary
        public TaskExecutor taskExecutor() {
            return new SyncTaskExecutor();
        }

        @Bean
        @StepScope
        public LineRangePartitioner partitioner() {
            LineRangePartitioner partitioner = new LineRangePartitioner();
            partitioner.setResource(new ClassPathResource("data/test_data.csv"));
            return partitioner;
        }

        @Bean
        @StepScope
        public FlatFileItemReader<RestaurantCsvDto> csvFileReader(
                @Value("#{stepExecutionContext['startLine']}") Long startLine,
                @Value("#{stepExecutionContext['endLine']}") Long endLine
        ) {
            final String[] actualCsvHeaders = new String[]{
                    "번호", "개방서비스명", "개방서비스아이디", "개방자치단체코드", "관리번호", "인허가일자",
                    "인허가취소일자", "영업상태구분코드", "영업상태명", "상세영업상태코드", "상세영업상태명",
                    "폐업일자", "휴업시작일자", "휴업종료일자", "재개업일자", "소재지전화", "소재지면적",
                    "소재지우편번호", "소재지전체주소", "도로명전체주소", "도로명우편번호", "사업장명",
                    "최종수정시점", "데이터갱신구분", "데이터갱신일자", "업태구분명",
                    "좌표정보x(epsg5174)", "좌표정보y(epsg5174)", "위생업태명", "남성종사자수",
                    "여성종사자수", "영업장주변구분명", "등급구분명", "급수시설구분명", "총직원수",
                    "본사직원수", "공장사무직직원수", "공장판매직직원수", "공장생산직직원수", "건물소유구분명",
                    "보증액", "월세액", "다중이용업소여부", "시설총규모", "전통업소지정번호",
                    "전통업소주된음식", "홈페이지"
            };

            return new FlatFileItemReaderBuilder<RestaurantCsvDto>()
                    .name("csvFileReader")
                    .resource(new ClassPathResource("data/test_data.csv"))
                    .linesToSkip(startLine.intValue())
                    .maxItemCount((int) (endLine - startLine + 1))
                    .encoding("EUC-KR")
                    .delimited()
                    .quoteCharacter('"')
                    .names(actualCsvHeaders)
                    .fieldSetMapper(new FieldSetMapper<RestaurantCsvDto>() {
                        @Override
                        public RestaurantCsvDto mapFieldSet(FieldSet fieldSet) throws BindException {
                            RestaurantCsvDto dto = new RestaurantCsvDto();
                            dto.setOpenServiceId(fieldSet.readString("개방서비스아이디"));
                            dto.setManagementNumber(fieldSet.readString("관리번호"));
                            dto.setLicenseDate(fieldSet.readString("인허가일자"));
                            dto.setBusinessStatusName(fieldSet.readString("영업상태명"));
                            dto.setCloseDate(fieldSet.readString("폐업일자"));
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
                            dto.setCoordinateX(fieldSet.readString("좌표정보x(epsg5174)"));
                            dto.setCoordinateY(fieldSet.readString("좌표정보y(epsg5174)"));
                            dto.setOpenAuthCode(fieldSet.readString("개방서비스명"));
                            dto.setMaleWorkerCount(fieldSet.readString("남성종사자수"));
                            dto.setFemaleWorkerCount(fieldSet.readString("여성종사자수"));
                            dto.setSurroundingAreaType(fieldSet.readString("영업장주변구분명"));
                            dto.setGradeType(fieldSet.readString("등급구분명"));
                            dto.setWaterFacilityType(fieldSet.readString("급수시설구분명"));
                            dto.setBuildingOwnershipType(fieldSet.readString("건물소유구분명"));
                            dto.setMonthlyRent(fieldSet.readString("월세액"));
                            dto.setMultiUseBusinessYn(fieldSet.readString("다중이용업소여부"));
                            dto.setTotalFacilitySize(fieldSet.readString("시설총규모"));
                            dto.setTraditionalBusinessNumber(fieldSet.readString("전통업소지정번호"));
                            dto.setTraditionalBusinessMainFood(fieldSet.readString("전통업소주된음식"));
                            dto.setTotalWorkerCount(fieldSet.readString("총직원수"));
                            return dto;
                        }
                    })
                    .build();
        }
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM restaurant");
    }

    @Test
    @DisplayName("csvToDbJob을 실행하면 테스트 CSV 파일의 데이터가 DB에 정상적으로 저장된다.")
    void csvToDbJob_Success() throws Exception {
        jobLauncherTestUtils.setJob(csvToDbJob);
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM restaurant", Integer.class);
        assertThat(count).isEqualTo(100);
    }
}

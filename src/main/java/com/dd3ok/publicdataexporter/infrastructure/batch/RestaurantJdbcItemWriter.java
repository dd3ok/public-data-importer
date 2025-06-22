package com.dd3ok.publicdataexporter.infrastructure.batch;

import com.dd3ok.publicdataexporter.domain.model.Restaurant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Restaurant 도메인 객체를 데이터베이스에 배치 INSERT하는 Writer
 * - JdbcBatchItemWriter를 사용하여 배치 성능 최적화
 * - 중복 키 오류는 Step 레벨에서 처리 (skip 설정)
 * - Named Parameter를 사용하여 SQL 인젝션 방지
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantJdbcItemWriter {

    private final DataSource dataSource;

    private static final String INSERT_SQL =
            """
            INSERT INTO restaurant (
                management_number, licensing_date, close_date, location_phone_number,
                location_area, location_zip_code, full_address, road_name_address, road_name_zip_code,
                business_name, last_modified_at, data_update_type, data_updated_at, industry_type,
                coordinate_x, coordinate_y, open_auth_code, male_worker_count, female_worker_count,
                surrounding_area_type, grade_type, water_facility_type, building_ownership_type,
                monthly_rent, multi_use_business_yn, total_facility_size, traditional_business_number,
                traditional_business_main_food
            ) VALUES (
                :managementNumber, :licensingDate, :closeDate, :locationPhoneNumber,
                :locationArea, :locationZipCode, :fullAddress, :roadNameAddress, :roadNameZipCode,
                :businessName, :lastModifiedAt, :dataUpdateType, :dataUpdatedAt, :industryType,
                :coordinateX, :coordinateY, :openAuthCode, :maleWorkerCount, :femaleWorkerCount,
                :surroundingAreaType, :gradeType, :waterFacilityType, :buildingOwnershipType,
                :monthlyRent, :multiUseBusinessYn, :totalFacilitySize, :traditionalBusinessNumber,
                :traditionalBusinessMainFood
            )
            """;

    /**
     * 배치 INSERT용 JdbcBatchItemWriter 생성
     * @return 설정된 JdbcBatchItemWriter 인스턴스
     */
    public ItemWriter<Restaurant> createWriter() {
        log.debug("Restaurant JDBC Writer 생성");

        return new JdbcBatchItemWriterBuilder<Restaurant>()
                .dataSource(dataSource)
                .sql(INSERT_SQL)
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .build();
    }
}

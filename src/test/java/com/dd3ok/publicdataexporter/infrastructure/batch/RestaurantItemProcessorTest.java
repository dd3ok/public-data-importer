package com.dd3ok.publicdataexporter.infrastructure.batch;

import com.dd3ok.publicdataexporter.domain.model.Restaurant;
import com.dd3ok.publicdataexporter.infrastructure.adapter.in.file.RestaurantCsvDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RestaurantItemProcessorTest {

    private RestaurantItemProcessor processor;

    @BeforeEach
    void setUp() {
        // 각 테스트 전에 새로운 Processor 인스턴스를 생성
        processor = new RestaurantItemProcessor();
    }

    @Test
    @DisplayName("정상적인 DTO가 주어지면, Restaurant 도메인 객체로 성공적으로 변환된다")
    void process_Success_WhenGivenValidDto() throws Exception {
        // given: 정상적인 입력 데이터
        RestaurantCsvDto dto = new RestaurantCsvDto();
        dto.setManagementNumber("12345-6789");
        dto.setBusinessName("테스트 식당");
        dto.setLicenseDate("2023-01-01");
        dto.setCloseDate("2025-12-31");
        dto.setLocationArea("100.5");
        dto.setCoordinateX("127.12345");

        // when: processor 실행
        Restaurant result = processor.process(dto);

        // then: 결과 검증
        assertThat(result).isNotNull();
        assertThat(result.getManagementNumber()).isEqualTo("12345-6789");
        assertThat(result.getBusinessName()).isEqualTo("테스트 식당");
        assertThat(result.getLicensingDate()).isEqualTo(LocalDate.of(2023, 1, 1));
        assertThat(result.getCloseDate()).isEqualTo(LocalDate.of(2025, 12, 31));
        assertThat(result.getLocationArea()).isEqualTo(100.5);
        assertThat(result.getCoordinateX()).isEqualTo(new BigDecimal("127.12345"));
    }

    @Test
    @DisplayName("관리번호가 비어있으면, null을 반환하여 해당 데이터를 스킵 처리한다")
    void process_ReturnsNull_WhenManagementNumberIsEmpty() throws Exception {
        // given: 관리번호가 없는 데이터
        RestaurantCsvDto dto = new RestaurantCsvDto();
        dto.setManagementNumber(""); // 또는 null

        // when: processor 실행
        Restaurant result = processor.process(dto);

        // then: 결과가 null 이어야 함
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("날짜 형식이 잘못되었을 경우, 해당 필드는 null로 처리되고 예외는 발생하지 않는다")
    void process_SetsFieldToNull_WhenDateFormatIsInvalid() throws Exception {
        // given: 날짜 형식이 잘못된 데이터
        RestaurantCsvDto dto = new RestaurantCsvDto();
        dto.setManagementNumber("12345-6789");
        dto.setLicenseDate("이건 날짜가 아님"); // 잘못된 형식

        // when: processor 실행
        Restaurant result = processor.process(dto);

        // then: 해당 필드만 null로 처리됨
        assertThat(result).isNotNull();
        assertThat(result.getManagementNumber()).isEqualTo("12345-6789");
        assertThat(result.getLicensingDate()).isNull();
    }
}
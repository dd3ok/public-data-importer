package com.dd3ok.publicdataexporter.infrastructure.batch;

import com.dd3ok.publicdataexporter.domain.model.Restaurant;
import com.dd3ok.publicdataexporter.infrastructure.adapter.in.file.RestaurantCsvDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

// RestaurantCsvDto를 입력받아 Restaurant 도메인 객체로 변환합니다. (타입 변환, 포맷 등)
@Slf4j
@Component
public class RestaurantItemProcessor implements ItemProcessor<RestaurantCsvDto, Restaurant> {

    // CSV 파일의 다양한 날짜/시간 형식을 처리하기 위한 포매터
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss") // 초까지 있는 경우
            .appendOptional(new DateTimeFormatterBuilder() // .S (밀리초)가 있을 수도 없을 수도
                    .appendLiteral(".")
                    .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, false) // 1~9자리 밀리초
                    .toFormatter())
            .toFormatter();

    @Override
    public Restaurant process(@NonNull final RestaurantCsvDto dto) throws Exception {
        // 1. 핵심 데이터(관리번호) 유효성 검증
        if (!StringUtils.hasText(dto.getManagementNumber())) {
            log.warn("Skipping record due to empty management number. DTO: {}", dto);
            return null; // null 반환 시, 해당 아이템은 Writer로 넘어가지 않음 (자동 Skip)
        }

        try {
            // 2. DTO -> Domain 객체로 변환
            // 47개 필드 중 사용할 필드만 선택하여 변환
            return Restaurant.builder()
                    .managementNumber(dto.getManagementNumber())
                    .licensingDate(parseLocalDate(dto.getLicenseDate()))
                    .closeDate(parseLocalDate(dto.getCloseDate()))
                    .locationPhoneNumber(dto.getLocationPhoneNumber())
                    .locationArea(parseDouble(dto.getLocationArea()))
                    .locationZipCode(dto.getLocationZipCode())
                    .fullAddress(dto.getFullAddress())
                    .roadNameAddress(dto.getRoadNameAddress())
                    .roadNameZipCode(dto.getRoadNameZipCode())
                    .businessName(dto.getBusinessName())
                    .lastModifiedAt(parseLocalDateTime(dto.getLastModifiedAt()))
                    .dataUpdateType(dto.getDataUpdateType())
                    .dataUpdatedAt(parseLocalDateTime(dto.getDataUpdatedAt()))
                    .industryType(dto.getIndustryType())
                    .coordinateX(parseBigDecimal(dto.getCoordinateX()))
                    .coordinateY(parseBigDecimal(dto.getCoordinateY()))
                    .openAuthCode(dto.getOpenAuthCode())
                    .maleWorkerCount(parseInteger(dto.getMaleWorkerCount()))
                    .femaleWorkerCount(parseInteger(dto.getFemaleWorkerCount()))
                    .surroundingAreaType(dto.getSurroundingAreaType())
                    .gradeType(dto.getGradeType())
                    .waterFacilityType(dto.getWaterFacilityType())
                    .buildingOwnershipType(dto.getBuildingOwnershipType())
                    .monthlyRent(parseInteger(dto.getMonthlyRent()))
                    .multiUseBusinessYn(dto.getMultiUseBusinessYn())
                    .totalFacilitySize(parseDouble(dto.getTotalFacilitySize()))
                    .traditionalBusinessNumber(dto.getTraditionalBusinessNumber())
                    .traditionalBusinessMainFood(dto.getTraditionalBusinessMainFood())
                    .build();
        } catch (Exception e) {
            // 3. 데이터 파싱 중 예외 발생 시, 로그 기록 후 해당 데이터 스킵
            log.error("Error processing DTO with managementNumber: {}. Error: {}. Skipping record.",
                    dto.getManagementNumber(), e.getMessage());
            return null;
        }
    }

    private LocalDate parseLocalDate(String dateStr) {
        if (!StringUtils.hasText(dateStr)) return null;
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("Invalid date format: '{}'. Returning null.", dateStr);
            return null;
        }
    }

    private LocalDateTime parseLocalDateTime(String dateTimeStr) {
        if (!StringUtils.hasText(dateTimeStr)) return null;
        try {
            // .S 패턴은 밀리초가 없는 경우도 파싱 가능
            return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("Invalid datetime format: '{}'. Returning null.", dateTimeStr);
            return null;
        }
    }

    private Double parseDouble(String value) {
        return StringUtils.hasText(value) ? Double.parseDouble(value) : null;
    }

    private BigDecimal parseBigDecimal(String value) {
        return StringUtils.hasText(value) ? new BigDecimal(value) : null;
    }

    private Integer parseInteger(String value) {
        return StringUtils.hasText(value) ? Integer.parseInt(value) : null;
    }
}

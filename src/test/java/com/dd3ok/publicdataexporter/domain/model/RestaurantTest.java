package com.dd3ok.publicdataexporter.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RestaurantTest {

    @Test
    @DisplayName("Restaurant 빌더로 객체 생성")
    void create_restaurant_with_builder_successfully() {
        // given
        String managementNumber = "3210000-101-2002-00018";
        String businessName = "테스트치킨";
        LocalDate closeDate = LocalDate.of(2022, 12, 31);
        BigDecimal coordinateX = new BigDecimal("127.123456789");

        // when
        Restaurant restaurant = Restaurant.builder()
                .managementNumber(managementNumber)
                .businessName(businessName)
                .closeDate(closeDate)
                .licensingDate(LocalDate.of(2002, 3, 15))
                .locationPhoneNumber("02-1234-5678")
                .locationArea(50.5)
                .fullAddress("경기도 성남시 분당구 삼평동 629")
                .roadNameAddress("경기도 성남시 분당구 판교역로 235")
                .industryType("한식")
                .coordinateX(coordinateX)
                .lastModifiedAt(LocalDateTime.now())
                .build();

        // then
        assertThat(restaurant).isNotNull();
        assertThat(restaurant.getManagementNumber()).isEqualTo(managementNumber);
        assertThat(restaurant.getBusinessName()).isEqualTo(businessName);
        assertThat(restaurant.getCloseDate()).isEqualTo(closeDate);
        assertThat(restaurant.getCoordinateX().compareTo(coordinateX)).isEqualTo(0);
    }
}

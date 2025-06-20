package com.dd3ok.publicdataexporter.application.model;

import com.dd3ok.publicdataexporter.domain.model.Restaurant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("Restaurant 도메인 모델 테스트")
class RestaurantTest {

    @Test
    @DisplayName("정상적인 데이터로 Restaurant 생성 성공")
    void createRestaurant_Success() {
        // given
        String serviceName = "전국일반음식점표준데이터";
        String serviceId = "6260000-2021-0001";
        String managementNumber = "2021-6260000-000001";
        String businessName = "맛있는 식당";

        // when
        Restaurant restaurant = Restaurant.of(serviceName, serviceId, managementNumber, businessName)
                .localGovernmentCode("6260000")
                .businessStatusCode("01")
                .businessStatusName("영업")
                .locationFullAddress("부산광역시 해운대구 우동 123-1")
                .roadNameFullAddress("부산광역시 해운대구 해운대로 123")
                .locationPostalCode("48094")
                .coordinateX("129.1652")
                .coordinateY("35.1584")
                .sanitationBusinessTypeName("일반음식점")
                .build();

        // then
        assertAll(
                () -> assertThat(restaurant.getServiceName()).isEqualTo(serviceName),
                () -> assertThat(restaurant.getServiceId()).isEqualTo(serviceId),
                () -> assertThat(restaurant.getLocalGovernmentCode()).isEqualTo("6260000"),
                () -> assertThat(restaurant.getManagementNumber()).isEqualTo(managementNumber),
                () -> assertThat(restaurant.getBusinessName()).isEqualTo(businessName),
                () -> assertThat(restaurant.getBusinessStatusCode()).isEqualTo("01"),
                () -> assertThat(restaurant.getBusinessStatusName()).isEqualTo("영업"),
                () -> assertThat(restaurant.getLocationFullAddress()).isEqualTo("부산광역시 해운대구 우동 123-1"),
                () -> assertThat(restaurant.getRoadNameFullAddress()).isEqualTo("부산광역시 해운대구 해운대로 123"),
                () -> assertThat(restaurant.getCoordinateX()).isEqualTo("129.1652"),
                () -> assertThat(restaurant.getCoordinateY()).isEqualTo("35.1584"),
                () -> assertThat(restaurant.getSanitationBusinessTypeName()).isEqualTo("일반음식점"),
                () -> assertThat(restaurant.getCreatedAt()).isNotNull(),
                () -> assertThat(restaurant.getUpdatedAt()).isNotNull()
        );
    }

    @Test
    @DisplayName("서비스명이 null이면 예외 발생")
    void validateServiceName_Null_ThrowException() {
        // when & then
        assertThatThrownBy(() -> Restaurant.of(null, "test-id", "test-mgmt", "test-business"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("서비스명은 필수입니다.");
    }

    @Test
    @DisplayName("최소 필수 필드만으로 Restaurant 생성 가능")
    void createRestaurant_MinimalFields_Success() {
        // given
        String serviceName = "전국일반음식점표준데이터";
        String serviceId = "6260000-2021-0001";
        String managementNumber = "2021-6260000-000001";
        String businessName = "맛있는 식당";

        // when
        Restaurant restaurant = Restaurant.of(serviceName, serviceId, managementNumber, businessName)
                .build();

        // then
        assertAll(
                () -> assertThat(restaurant.getServiceName()).isEqualTo(serviceName),
                () -> assertThat(restaurant.getServiceId()).isEqualTo(serviceId),
                () -> assertThat(restaurant.getManagementNumber()).isEqualTo(managementNumber),
                () -> assertThat(restaurant.getBusinessName()).isEqualTo(businessName),
                () -> assertThat(restaurant.getLocalGovernmentCode()).isNull(),
                () -> assertThat(restaurant.getLocationFullAddress()).isNull(),
                () -> assertThat(restaurant.getCreatedAt()).isNotNull(),
                () -> assertThat(restaurant.getUpdatedAt()).isNotNull()
        );
    }
}

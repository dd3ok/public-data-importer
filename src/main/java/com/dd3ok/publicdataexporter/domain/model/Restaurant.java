package com.dd3ok.publicdataexporter.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class Restaurant {
    // 기본 정보
    private final Long id;
    private final String serviceName;
    private final String serviceId;
    private final String localGovernmentCode;
    private final String managementNumber;

    // 인허가 정보
    private final String licenseDate;
    private final String licenseCancelDate;
    private final String businessStatusCode;
    private final String businessStatusName;
    private final String detailBusinessStatusCode;
    private final String detailBusinessStatusName;

    // 영업 상태 정보
    private final String closureDate;
    private final String suspensionStartDate;
    private final String suspensionEndDate;
    private final String reopenDate;

    // 소재지 정보
    private final String locationPhone;
    private final String locationArea;
    private final String locationPostalCode;
    private final String locationFullAddress;

    // 도로명 정보
    private final String roadNameFullAddress;
    private final String roadNamePostalCode;
    private final String businessName;

    // 시스템 정보
    private final String lastModifiedTime;
    private final String dataUpdateType;
    private final String dataUpdateDate;

    // 업태 정보
    private final String businessTypeName;
    private final String coordinateX;
    private final String coordinateY;
    private final String sanitationBusinessTypeName;

    // 시스템 필드
    @Builder.Default
    private final LocalDateTime createdAt = LocalDateTime.now();
    @Builder.Default
    private final LocalDateTime updatedAt = LocalDateTime.now();

    public static RestaurantBuilder of(String serviceName, String serviceId,
                                       String managementNumber, String businessName) {
        return Restaurant.builder()
                .serviceName(validateServiceName(serviceName))
                .serviceId(validateServiceId(serviceId))
                .managementNumber(validateManagementNumber(managementNumber))
                .businessName(validateBusinessName(businessName));
    }

    private static String validateServiceName(String serviceName) {
        if (serviceName == null || serviceName.trim().isEmpty()) {
            throw new IllegalArgumentException("서비스명은 필수입니다.");
        }
        return serviceName.trim();
    }

    private static String validateServiceId(String serviceId) {
        if (serviceId == null || serviceId.trim().isEmpty()) {
            throw new IllegalArgumentException("서비스ID는 필수입니다.");
        }
        return serviceId.trim();
    }

    private static String validateManagementNumber(String managementNumber) {
        if (managementNumber == null || managementNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("관리번호는 필수입니다.");
        }
        return managementNumber.trim();
    }

    private static String validateBusinessName(String businessName) {
        if (businessName == null || businessName.trim().isEmpty()) {
            throw new IllegalArgumentException("사업장명은 필수입니다.");
        }
        return businessName.trim();
    }
}

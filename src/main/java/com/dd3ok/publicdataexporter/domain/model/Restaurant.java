package com.dd3ok.publicdataexporter.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@ToString
@NoArgsConstructor
public class Restaurant {

    private Long id;

    private String managementNumber;     // 관리번호
    private LocalDate licensingDate;       // 인허가일자
    private LocalDate closeDate;           // 폐업일자
    private String locationPhoneNumber;    // 소재지전화
    private Double locationArea;         // 소재지면적
    private String locationZipCode;      // 소재지우편번호
    private String fullAddress;            // 소재지전체주소
    private String roadNameAddress;        // 도로명전체주소
    private String roadNameZipCode;      // 도로명우편번호
    private String businessName;           // 사업장명
    private LocalDateTime lastModifiedAt;  // 최종수정시점
    private String dataUpdateType;       // 데이터갱신구분 (I, U)
    private LocalDateTime dataUpdatedAt;   // 데이터갱신일자
    private String industryType;           // 업태구분명
    private BigDecimal coordinateX;        // 좌표정보(X)
    private BigDecimal coordinateY;        // 좌표정보(Y)
    private String openAuthCode;                // 개방자치단체코드
    private Integer maleWorkerCount;            // 남성종사자수
    private Integer femaleWorkerCount;          // 여성종사자수
    private String surroundingAreaType;         // 영업장주변구분명
    private String gradeType;                   // 등급구분명 
    private String waterFacilityType;           // 급수시설구분명 
    private String buildingOwnershipType;       // 건물소유구분명 
    private Integer monthlyRent;                // 월세액
    private String multiUseBusinessYn;          // 다중이용업소여부 
    private Double totalFacilitySize;           // 시설총규모
    private String traditionalBusinessNumber;   // 전통업소지정번호 
    private String traditionalBusinessMainFood; // 전통업소주된음식 

    @Builder
    public Restaurant(String managementNumber, LocalDate licensingDate, LocalDate closeDate,
                      String locationPhoneNumber, Double locationArea, String locationZipCode,
                      String fullAddress, String roadNameAddress, String roadNameZipCode,
                      String businessName, LocalDateTime lastModifiedAt, String dataUpdateType,
                      LocalDateTime dataUpdatedAt, String industryType, BigDecimal coordinateX,
                      BigDecimal coordinateY,
                      String openAuthCode, Integer maleWorkerCount, Integer femaleWorkerCount,
                      String surroundingAreaType, String gradeType, String waterFacilityType,
                      String buildingOwnershipType, Integer monthlyRent, String multiUseBusinessYn,
                      Double totalFacilitySize, String traditionalBusinessNumber,
                      String traditionalBusinessMainFood) {
        this.managementNumber = managementNumber;
        this.licensingDate = licensingDate;
        this.closeDate = closeDate;
        this.locationPhoneNumber = locationPhoneNumber;
        this.locationArea = locationArea;
        this.locationZipCode = locationZipCode;
        this.fullAddress = fullAddress;
        this.roadNameAddress = roadNameAddress;
        this.roadNameZipCode = roadNameZipCode;
        this.businessName = businessName;
        this.lastModifiedAt = lastModifiedAt;
        this.dataUpdateType = dataUpdateType;
        this.dataUpdatedAt = dataUpdatedAt;
        this.industryType = industryType;
        this.coordinateX = coordinateX;
        this.coordinateY = coordinateY;
        this.openAuthCode = openAuthCode;
        this.maleWorkerCount = maleWorkerCount;
        this.femaleWorkerCount = femaleWorkerCount;
        this.surroundingAreaType = surroundingAreaType;
        this.gradeType = gradeType;
        this.waterFacilityType = waterFacilityType;
        this.buildingOwnershipType = buildingOwnershipType;
        this.monthlyRent = monthlyRent;
        this.multiUseBusinessYn = multiUseBusinessYn;
        this.totalFacilitySize = totalFacilitySize;
        this.traditionalBusinessNumber = traditionalBusinessNumber;
        this.traditionalBusinessMainFood = traditionalBusinessMainFood;
    }
}
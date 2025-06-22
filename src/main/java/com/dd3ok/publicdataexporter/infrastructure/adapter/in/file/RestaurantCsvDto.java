package com.dd3ok.publicdataexporter.infrastructure.adapter.in.file;

import lombok.Data;

// CSV 파일의 row를 임시로 담는 DTO입니다.
@Data
public class RestaurantCsvDto {
    private String recordNumber; // 번호
    private String openServiceName; // 개방서비스명
    private String openServiceId; // 개방서비스아이디
    private String openAuthCode; // 개방자치단체코드
    private String managementNumber; // 관리번호
    private String licenseDate; // 인허가일자
    private String licenseCancelDate; // 인허가취소일자
    private String businessStatusCode; // 영업상태구분코드
    private String businessStatusName; // 영업상태명
    private String detailedBusinessStatusCode; // 상세영업상태코드
    private String detailedBusinessStatusName; // 상세영업상태명
    private String closeDate; // 폐업일자
    private String suspensionStartDate; // 휴업시작일자
    private String suspensionEndDate; // 휴업종료일자
    private String reopenDate; // 재개업일자
    private String locationPhoneNumber; // 소재지전화
    private String locationArea; // 소재지면적
    private String locationZipCode; // 소재지우편번호
    private String fullAddress; // 소재지전체주소
    private String roadNameAddress; // 도로명전체주소
    private String roadNameZipCode; // 도로명우편번호
    private String businessName; // 사업장명
    private String lastModifiedAt; // 최종수정시점
    private String dataUpdateType; // 데이터갱신구분
    private String dataUpdatedAt; // 데이터갱신일자
    private String industryType; // 업태구분명
    private String coordinateX; // 좌표정보(X)
    private String coordinateY; // 좌표정보(Y)
    private String sanitationIndustryType; // 위생업태명
    private String maleWorkerCount; // 남성종사자수
    private String femaleWorkerCount; // 여성종사자수
    private String surroundingAreaType; // 영업장주변구분명
    private String gradeType; // 등급구분명
    private String waterFacilityType; // 급수시설구분명
    private String totalWorkerCount; // 총종업원수
    private String headOfficeWorkerCount; // 본사종업원수
    private String factoryOfficeWorkerCount; // 공장사무직종업원수
    private String factorySalesWorkerCount; // 공장판매직종업원수
    private String factoryProductionWorkerCount; // 공장생산직종업원수
    private String buildingOwnershipType; // 건물소유구분명
    private String depositAmount; // 보증액
    private String monthlyRent; // 월세액
    private String multiUseBusinessYn; // 다중이용업소여부
    private String totalFacilitySize; // 시설총규모
    private String traditionalBusinessNumber; // 전통업소지정번호
    private String traditionalBusinessMainFood; // 전통업소주된음식
    private String homepage; // 홈페이지
//    private String col48;
}

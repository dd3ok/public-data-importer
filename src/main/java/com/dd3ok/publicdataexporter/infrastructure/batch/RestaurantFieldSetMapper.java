package com.dd3ok.publicdataexporter.infrastructure.batch;

import com.dd3ok.publicdataexporter.infrastructure.adapter.in.file.RestaurantCsvDto;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.stereotype.Component;

/**
 * CSV 파일의 각 라인을 RestaurantCsvDto 객체로 매핑하는 클래스
 * 총 47개 컬럼의 데이터를 처리 (마지막 dummy 컬럼 포함)
 */
@Component
public class RestaurantFieldSetMapper implements FieldSetMapper<RestaurantCsvDto> {

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
        dto.setHeadOfficeWorkerCount(fieldSet.readString("본사종업원수"));
        dto.setFactoryOfficeWorkerCount(fieldSet.readString("공장사무직종업원수"));
        dto.setFactorySalesWorkerCount(fieldSet.readString("공장판매직종업원수"));
        dto.setFactoryProductionWorkerCount(fieldSet.readString("공장생산직종업원수"));
        dto.setSurroundingAreaType(fieldSet.readString("영업장주변구분명"));
        dto.setGradeType(fieldSet.readString("등급구분명"));
        dto.setWaterFacilityType(fieldSet.readString("급수시설구분명"));
        dto.setBuildingOwnershipType(fieldSet.readString("건물소유구분명"));
        dto.setDepositAmount(fieldSet.readString("보증액"));
        dto.setMonthlyRent(fieldSet.readString("월세액"));
        dto.setMultiUseBusinessYn(fieldSet.readString("다중이용업소여부"));
        dto.setTotalFacilitySize(fieldSet.readString("시설총규모"));
        dto.setTraditionalBusinessNumber(fieldSet.readString("전통업소지정번호"));
        dto.setTraditionalBusinessMainFood(fieldSet.readString("전통업소주된음식"));
        dto.setHomepage(fieldSet.readString("홈페이지"));

        return dto;
    }
}

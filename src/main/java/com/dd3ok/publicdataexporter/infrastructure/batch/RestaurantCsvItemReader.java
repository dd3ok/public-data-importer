package com.dd3ok.publicdataexporter.infrastructure.batch;

import com.dd3ok.publicdataexporter.infrastructure.adapter.in.file.RestaurantCsvDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * 일반음식점 현황 CSV 파일을 읽어 RestaurantCsvDto 객체로 변환하는 Reader
 * - 멀티스레드 파티셔닝을 지원하여 대용량 파일 처리 최적화
 * - EUC-KR 인코딩으로 한글 데이터 처리
 * - CSV 파일의 특정 라인 범위만 읽도록 구성 가능
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantCsvItemReader {
    
    private final RestaurantFieldSetMapper fieldSetMapper;
    
    /**
     * CSV 파일의 헤더 정의 (총 47개 컬럼)
     * 공공데이터포털 표준 형식에 따른 컬럼 순서
     */
    private static final String[] CSV_HEADERS = {
        "번호","개방서비스명","개방서비스아이디","개방자치단체코드","관리번호",
        "인허가일자","인허가취소일자","영업상태구분코드","영업상태명","상세영업상태코드","상세영업상태명",
        "폐업일자","휴업시작일자","휴업종료일자","재개업일자",
        "소재지전화","소재지면적","소재지우편번호","소재지전체주소","도로명전체주소","도로명우편번호",
        "사업장명","최종수정시점","데이터갱신구분","데이터갱신일자","업태구분명",
        "좌표정보(X)","좌표정보(Y)","위생업태명",
        "남성종사자수","여성종사자수","영업장주변구분명","등급구분명","급수시설구분명","총종업원수",
        "본사종업원수","공장사무직종업원수","공장판매직종업원수","공장생산직종업원수",
        "건물소유구분명","보증액","월세액","다중이용업소여부","시설총규모",
        "전통업소지정번호","전통업소주된음식","홈페이지",
        "dummy" // CSV 파일 끝의 빈 컬럼 처리용
    };
    
    /**
     * 파티션별로 CSV 파일의 특정 범위를 읽는 Reader 생성
     * 
     * @param startLine 읽기 시작할 라인 번호 (0부터 시작)
     * @param endLine 읽기 종료할 라인 번호 (포함)
     * @return 설정된 범위의 데이터를 읽는 FlatFileItemReader
     */
    @StepScope
    public FlatFileItemReader<RestaurantCsvDto> createReader(
            @Value("#{stepExecutionContext['startLine']}") Long startLine,
            @Value("#{stepExecutionContext['endLine']}") Long endLine) {
        
        log.info("CSV Reader 생성 - 처리 범위: {} ~ {} 라인", startLine, endLine);
        
        return new FlatFileItemReaderBuilder<RestaurantCsvDto>()
                .name("restaurantCsvReader")
                .resource(new ClassPathResource("data/restaurant_data.csv"))
                .linesToSkip(startLine.intValue()) // 파티션 시작 라인까지 스킵
                .maxItemCount((int) (endLine - startLine + 1)) // 파티션 크기만큼 제한
                .encoding("EUC-KR") // 공공데이터 표준 인코딩
                .delimited()
                .quoteCharacter('"') // CSV 필드 따옴표 처리
                .names(CSV_HEADERS)
                .fieldSetMapper(fieldSetMapper)
                .build();
    }
}

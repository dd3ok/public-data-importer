# 전국 일반음식점 데이터 마이그레이션

## 1. 프로젝트 개요

약 221만 건에 달하는 '전국일반음식점표준데이터' CSV 파일을 읽어, 필요한 데이터를 정제 및 변환한 후 MySQL 데이터베이스에 효율적으로 적재하는 Spring Batch 애플리케이션입니다.

대용량 데이터 처리 성능을 극대화하기 위해 **파티셔닝(Partitioning)** 기법을 도입하여 병렬 처리를 구현했으며, 안정적인 운영을 위해 견고한 예외 처리 및 재시작 기능을 갖추었습니다.

## 2. 주요 기술 스택

- **Language:** Java 17
- **Framework:** Spring Boot, Spring Batch
- **Database:** MySQL 8.0
- **Build Tool:** Gradle
- **Library:** Lombok

## 3. 핵심 기능 및 설계 결정

### 3-1. 파티셔닝(Partitioning) 기반 병렬 처리
- **Manager-Worker 구조:** 작업을 분할하는 `Manager Step`과 실제 처리를 담당하는 `Worker Step`으로 역할을 분리하여 파티셔닝을 구현했습니다.
- **동적 Grid Size:** `Runtime.getRuntime().availableProcessors()`를 사용하여 실행 환경의 CPU 코어 수에 맞춰 파티션 개수를 동적으로 설정, 시스템 리소스를 최적으로 활용합니다.
- **`LineRangePartitioner`:** CSV 파일을 라인 수 기준으로 분할하여 각 워커 스텝에 할당합니다.
- **멀티스레딩:** `ThreadPoolTaskExecutor`를 사용하여 `GRID_SIZE`만큼의 스레드를 생성하고, 각 파티션을 독립적인 스레드에서 동시에 처리함으로써 처리 속도를 비약적으로 향상시켰습니다.

### 3-2. 데이터 처리 파이프라인 (Reader -> Processor -> Writer)
- **Reader:** `FlatFileItemReader`를 사용하며, 한글 헤더와 영문 DTO 필드 간의 명확한 매핑을 위해 `CustomFieldSetMapper`를 구현했습니다. `EUC-KR` 인코딩 문제를 해결했습니다.
- **Processor:** `ItemProcessor`에서 48개의 Raw 데이터 중 필요한 17개의 핵심 데이터만 선택하고, `String` 타입을 `LocalDate`, `BigDecimal` 등 올바른 데이터 타입으로 변환하여 데이터의 품질과 무결성을 보장합니다.
- **Writer:** 대용량 데이터 쓰기에 가장 효율적인 `JdbcBatchItemWriter`를 사용하여, JDBC의 Batch Update 기능으로 DB 입력 성능을 극대화했습니다.

### 3-3. 예외 처리 및 무결성 보장
- **`faultTolerant().skip()`:** 데이터 저장 시 발생하는 `DuplicateKeyException` (중복 키 에러)을 감지하면, 해당 레코드를 건너뛰고 배치가 중단되지 않도록 설정하여 안정성을 높였습니다.
- **`UNIQUE` 제약조건:** 데이터베이스 테이블의 `management_number` 컬럼에 `UNIQUE` 키를 설정하여 데이터 중복을 원천적으로 방지합니다.

### 3-4. 의미 없는 컬럼은 저장하지 않음
- **개방서비스명,개방서비스아이디** 등 모두 동일 데이터가 들어 있을 경우 건너뛰어 성능 최적화를 했습니다.

## 4. 실행 방법

### 4-1. 전제 조건
- Java 17 이상 설치
- MySQL 서버 실행 중

### 4-2. 애플리케이션 설정
`src/main/resources/application.yml` 파일의 `datasource` 정보를 본인의 DB 환경에 맞게 수정합니다.

````
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/restaurant_batch
    username: batch_user
    password: your_password
````

### 4-3. 데이터 파일 준비
다운로드한 `전국일반음식점표준데이터.csv` 파일을 `src/main/resources/data/` 폴더에 `restaurant_data.csv` 이름으로 위치시킵니다.

### 4-4. 애플리케이션 실행
애플리케이션이 시작되면 자동으로 DB 스키마 생성 및 배치 작업이 실행됩니다.

## 5. 성능 테스트 결과

### 테스트 환경

-   **CPU**: 8 코어
-   **Partitioning Grid Size (스레드 수)**: 8
-   **총 처리 데이터**: 약 2,211,311 건
-   **변경 변수**: `Chunk` 사이즈 (1000, 2000, 5000)

### 실험 결과

| Chunk 사이즈 | 총 소요 시간    | 평균 처리 속도 (TPS)  | 분석                                            |
| :----------- |:-----------|:----------------|:----------------------------------------------|
| 1000         | 10분 15초    | **약 3,613 건/초** | 성능 개선 시작, 여전히 최적점에 도달하지 못함                    |
| **2000**     | **9분 36초** | **약 3,839 건/초** | **최적점 (Sweet Spot)**: I/O 효율과 메모리 부담의 이상적인 균형 |
| 5000         | 9분 52초     | **약 3,735 건/초** | 사용 메모리 증가해 chunk 2000보다 성능 저하                 |

따라서, 현재 시스템 환경에서 **I/O 효율을 극대화하면서도 메모리 부담을 감당할 수 있는 최적의 균형점(Sweet Spot)은 Chunk 사이즈 2000**으로 결론 내렸습니다.

## 6. 어려웠던 점과 해결 과정

### 6-1. 많은 파싱 에러 (`FlatFileParseException`)
가장 많은 시간을 소요했던 문제입니다. 처음에는 `IncorrectTokenCountException: expected 17, actual 48` 에러가, 설정을 바꾸면 `expected 47, actual 17` 에러가 반복적으로 발생했습니다.
- **원인 분석:**
    1.  **인코딩 문제:** 공공데이터 CSV 파일이 `UTF-8`이 아닌 `EUC-KR`로 인코딩되어 있었습니다.
    2.  **데이터 불일치:** 실제 데이터 라인 끝에 불필요한 쉼표(Trailing Comma)가 존재하여, 명세서의 47개 컬럼이 아닌 **48개**의 토큰으로 분리되는 현상을 발견했습니다.
- **해결:**
    - `encoding("EUC-KR")`로 설정하고, DTO와 헤더 설정을 48개에 맞춰 모든 데이터를 Raw하게 읽는 데 성공했습니다.
    - 최종적으로, 한글 CSV 헤더와 영문 DTO 필드명(`recordNumber`, `managementNumber` 등) 간의 명확한 매핑을 위해 `BeanWrapperFieldSetSetMapper` 대신 `CustomFieldSetMapper`를 직접 구현하여, 어떤 상황에서도 안정적으로 데이터를 매핑하는 구조를 완성했습니다.

### 6-2. 데이터 무결성 예외 처리
파티셔닝 환경에서 여러 스레드가 동시에 DB에 쓰기를 시도하자 `DuplicateKeyException`이 발생했습니다.
- **원인 분석:** 원본 데이터에 중복된 `관리번호`가 존재하거나, 다른 파티션에 동일한 데이터가 나뉘어 들어간 경우, 여러 스레드가 동시에 같은 키를 삽입하려다 충돌이 발생했습니다.
- **해결:** Spring Batch의 강력한 예외 처리 기능인 **`faultTolerant().skip()`**을 활용했습니다. `DuplicateKeyException` 발생 시 해당 레코드는 건너뛰고, 로그를 남기며, 배치는 중단 없이 계속 진행되도록 설계하여 안정성을 확보했습니다. 이는 단순히 에러를 회피하는 것이 아니라, '데이터의 현실을 수용하고 비즈니스 로직을 계속 수행한다'는 배치 처리의 중요한 원칙을 적용한 것입니다.

### 6-3. 테스트 구현
배치 환경 설정 및 테스트 데이터 분리, Bean 이름 충돌 등 문제가 있었습니다.
- **원인 분석:** 테스트에서 운영코드 설정 그대로 사용, TaskExecutor 빈 충돌
- **해결:** @TestConfiguration 내에서 테스트용 CSV 파일을 바라보도록 변경, application-test.yml에 spring.main.allow-bean-definition-overriding: true 설정을 추가하여 테스트 시 Bean 재정의를 허용


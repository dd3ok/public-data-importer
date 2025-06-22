CREATE TABLE IF NOT EXISTS restaurant
(
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    management_number           VARCHAR(50)  NOT NULL,
    licensing_date              DATE,
    close_date                  DATE,
    location_phone_number       VARCHAR(20),
    location_area               DOUBLE,
    location_zip_code           VARCHAR(10),
    full_address                VARCHAR(500),
    road_name_address           VARCHAR(500),
    road_name_zip_code          VARCHAR(10),
    business_name               VARCHAR(255),
    last_modified_at            DATETIME,
    data_update_type            VARCHAR(1),
    data_updated_at             DATETIME,
    industry_type               VARCHAR(100),
    coordinate_x                DECIMAL(19, 9),
    coordinate_y                DECIMAL(19, 9),
    open_auth_code              VARCHAR(50),        -- 추가
    male_worker_count           INT,                -- 추가
    female_worker_count         INT,                -- 추가
    surrounding_area_type       VARCHAR(100),       -- 추가
    grade_type                  VARCHAR(50),        -- 추가
    water_facility_type         VARCHAR(50),        -- 추가
    building_ownership_type     VARCHAR(50),        -- 추가
    monthly_rent                BIGINT,             -- 추가
    multi_use_business_yn       VARCHAR(1),         -- 추가
    total_facility_size         DOUBLE,             -- 추가
    traditional_business_number VARCHAR(50),        -- 추가
    traditional_business_main_food VARCHAR(255),    -- 추가
    total_worker_count          INT,                -- 기존 유지
    created_at                  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at                  DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_restaurant_management_number UNIQUE (management_number)
);

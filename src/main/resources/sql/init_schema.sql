USE surveys;

DROP TABLE IF EXISTS company;
DROP TABLE IF EXISTS survey_result;
DROP TABLE IF EXISTS survey;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS member;

-- Company Table 생성
CREATE TABLE company (
                         id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

select * from company;

-- 카테고리 테이블: 설문 내 분류(예: "채용 공고 및 지원 경험")
CREATE TABLE category (
                          id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                          company_id BIGINT UNSIGNED NOT NULL,
                          name VARCHAR(255) NOT NULL,
                          sort_order INT NOT NULL DEFAULT 0,
                          created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                          updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

SELECT * from category;


CREATE TABLE survey (
                             id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                             company_id BIGINT UNSIGNED NOT NULL,
                             category_id BIGINT UNSIGNED NOT NULL,
                             type ENUM('radio', 'checkbox', 'rating', 'textarea') NOT NULL,
                             title VARCHAR(255) NOT NULL,
                             subtitle VARCHAR(255),
                             options JSON DEFAULT NULL COMMENT 'JSON Array 때문에 JSON타입으로 선언합니다.',
                             max INT DEFAULT NULL,
                             required BOOLEAN DEFAULT FALSE,
                             created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                             updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

ALTER TABLE survey MODIFY COLUMN type ENUM('RADIO', 'CHECKBOX', 'RATING', 'TEXTAREA') NOT NULL;
UPDATE survey SET type = UPPER(type) WHERE type IN ('radio', 'checkbox');

SELECT * from survey;

CREATE TABLE member (
                        id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        email VARCHAR(255) NOT NULL UNIQUE,
                        gender ENUM('MALE', 'FEMALE', 'OTHER') DEFAULT NULL,
                        age_range VARCHAR(20) DEFAULT NULL,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

SELECT * from member;

CREATE TABLE survey_result (
                               id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                               member_id BIGINT UNSIGNED NOT NULL,
                               survey_id BIGINT UNSIGNED NOT NULL,
                               answer_text TEXT,
                               created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                               updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

SELECT * from survey_result;


CREATE TABLE user_activity_log (
                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   member_id BIGINT,
                                   http_method VARCHAR(10),
                                   request_url VARCHAR(1000),
                                   request_body TEXT,
                                   response_code INT,
                                   response_body TEXT,
                                   execution_time_ms BIGINT,
                                   created_at DATETIME
);

SELECT * from user_activity_log;











-- Company 테이블 샘플 데이터
INSERT INTO company (name) VALUES
                               ('삼성전자'),
                               ('LG전자'),
                               ('SK하이닉스'),
                               ('현대자동차'),
                               ('네이버'),
                               ('카카오'),
                               ('롯데그룹'),
                               ('신한은행'),
                               ('KB국민은행'),
                               ('CJ그룹');

-- Category 테이블 샘플 데이터
INSERT INTO category (company_id, name, sort_order) VALUES
-- 삼성전자 카테고리
(1, '채용 공고 및 지원 경험', 1),
(1, '서류 전형 및 사전 평가', 2),
(1, '면접 과정', 3),
(1, '기업 이미지 및 문화', 4),
-- LG전자 카테고리
(2, '채용 정보 접근성', 1),
(2, '지원 절차 편의성', 2),
(2, '면접 경험', 3),
(2, '기업 가치관', 4),
-- SK하이닉스 카테고리
(3, '채용 공고 내용', 1),
(3, '서류 전형 피드백', 2),
(3, '인터뷰 프로세스', 3),
(3, '기업 문화', 4),
-- 현대자동차 카테고리
(4, '채용 절차 투명성', 1),
(4, '서류 지원 시스템', 2),
(4, '면접관 태도', 3),
(4, '직무 설명 명확성', 4),
-- 네이버 카테고리
(5, '채용 사이트 사용성', 1),
(5, '코딩테스트 경험', 2),
(5, '기술 면접', 3),
(5, '회사 복지제도', 4);

-- Survey 테이블 샘플 데이터
INSERT INTO survey (company_id, category_id, type, title, subtitle, max, required, options) VALUES
-- 삼성전자 설문
(1, 1, 'radio', '채용 공고는 명확하고 이해하기 쉬웠나요?', '채용 공고 내용의 명확성에 대한 평가입니다.', NULL, TRUE, '["매우 그렇다", "그렇다", "보통이다", "그렇지 않다", "전혀 그렇지 않다"]'),
(1, 1, 'checkbox', '어떤 채널을 통해 채용 정보를 접하셨나요?', '모든 해당 항목을 선택해주세요.', 3, FALSE, '["회사 홈페이지", "취업 포털(사람인, 잡코리아 등)", "링크드인", "페이스북", "인스타그램", "지인 추천", "기타"]'),
(1, 2, 'rating', '서류 전형 결과 통보는 적절한 시기에 이루어졌나요?', '1-5점 척도로 평가해주세요.', 5, TRUE, NULL),
(1, 2, 'textarea', '서류 전형 과정에서 개선되었으면 하는 점이 있다면 자유롭게 작성해주세요.', NULL, NULL, FALSE, NULL),
(1, 3, 'radio', '면접관의 태도는 어떠했나요?', '면접관의 친절도와 전문성에 대한 평가입니다.', NULL, TRUE, '["매우 좋았다", "좋았다", "보통이다", "좋지 않았다", "매우 좋지 않았다"]'),
(1, 4, 'checkbox', '삼성전자 하면 떠오르는 이미지는 무엇인가요?', '최대 3개까지 선택 가능합니다.', 3, TRUE, '["혁신적인", "글로벌한", "전통적인", "체계적인", "보수적인", "역동적인", "안정적인", "권위적인"]'),

-- LG전자 설문
(2, 5, 'radio', 'LG전자 채용 정보를 쉽게 찾을 수 있었나요?', '채용 정보 접근성에 대한 질문입니다.', NULL, TRUE, '["매우 쉬웠다", "쉬웠다", "보통이다", "어려웠다", "매우 어려웠다"]'),
(2, 6, 'checkbox', '지원 과정에서 불편했던 점은 무엇인가요?', '해당하는 항목을 모두 선택해주세요.', NULL, FALSE, '["지원서 작성 시간이 오래 걸림", "제출 후 수정 불가", "첨부파일 용량 제한", "시스템 오류 경험", "불필요한 개인정보 요구", "없음"]'),
(2, 7, 'rating', '면접 과정에서 직무에 관한 질문은 얼마나 적절했나요?', '1-5점 척도로 평가해주세요.', 5, TRUE, NULL),
(2, 8, 'textarea', 'LG전자의 기업 가치관 중 가장 공감되는 부분은 무엇인가요?', '자유롭게 작성해주세요.', NULL, FALSE, NULL);


-- member 데이터
INSERT INTO member (name, email, gender, age_range)
VALUES
    ('Alice Kim', 'alice.kim@example.com', 'FEMALE', '20-29'),
    ('Bob Lee', 'bob.lee@example.com', 'MALE', '30-39'),
    ('Charlie Park', 'charlie.park@example.com', 'OTHER', '20-29'),
    ('Diana Choi', 'diana.choi@example.com', 'FEMALE', '40-49'),
    ('Ethan Jung', 'ethan.jung@example.com', 'MALE', '30-39'),
    ('Fiona Han', 'fiona.han@example.com', 'FEMALE', '10-19'),
    ('George Yoo', 'george.yoo@example.com', 'MALE', '50-59'),
    ('Hannah Kwon', 'hannah.kwon@example.com', 'FEMALE', '20-29'),
    ('Ian Seo', 'ian.seo@example.com', 'MALE', '30-39'),
    ('Jisoo Lim', 'jisoo.lim@example.com', 'FEMALE', '20-29');

-- 회원 정보 (member)도 있다고 가정하고, member_id는 1~5번 사용

-- survey_result 다중 INSERT 예제

INSERT INTO survey_result (member_id, survey_id, answer_text) VALUES
(1, 1, '매우 그렇다'),
(2, 1, '그렇다'),
(3, 1, '보통이다'),

(1, 2, '회사 홈페이지, 링크드인'),
(2, 2, '취업 포털(사람인, 잡코리아 등)'),
(3, 2, '페이스북'),

(1, 3, '4'),
(2, 3, '5'),
(3, 3, '3'),
(4, 3, '4'),
(5, 3, '2'),

(1, 4, '서류 전형 과정에서 대기 시간이 너무 길었습니다.'),
(2, 4, '과정을 좀 더 명확히 안내해주었으면 좋겠습니다.'),

(1, 5, '매우 좋았다'),
(2, 5, '좋았다'),

(1, 6, '혁신적인, 글로벌한'),
(2, 6, '체계적인, 역동적인');





select
    (select name from company where id = survey.company_id) as company_name,
    (select name from category where id = survey.category_id) as category_name,
    (select sort_order from category where id = survey.category_id) as sort_order,
    survey.*
from survey where company_id = :companyId;

TRUNCATE TABLE SPRING_SESSION;
TRUNCATE TABLE SPRING_SESSION_ATTRIBUTES;



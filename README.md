# Survey API

Spring Boot 기반의 설문 관리 서비스입니다.  
회사별로 다양한 카테고리의 질문을 설정하고, 사용자가 설문에 응답하면 이를 저장하고 통계를 확인할 수 있습니다.

## 주요 기능

### ✅ 사용자 측
- **설문 참여**
  - 로그인 후 설문 응답 제출
  - 질문 유형별(객관식, 주관식 등) 응답 처리
  - 중복 응답 방지 로직 적용  
- **설문 결과 확인**
  - 본인 응답 확인 기능
  - 통계 정보(선택지별 응답 수 등) 제공

### ✅ 관리자 측
- **설문 통계 조회**
  - 전체 설문 응답 수, 선택지별 응답 통계 확인
- **회원 및 활동 관리**
  - 회원 정보 관리 (이름, 이메일 등)
  - 유저 활동 로그 기록 (요청 URL, 응답 코드, 처리 시간 등)
  
## 기술 스택
- Java 17
- Spring Boot 3.4.5
- Spring Data JPA
- Spring Security 
- MySQL
- Thymeleaf
- Lombok
- Gradle
- Swagger

## DB ERD
<img width="3364" height="2756" alt="image" src="https://github.com/user-attachments/assets/f50b210b-28ec-4ba8-a663-5676f5b5df4e" />

## Cache Server가 다운됐을때
1) Redis 존재(정상 동작 기준)

1차 API 요청 (Cold/MISS)
```json
{
    "cacheInfo": {
      "cache": "companySurveys",
      "companyId": 1,
      "outcome": "MISS",
      "methodExecuted": true,
      "durationMs": 435,
      "categoryCount": 4
  }
}
```

2차 API 요청 (Warm/HIT)
```json
{
  "cacheInfo": {
    "cache": "companySurveys",
    "companyId": 1,
    "outcome": "HIT",
    "methodExecuted": false,
    "durationMs": 4,
    "categoryCount": 4
  }
}
```
| 성능 비교 |
|---|
| 콜드 435ms → 웜 4ms (**약99.08% 감소, ~108.8× 빠름**) |

2) Redis 중지 상태
```json
{
    "code": "internal_server_error",
    "message": "Redis command timed out"
}
```
|장애 결과 (Redis OFF)|
|---|
| **상태:** 실패(500) · **이유:** 캐시 조회 단계 예외 전파(타임아웃/연결거부)|

3) Redis 중지 + fail-open 적용
```json
{
    "cacheInfo": {
        "cache": "companySurveys",
        "outcome": "FAIL_OPEN",
        "companyId": 1,
        "methodExecuted": true,
        "cacheErrors": 1,
        "durationMs": 438,
        "categoryCount": 4
    }
}
```






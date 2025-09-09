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

## 장애 대비 캐시 전략 실험
- 문제 : Redis 캐시 서버 장애 시 API가 500(타임아웃/연결거부)로 실패
- 목표 : Redis 장애 상황에서도 정상 응답(200) 유지


1) 기준(Baseline) - Redis ON

1차 API 요청 (Cold/MISS) -> DB 조회
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

2차 API 요청 (Warm/HIT) -> 캐시 조회
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

2) 장애 재현 - Redis OFF(Fail-open 미적용)
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
- 동작 방식 : Redis에 문제가 생기면 캐시를 쓰지 않고 바로 DB에서 데이터를 가져옴 -> 에러 대신 정상 응답으로 반환
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
|요약|
|---|
| API가 죽지 않고 **정상 동작**(200 응답 유지)|


4) Redis 중지 + 2-Level + fail-open 적용
- 동작 방식 : L1(로컬 캐시) → L2(Redis) 순으로 조회하다 Redis에 문제가 생기면 DB에서 데이터를 가져오고, 가져온 값을 L1에 저장해 반복 요청을 막음.

| 장점 | 단점 |
|------|------|
| • L1이 반복 요청을 흡수해 DB 폭주 방지 <br> • 평소엔 L1/L2를 활용해 응답 속도와 분산 효율 극대화 | • L1은 서버별 캐시라 데이터 최신성이 Redis보다 늦을 수 있음 <br> • TTL 관리나 무효화 전략 필요 |


## 결과 - 2-Level + fail-open 적용
- 이번 트래픽 패턴에서는 성능 차이가 두드러지지 않았지만, 2-Level은 장애 상황에서 반복 조회를 흡수해 응답을 안정적으로 유지하는 안전망 역할을 했다.
  따라서 조회가 많고 변경이 드문 API에는 2-Level을 선택적으로 적용하고, 최신성이 중요한 API는 Fail-open 단독으로 운영하는 것이 적절하다.






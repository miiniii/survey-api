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


## Redis 용량 실험
- 문제 : 회사 수 증가에 따라 캐시 키가 기하급수적으로 늘어나면, Redis 메모리 한도 초과 시 키 퇴출(eviction) 발생
- 목표<br>
1)Hash vs String 저장 구조의 메모리 효율 비교<br>
2)100MB 환경에서 안전하게 저장 가능한 키 수와 퇴출 임계치 확인<br>

### 저장 구조 (String vs Hash)
- **String(JSON)**: DTO 전체를 직렬화해 저장 → 단순/편리하지만 **부분 갱신/부분 조회에 불리**
- **Hash(Map)**: 필드 단위 저장(HSET/HGETALL) → **카운터/부분 갱신 유리**, Medis에서 **테이블 형태**로 확인 가능
<p align="center">
  <img src="https://github.com/user-attachments/assets/8d888265-4d93-4344-9ffe-3004234c8fa7" alt="String(JSON) 저장" width="40%">
  <img src="https://github.com/user-attachments/assets/2b2449d7-450b-44c1-a5a5-57f02c47b57a" alt="Hash(Map) 저장" width="55%">
</p>
<p align="center">
  <em>왼쪽: String(JSON) 저장 / 오른쪽: Hash(Map) 저장</em>
</p>

1) Hash 구조
   
| 단계 | 누적 키 수(DBSIZE) | used_memory_human | evicted_keys | 비고 |
|------|------------------:|------------------:|-------------:|------|
| ① 초기 | 10,000 | ~3.10MB | 0 | 초기 주입 |
| ② 정상 구간 | 300,000 | ~49.10MB | 0 | 안정 구간 |
| ③ 임계 직전 | ~540,000 | ~95MB | 0 | 80~95% 구간, 모니터링 필요 |
| ④ 한도 도달 | 623,566 | 100.00MB | 76,436 | **LRU 퇴출 시작** |

- 샘플 키 메모리(MEMORY USAGE "survey:company:meta:{12345}") ≈ 152B
- 100MB에서 이론상 ~690k 키 저장 가능
- 운영 안전선(80%): ~550k 키 전후 (실측과 일치)
- **결론**: 100MB 환경에서 약 55만 키까지 안정, 이후에는 오래 안 쓰인 키부터 LRU 정책으로 제거됨

<br>
   
2) String 구조
   
| 단계 | 누적 키 수(DBSIZE) | used_memory_human | evicted_keys | 비고 |
|------|------------------:|------------------:|-------------:|------|
| ① 초기 | 50,000 | 12.30MB | 0 | 초기 주입 |
| ② 정상 구간 | 300,000 | 70.73MB | 0 | 안정 구간 |
| ③ 임계 직전 | 410,000 | 95.09MB | 0 | 알람 대상 |
| ④ 한도 도달 | 431,580 | 100.00MB | 18,420 | **LRU 퇴출 시작** |

- 샘플 키 메모리(MEMORY USAGE "companySurveys:{1234}") ≈ 224B/키
- 실측상 95MB에서 41만 키 → 키당 ~237B 수준 (오버헤드 포함)
- 운영 안전선(80%): 330k~350k 키 권장
- **결론**: 100MB 환경에서 약 33~35만 키까지 안정, 이후엔 LRU로 퇴출 발생

<br>

### 운영 안전선 계산
- Redis 모니터링 값 : used_memory_human(95MB), DBSIZE(410,000 = 키 개수)
- 단위 변환
```
95MB = 95 × 1,048,576 = 99,532,800 bytes
```
- 키당 평균 메모리
```
99,532,800 bytes ÷ 410,000 keys ≈ 242.8 bytes/키
```
  - 샘플 MEMORY USAGE 로 찍은 개별 키는 224B 정도지만,전체적으로 보면 오버헤드(포인터, 메타데이터 등)가 더해져서 평균은 약 237B 수준
    
- 총 메모리 = 100MB = 104,857,600 bytes
- 안전선(80%) = 100MB × 0.8 = 83,886,080 bytes
- 키당 메모리 = 237B (실측 평균)
```
83,886,080 ÷ 237 ≈ 354,806 keys
```
약 **33만~35만** 키까지는 안전 구간으로 볼 수 있음<br>

<br>

### 캐시 스케일링 전략
- 단일 노드에서 100MB 한도에 도달하면 LRU 퇴출이 발생했기 때문에, 이를 확장하기 위해 **애플리케이션 레벨 샤딩(3노드)** 을 구성해 성능을 검증

#### 왜 Redis Cluster가 아닌 애플리케이션 레벨 샤딩인가?
- **실험 목적**: 장애 대응 시나리오에 초점을 맞춰, 복잡한 운영 자동화보다 **샤딩 동작 원리 자체를 직접 검증**하는 것이 목적
- **Redis Cluster 단점**: 슬롯 관리·재배치·운영 난이도가 높아, 단순 실험 환경에서는 과도한 오버헤드.  
- **애플리케이션 샤딩 장점**: 코드 단에서 샤드 노드를 직접 지정해 **구현·관찰이 단순**  

#### 실험 결과
500,000키 시점
| Shard(Port) | Keys(DB1) | used_memory_human | evicted_keys |
|------|------------------:|------------------:|-------------:|
| 6379 | 184,638 |17.29MB | 0 |
| 6380 | 165,546| 15.76MB | 0 |
| 6381 | 149,818 | 14.56MB| 0 |
| 합계 | ≈500,002 | ≈47.61MB| 0 |
- 분포 비중: 6379 36.93%, 6380 33.11%, 6381 29.96%
- 단일 100MB: 첫 LRU 431,580키
- 샤딩 100MB×3: 500,000키까지도 evicted=0
- 분포 기반 추정: 첫 LRU ≈ 1,168,724키 → 수용량 약 3배

<br>

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


### 결과 - 2-Level + fail-open 적용
- 이번 트래픽 패턴에서는 성능 차이가 두드러지지 않았지만, 2-Level은 장애 상황에서 반복 조회를 흡수해 응답을 안정적으로 유지하는 안전망 역할을 했다.
  따라서 조회가 많고 변경이 드문 API에는 2-Level을 선택적으로 적용하고, 최신성이 중요한 API는 Fail-open 단독으로 운영하는 것이 적절하다.

































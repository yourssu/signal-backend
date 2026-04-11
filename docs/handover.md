# 인수인계 가이드

> 작성일: 2026-04-12  
> 대상: 신규 개발자 / 운영 담당자

---

## 서비스 개요

SIGNAL은 대학생 소개팅 매칭 플랫폼입니다.  
유저는 프로필을 등록하고, 티켓을 충전한 뒤 마음에 드는 상대의 연락처를 열람할 수 있습니다.

```
[회원가입/로그인] → [프로필 등록] → [티켓 충전] → [랜덤 프로필 열람] → [연락처 열람]
```

---

## 레포지토리 구조

```
signal-backend/
├── app/        # Spring Boot 백엔드 (Kotlin, JDK 21)
├── observer/   # Python 옵저버 (로그 감시 → Slack 알림)
└── docs/       # 프로젝트 문서
```

배포는 하나의 Docker 이미지에 Spring Boot + Python Observer가 함께 실행됩니다.  
(`app/Dockerfile` 참고 — 컨테이너 시작 시 `observer.py`와 `app.jar`를 동시에 실행)

---

## 핵심 도메인

### 1. 인증 (Auth)

- 최초 앱 진입 시 `/api/auth/register`를 호출해 UUID 기반의 **비회원 토큰**을 발급받습니다.
- Google OAuth 연동 후 실명 계정과 연결합니다 (`/api/auth/google`).
- JWT Access Token (30분) / Refresh Token (30일) 구조입니다.
- Refresh는 `/api/auth/refresh`로 처리합니다.

#### ⚠️ 알려진 버그: 비회원 정보 유실

**현상**: 프론트엔드에서 토큰이 만료되었을 때 기존 UUID를 버리고 `/register`를 재호출하면, 이전에 연결된 Google 계정 및 프로필 정보가 날아갑니다.

**원인**: 비회원 UUID ↔ Google 계정 연결은 `google_user` 테이블에 UUID 기준으로 저장됩니다.  
토큰이 만료되면 프론트가 새 UUID를 발급받아 기존 UUID와의 연결 고리가 끊깁니다.

**해결 방향**:  
- 프론트에서 액세스 토큰 만료 시 반드시 Refresh Token으로 갱신하고, Refresh Token도 만료된 경우에만 재등록하도록 처리해야 합니다.
- 혹은 서버 측에서 Google 계정으로 재연결 시 기존 UUID를 찾아 병합하는 로직을 추가해야 합니다.
- **현재 미해결 상태이며, 신규 개발 시 우선 처리 권장합니다.**

---

### 2. 프로필 (Profile)

- 유저는 학과, 닉네임, 연락처(Instagram 등), 자기소개 문장을 등록합니다.
- 연락처는 `DataCipher`로 **AES 암호화**하여 저장하며, 조회 시 복호화합니다.
- 프로필 매칭은 성별 기반 필터링 + 우선순위 가중 랜덤 선택(`GaussianDistributionUtils`)으로 이루어집니다.
- 동일 연락처는 `CONTACT_LIMIT` 개 초과 시 등록 거부, `CONTACT_LIMIT_WARNING` 이상이면 Slack 경고를 보냅니다.

---

### 3. 티켓 (Viewer / Ticket)

유저는 **Viewer**라는 도메인으로 관리됩니다.  
Viewer가 티켓을 소비하면 프로필 연락처를 열람할 수 있습니다 (`TICKET_COST`개 소비).

---

### 4. 티켓 충전 — 문자 송금 시스템

> 이 시스템이 가장 복잡한 부분입니다. 자세히 파악이 필요합니다.

#### 전체 흐름

```
[유저] 인증번호 발급 (/api/viewers/verification)
    └→ 서버가 4자리 인증번호 생성 및 저장

[유저] 계좌로 송금 (받는 분 통장 표시 = 인증번호 4자리)

[운영자 폰] 입금 문자 수신 → 문자 내용을 API로 전송
    └→ POST /api/viewers/sms  또는
       POST /api/viewers/sms/{type}/{secretKey}

[서버] 문자 파싱 → 인증번호 확인 → 입금금액으로 티켓 계산 → 자동 발급

[Slack] 티켓 발급 완료 알림
```

#### 상세 설명

**① 인증번호 발급**

- 유저가 인증번호를 요청하면 서버는 `VerificationCodePool`에서 0~9999 사이의 숫자를 셔플된 큐에서 꺼내 발급합니다.
- 같은 UUID에 재요청하면 기존 코드를 반환합니다.
- 코드 풀이 고갈되면 DB에서 전체 코드를 초기화하고 재생성합니다.
- 레퍼럴 코드가 있으면 함께 등록합니다.

**② 입금 문자 파싱**

현재 두 가지 은행 문자 형식을 지원합니다:

| 파서 | `type` 값 | 설명 |
|------|-----------|------|
| `KakaoBankSMSParser` | `kakao_sms` | 카카오뱅크 입금 문자 |
| `KbBankSMSParser` | `kb_sms` | KB국민은행 입금 문자 |

각 파서는 문자에서 **입금 금액**과 **받는 분 통장 표시(이름)** 를 추출합니다.

**③ 인증번호 매칭 & 티켓 계산**

추출된 이름이 숫자(인증번호)이면 DB에서 해당 인증번호를 가진 유저를 찾아 티켓을 발급합니다.

티켓 수량은 입금 금액 → `TicketPricePolicy`가 계산합니다:
- 프로필 등록 후 **첫 구매**인 경우 `TICKET_PRICE_REGISTERED_POLICY` 적용 (할인)
- 그 외에는 `TICKET_PRICE_POLICY` 적용

정책 형식: `이름@가격n수량.이름@가격n수량` (예: `single@1000n1.small@3000n4.best@5000n8`)

**④ 인증번호 불일치 시 (이름이 숫자가 아닌 경우)**

받는 분 통장 표시가 이름인 경우 (예: "홍길동"), 서버는:
1. `DepositManager.smsRecord` (인메모리 `ConcurrentHashMap`)에 이름 → SMSMessage로 임시 저장합니다.
2. Slack `#signal-admin` 채널에 알림을 보냅니다.
3. 운영자가 Slack에서 `/t {인증번호} <개수>` 명령으로 수동 발급하거나, 유저가 직접 `/api/viewers/deposit`으로 인증번호를 입력해 재시도합니다.

> **주의**: `smsRecord`는 **인메모리**입니다. 서버 재시작 시 미처리 입금 기록이 사라집니다.  
> 운영 중 서버를 재시작할 때는 미처리 입금이 없는지 Slack 알림을 반드시 확인하세요.

**⑤ 수동 발급 API**

| 엔드포인트 | 설명 |
|-----------|------|
| `POST /api/viewers` | 운영자가 인증번호 + 수량 직접 지정하여 발급 |
| `POST /api/viewers/deposit` | 유저가 이름 + 인증번호로 재시도 |
| `POST /api/viewers/sms` | 은행 문자 본문을 body로 전송하여 자동 발급 |
| `POST /api/viewers/sms/{type}/{secretKey}` | path variable 방식 (동일 기능) |

---

### 5. 레퍼럴 (Referral)

- 유저는 자신의 레퍼럴 코드를 생성할 수 있습니다.
- 신규 유저가 인증번호 발급 시 레퍼럴 코드를 함께 전달하면 연결됩니다.
- 레퍼럴 유저가 티켓을 구매하면 추천인에게 보너스 티켓이 지급됩니다.

---

## 현재 미사용 / 교체 필요 기능

### ❌ KakaoPay 결제 — 미사용

`PaymentController`, `PaymentService`, `KakaoPayAdapter` 등 KakaoPay 결제 연동 코드가 구현되어 있으나 **현재 사용하지 않습니다**.  
실제 결제는 계좌 이체(문자 송금 시스템)로만 운영 중입니다.

- 관련 파일: `app/src/main/kotlin/.../payment/`
- 관련 환경변수: `KAKAOPAY_*` (설정은 유지하되 실제로 호출되지 않음)
- 향후 카드결제 도입 시 이 코드를 활성화하거나 교체해야 합니다.

---

### ⚠️ OpenAI 모델 어댑터 — 교체 필요

닉네임 자동 생성 기능(`/api/profiles/nickname`)에서 OpenAI API를 사용합니다.

**현재 문제**:

`OpenAIModel`이 OpenAI의 구 Responses API(`/v1/responses`) 형식으로 요청을 보내고 있으며,  
응답 파싱 로직이 해당 포맷에 하드코딩되어 있습니다.

```kotlin
// OpenAIModel.kt — 현재 파싱 로직
val outputArray = root["output"]!!.jsonArray
val firstOutput = outputArray[0].jsonObject
val contentArray = firstOutput["content"]!!.jsonArray
```

`observer/script/openai_client.py`도 동일한 Responses API 형식을 사용합니다.

**권장 조치**:  
- OpenAI 공식 Kotlin/Java SDK(`com.openai:openai-java`)로 교체하거나  
- Chat Completions API(`/v1/chat/completions`) 방식으로 어댑터를 재작성해야 합니다.
- `LocalChatModel`은 로컬/테스트 환경에서 자기소개 앞글자를 잘라 닉네임을 생성하므로 테스트에는 영향 없습니다.

---

## Python Observer

Spring Boot 서버의 로그 파일(`logs/*.log`)을 실시간으로 감시하여 특정 이벤트가 발생하면 Slack으로 알림을 전송합니다.

### 감시 이벤트 목록

| 이벤트 | Slack 채널 | 설명 |
|--------|-----------|------|
| 서버 재시작 | `SLACK_CHANNEL` | Tomcat 시작 감지 |
| 내부 서버 에러 | `SLACK_LOG_CHANNEL` | 500 에러 발생 시 |
| 프로필 등록 | `SLACK_ADMIN_CHANNEL` | 신규 프로필 등록 완료 |
| 티켓 자동 발급 완료 | `SLACK_CHANNEL` | 입금 문자 처리 성공 |
| 티켓 수동 발급 대기 | `SLACK_CHANNEL` | 인증번호 불일치 — 운영자 확인 필요 |
| 연락처 중복 경고 | `SLACK_LOG_CHANNEL` | 동일 연락처 임계값 초과 |
| 입금 금액 불일치 | `SLACK_CHANNEL` | 정책에 없는 금액 입금 |
| 결제 확인 요청 | `SLACK_CHANNEL` | `/api/viewers/deposit` 재시도 |

### Observer 실행 파일

| 파일 | 설명 |
|------|------|
| `observer/observer.py` | 구버전 단일 파일 (레거시) |
| `observer/script/observer.py` | 리팩토링된 최신 버전 (클래스 구조) |
| `observer/observer_run.sh` | 운영 환경 실행 스크립트 |

현재 Docker 이미지는 `observer/script/observer.py`를 실행합니다. (`Dockerfile` 참고)

---

## 운영 유의사항

### 티켓 수동 발급 시

1. Slack `#signal-admin`에서 인증번호 불일치 알림 확인
2. 해당 유저에게 연락하여 인증번호 확인
3. `POST /api/viewers` 또는 Slack `/t {인증번호} <개수>`로 수동 발급

### 서버 재시작 시

- `DepositManager.smsRecord` (인메모리)가 초기화됩니다.
- 재시작 전 Slack에서 미처리 입금 알림이 없는지 반드시 확인하세요.
- 재시작 후 Slack `#signal-channel`에 "SERVER RESTARTED" 메시지가 오면 정상입니다.

### 은행 추가 시

`SMSParser` 인터페이스를 구현하는 새 파서 클래스를 작성하고,  
`SMSParser.parsers` 리스트에 등록하면 됩니다.

```kotlin
// SMSParser.kt
private val parsers: List<SMSParser> = listOf(
    KakaoBankSMSParser,
    KbBankSMSParser,
    NewBankSMSParser  // 추가
)
```

---

## 개발 환경 빠른 시작

```bash
# 1. 환경변수 설정
cp app/.env.example app/.env
# app/.env 파일에서 필요한 값 채우기

# 2. Spring Boot 실행 (로컬은 H2 + LocalChatModel 자동 활성화)
cd app && ./gradlew bootRun

# 3. Observer 실행 (선택)
cp observer/.env.example observer/.env
pip install -r observer/script/requirements.txt
cd observer && bash observer_run.sh
```

로컬에서는 `local` 프로파일이 자동 적용됩니다.  
Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## 관련 문서

- [환경변수 및 인프라 설정](./environment-variables.md)

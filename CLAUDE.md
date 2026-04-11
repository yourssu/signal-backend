# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SIGNAL은 Kotlin/Spring Boot 기반의 소개팅 매칭 서비스 플랫폼입니다.
이 레포지토리는 모노레포 구조로, Java 백엔드 서버(`app/`)와 Python 옵저버 스크립트(`observer/`)로 구성됩니다.

```
signal-backend/
├── app/        # Java/Spring Boot 백엔드 서버
└── observer/   # Python 옵저버 및 분석 스크립트
```

---

## app/ — Spring Boot 백엔드

### 개발 명령어

```bash
cd app
./gradlew build          # 빌드
./gradlew bootRun        # 애플리케이션 실행
./gradlew clean          # 빌드 결과물 삭제
./gradlew test           # 전체 테스트 실행
./gradlew check          # 테스트 포함 전체 검사
./gradlew test --tests "ClassName"             # 특정 테스트 클래스 실행
./gradlew test --tests "ClassName.methodName"  # 특정 테스트 메서드 실행
```

### 아키텍처 (DDD 구조)

```
app/src/main/kotlin/com/yourssu/signal/
├── api/                    # Controllers 및 API DTOs
├── config/                 # Security, Properties 등 설정 클래스
├── domain/                 # 도메인 모듈 (비즈니스 로직)
│   ├── auth/              # 인증 및 유저 관리
│   ├── blacklist/         # 유저 차단 기능
│   ├── order/             # 주문 관리
│   ├── payment/           # 결제 처리 (KakaoPay 연동)
│   ├── profile/           # 프로필 및 매칭
│   └── viewer/            # 유저 데이터 접근
├── infrastructure/        # 외부 연동
└── utils/                 # 유틸리티 클래스
```

각 도메인 모듈은 일관된 구조를 따릅니다:
- `business/` — 애플리케이션 서비스 및 비즈니스 로직
- `implement/` — 도메인 엔티티, 레포지토리, 비즈니스 규칙
- `storage/` — JPA 엔티티 및 레포지토리 구현

### 주요 기술 스택

- **Spring Boot 3.4.1** + Kotlin 1.9.25
- **JPA/Hibernate** + QueryDSL
- **Spring Security** + JWT 인증
- **KakaoPay API** 결제 연동
- **H2/MySQL** 데이터베이스
- **Kotest** 테스트 프레임워크
- **OpenAPI/Swagger** API 문서화

### 핵심 비즈니스 도메인

#### 프로필 관리
- 유저 프로필 생성 (연락처 등 민감 정보는 `DataCipher`로 암호화)
- 성별/선호도 기반 프로필 매칭
- 우선순위 기반 랜덤 프로필 선택

#### 티켓 시스템
- 티켓 소비로 상대방 연락처 열람
- 주문 이력 관리
- KakaoPay 결제 연동

#### 인증
- Google OAuth 연동
- JWT 세션 관리
- UUID 기반 유저 식별 (Argument Resolver)

### 주요 패턴 및 컨벤션

- 의존성 주입: 생성자 주입 선호, `@ConfigurationProperties` 활용
- 데이터 암호화: 연락처는 저장 전 암호화, 조회 후 복호화 (`DataCipher`)
- 예외 처리: 도메인별 커스텀 예외 (`ProfileNotFoundException` 등), 글로벌 핸들러
- 테스트: Kotest 사용, 클래스명은 `ClassNameTest.kt` 패턴, H2 인메모리 DB

### 주요 파일

- `app/src/.../config/WebSecurityConfig.kt` — 보안 설정
- `app/src/.../utils/DataCipher.kt` — 암호화/복호화 유틸
- `app/src/.../domain/profile/implement/Profile.kt` — 프로필 도메인 모델
- `app/src/.../domain/auth/implement/User.kt` — 유저 인증 모델

---

## observer/ — Python 옵저버

Signal 서버와 연동하여 매칭 이벤트를 감지하고 분석하는 Python 스크립트 모음입니다.

```
observer/
├── observer.py          # 메인 옵저버 실행 파일
├── observer_run.sh      # 옵저버 실행 셸 스크립트
├── analysis.py          # 데이터 분석
├── analysis_date.py     # 날짜 기반 분석
├── run.sh               # 실행 스크립트
├── logs/                # 로그 디렉토리
└── script/              # 보조 스크립트
    ├── admin.py
    ├── docker-deploy.sh
    ├── log_handlers.py
    ├── observer.py
    ├── openai_client.py
    ├── requirements.txt
    ├── signal_handler.py
    ├── signal_server_client.py
    └── test_openai_simple.sh
```

### 실행

```bash
cd observer
bash observer_run.sh     # 옵저버 실행
bash run.sh              # 일반 실행
pip install -r script/requirements.txt  # 의존성 설치
```

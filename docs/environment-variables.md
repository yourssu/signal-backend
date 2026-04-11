# 환경변수 및 시크릿 정리

> 이 문서는 SIGNAL 서비스 전체 구성에 필요한 환경변수와 시크릿을 정리합니다.  
> 실제 값은 팀 내부 채널(Notion, 1Password 등)에서 관리합니다.

---

## 분류 기준

| 구분 | 설명 | GitHub 저장 위치 |
|------|------|-----------------|
| `secret` | 외부에 노출 시 보안 위협이 있는 값 | GitHub Secrets |
| `var` | 환경별로 다르지만 노출되어도 무방한 값 | GitHub Variables |

---

## GitHub Actions 환경

워크플로우는 `dev` / `prod` 두 개의 **environment**를 사용합니다.  
각 environment에 아래 Secrets / Variables를 별도로 설정해야 합니다.

### AWS / 인프라

| 이름 | 구분 | 예시 | 설명 |
|------|------|------|------|
| `AWS_ROLE_ARN` | var | `arn:aws:iam::123456789:role/GitHubActionsRole` | OIDC로 Assume할 IAM Role ARN |
| `ECR_PUBLIC_REGISTRY_ID` | var | `a1b2c3d4` | ECR Public 레지스트리 alias |
| `PROJECT_NAME` | var | `signal` | 컨테이너 이름 및 ECR 레포지토리명 기준 |
| `HOST_URL` | var | `1.2.3.4` | 배포 대상 EC2 퍼블릭 IP 또는 도메인 |
| `YOURSSU_PEM` | secret | `-----BEGIN RSA...` | EC2 접속용 SSH 개인키 (dev) |
| `HOME_PEM` | secret | `-----BEGIN RSA...` | EC2 접속용 SSH 개인키 (prod) |
| `HOME_URL` | var | `api.signal.yourssu.com` | prod EC2 호스트 (prod 전용) |

> **AWS OIDC 설정 방법**: GitHub Actions에서 AWS 자격증명을 안전하게 사용하기 위해 OIDC를 사용합니다.  
> IAM Identity Provider에 `token.actions.githubusercontent.com`을 등록하고, 해당 Role에 ECR Push + EC2 접속 권한을 부여하세요.

---

### 서버 (app/)

#### 데이터베이스

| 이름 | 구분 | 예시 | 설명 |
|------|------|------|------|
| `DB_URL` | secret | `jdbc:mysql://host:3306/signal` | MySQL JDBC URL |
| `DB_USERNAME` | secret | `signal_user` | DB 계정명 |
| `DB_PASSWORD` | secret | `...` | DB 비밀번호 |

#### JWT

| 이름 | 구분 | 예시 | 설명 |
|------|------|------|------|
| `JWT_SECRET` | secret | 256bit 이상 랜덤 문자열 | JWT 서명 키 |
| `JWT_ACCESS_TOKEN_EXPIRATION` | var | `1800000` | 액세스 토큰 만료 (ms), 기본 30분 |
| `JWT_REFRESH_TOKEN_EXPIRATION` | var | `2592000000` | 리프레시 토큰 만료 (ms), 기본 30일 |

#### 서버 설정

| 이름 | 구분 | 예시 | 설명 |
|------|------|------|------|
| `SERVER_PORT` | var | `8080` | Spring Boot 서버 포트 |
| `CORS_ALLOWED_ORIGIN` | var | `https://signal.yourssu.com` | CORS 허용 Origin |
| `ENVIRONMENT` | var | `dev` / `prod` | Spring 프로파일 (`dev` or `prod`) |
| `SPRINGDOC_API_DOCS_PATH` | var | `/api-docs` | Swagger API docs 경로 (prod에서는 비활성) |
| `SPRINGDOC_SWAGGER_UI_PATH` | var | `/swagger-ui.html` | Swagger UI 경로 |

#### 어드민 / 도메인 보안

| 이름 | 구분 | 예시 | 설명 |
|------|------|------|------|
| `ADMIN_ACCESS_KEY` | secret | 랜덤 문자열 | 어드민 API 접근 키 |
| `CONTACT_SECRET_KEY` | secret | 랜덤 문자열 | 연락처 암호화 키 (`DataCipher` 사용) |

#### 티켓 정책

| 이름 | 구분 | 예시 | 설명 |
|------|------|------|------|
| `WHITELIST` | var | `false` | 화이트리스트 모드 활성화 여부 |
| `CONTACT_LIMIT` | var | `4` | 동일 연락처 등록 제한 수 |
| `CONTACT_LIMIT_WARNING` | var | `2` | 동일 연락처 경고 기준 수 |
| `TICKET_COST` | var | `1` | 연락처 열람 시 소비 티켓 수 |
| `TICKET_PRICE_POLICY` | var | `single@1000n1.small@3000n4.best@5000n8` | 티켓 가격 정책 (`이름@가격n수량` 형식) |
| `TICKET_PRICE_REGISTERED_POLICY` | var | `single@700n1.small@2100n4.best@3500n8` | 프로필 등록 첫 구매 할인 정책 |

#### KakaoPay

| 이름 | 구분 | 예시 | 설명 |
|------|------|------|------|
| `KAKAOPAY_ADMIN_KEY` | secret | `DEV4FF...` (dev) / `...` (prod) | KakaoPay Admin Key |
| `KAKAOPAY_CID` | var | `TC0ONETIME` (dev) / `...` (prod) | 가맹점 코드 |
| `KAKAOPAY_APPROVAL_URL` | var | `https://signal.yourssu.com/purchase/success` | 결제 성공 Redirect URL |
| `KAKAOPAY_CANCEL_URL` | var | `https://signal.yourssu.com/purchase/cancel` | 결제 취소 Redirect URL |
| `KAKAOPAY_FAIL_URL` | var | `https://signal.yourssu.com/purchase/fail` | 결제 실패 Redirect URL |

#### Google OAuth

| 이름 | 구분 | 예시 | 설명 |
|------|------|------|------|
| `GOOGLE_OAUTH_CLIENT_ID` | var | `...apps.googleusercontent.com` | Google OAuth Client ID |
| `GOOGLE_OAUTH_CLIENT_SECRET` | secret | `GOCSPX-...` | Google OAuth Client Secret |
| `GOOGLE_OAUTH_REDIRECT_URI` | var | `https://api.signal.yourssu.com/auth/callback` | OAuth Redirect URI |

#### OpenAI

| 이름 | 구분 | 예시 | 설명 |
|------|------|------|------|
| `OPENAI_API_KEY` | secret | `sk-...` | OpenAI API Key |
| `OPENAI_URL` | var | `https://api.openai.com/v1/responses` | OpenAI API Endpoint |
| `OPENAI_MODEL` | var | `gpt-4o-mini` | 사용할 모델명 |
| `OPENAI_PROMPT` | var | (프롬프트 전문) | 닉네임 생성용 시스템 프롬프트 |
| `OPENAI_POLICY_PROMPT` | var | (프롬프트 전문) | 프로필 정책 위반 감지용 프롬프트 |
| `USER_INPUT_LIMIT` | var | `100` | 유저 입력 최대 토큰 수 |

---

### 옵저버 (observer/)

#### Slack

| 이름 | 구분 | 예시 | 설명 |
|------|------|------|------|
| `SLACK_TOKEN` | secret | `xoxb-...` | Slack Bot Token |
| `SLACK_CHANNEL` | var | `#signal-events` | 일반 알림 채널 ID 또는 이름 |
| `SLACK_ADMIN_CHANNEL` | var | `#signal-admin` | 어드민 알림 채널 |
| `SLACK_LOG_CHANNEL` | var | `#signal-logs` | 로그/에러 알림 채널 |

#### 입금 계좌 (티켓 수동 발급)

| 이름 | 구분 | 예시 | 설명 |
|------|------|------|------|
| `BANK_NAME` | var | `카카오뱅크` | 입금 받는 은행명 |
| `BANK_ACCOUNT` | secret | `1234567890` | 입금 계좌번호 |

#### 서버 연결

| 이름 | 구분 | 예시 | 설명 |
|------|------|------|------|
| `SERVER_PORT` | var | `9012` | Signal API 서버 포트 (observer → app 통신) |
| `ADMIN_ACCESS_KEY` | secret | (app과 동일) | 어드민 API 접근 키 |

---

## 로컬 개발 환경 설정

### app/ (Spring Boot)

```bash
# 1. 예시 파일 복사
cp app/.env.example app/.env

# 2. app/.env 파일을 열어 값 채우기
#    (로컬은 H2 in-memory DB 사용, DB_URL 불필요)

# 3. 실행
cd app && ./gradlew bootRun
```

`app/.env` 파일은 `.gitignore`로 추적되지 않습니다.  
`application-local.yml`은 `optional:file:.env[.properties]`로 `.env`를 자동 로드합니다.

### observer/ (Python)

```bash
# 1. 예시 파일 복사
cp observer/.env.example observer/.env

# 2. observer/.env 파일을 열어 값 채우기

# 3. 의존성 설치
pip install -r observer/script/requirements.txt

# 4. 실행
cd observer && bash observer_run.sh
```

---

## 인프라 구성 요약

```
GitHub Actions
    │
    ├── AWS OIDC (IAM Role) ──→ Amazon ECR Public (이미지 Push)
    │
    └── SSH (PEM key) ────────→ EC2 (Ubuntu)
                                   │
                                   ├── docker-deploy.sh 실행
                                   │
                                   └── Docker Container
                                          ├── Spring Boot (app.jar)
                                          │     └── :SERVER_PORT
                                          └── observer.py (백그라운드)
                                                └── logs/ 감시 → Slack 알림
```

### EC2 서버 디렉토리 구조

```
/home/ubuntu/{PROJECT_NAME}-api/
├── .env                  # 배포 시 CI/CD가 생성
├── docker-deploy.sh      # 배포 스크립트
└── logs/                 # 컨테이너와 volume mount
```

### Docker 이미지 경로

```
public.ecr.aws/{ECR_PUBLIC_REGISTRY_ID}/yourssu/{PROJECT_NAME}:{IMAGE_TAG}
```

---

## GitHub Repository 설정 체크리스트

### Environments

- [ ] `dev` environment 생성
- [ ] `prod` environment 생성 (배포 보호 규칙 설정 권장)

### dev environment

**Secrets**
- [ ] `YOURSSU_PEM`
- [ ] `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- [ ] `JWT_SECRET`
- [ ] `ADMIN_ACCESS_KEY`, `CONTACT_SECRET_KEY`
- [ ] `OPENAI_API_KEY`
- [ ] `KAKAOPAY_ADMIN_KEY`
- [ ] `GOOGLE_OAUTH_CLIENT_SECRET`
- [ ] `SLACK_TOKEN`

**Variables**
- [ ] `AWS_ROLE_ARN`
- [ ] `ECR_PUBLIC_REGISTRY_ID`
- [ ] `PROJECT_NAME`
- [ ] `HOST_URL`
- [ ] `SERVER_PORT`
- [ ] `CORS_ALLOWED_ORIGIN`
- [ ] `ENVIRONMENT` = `dev`
- [ ] `GOOGLE_OAUTH_CLIENT_ID`, `GOOGLE_OAUTH_REDIRECT_URI`
- [ ] `KAKAOPAY_CID`, `KAKAOPAY_APPROVAL_URL`, `KAKAOPAY_CANCEL_URL`, `KAKAOPAY_FAIL_URL`
- [ ] `OPENAI_URL`, `OPENAI_MODEL`, `OPENAI_PROMPT`, `OPENAI_POLICY_PROMPT`, `USER_INPUT_LIMIT`
- [ ] `JWT_ACCESS_TOKEN_EXPIRATION`, `JWT_REFRESH_TOKEN_EXPIRATION`
- [ ] `WHITELIST`, `CONTACT_LIMIT`, `CONTACT_LIMIT_WARNING`, `TICKET_COST`
- [ ] `TICKET_PRICE_POLICY`, `TICKET_PRICE_REGISTERED_POLICY`
- [ ] `SLACK_CHANNEL`, `SLACK_ADMIN_CHANNEL`, `SLACK_LOG_CHANNEL`

### prod environment

dev와 동일한 항목 + 아래 추가:

**Secrets**
- [ ] `HOME_PEM` (prod용 EC2 키)

**Variables**
- [ ] `HOME_URL` (prod EC2 호스트)
- [ ] `ENVIRONMENT` = `prod`

# Yourssu-Signal-Backend

## 체크리스트

### 프로필 API(api/profiles)
- [x] 프로필 생성
- [x] 프로필 조회 - UUID
  - [x] 구매 프로필 아이디, 구매 시간 정보 포함
- [x] 전체 프로필 조회
- [x] 전체 프로필 개수 조회
- [x] 랜덤 프로필 조회
  - [x] Viewer 성별은 제외함
- [x] 티켓 사용하여 연락처 조회
  - [x] 이미 구매한 연락처이면 티켓을 소모하지 않음
  
### 열람 사용자 API(api/viewers)
- [x] Uuid 일대일 대응 Verification Code 생성
- [x] 티켓 발급(관리자 기능)
- [x] viewer 조회 - UUID
- [x] 전체 viewer 조회(관리자 기능) 

### 닉네임 추천 API(api/profiles/nickname)
- [x] 닉네임 추천 기능 - 실행 환경 별로 분리
  - [x] local 환경 LocalChatModel 사용
  - [x] prod 환경 실제 AI 모델 사용

## API Documentation
- [프로필 API](src/main/resources/http/docs/profile.http)
- [열람 사용자 API](src/main/resources/http/docs/viewer.http)

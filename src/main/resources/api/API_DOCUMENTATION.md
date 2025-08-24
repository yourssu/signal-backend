# API Documentation

## Authentication
- 🔐 Bearer Token authentication is required.

## Auth

[회원 가입 (POST /api/auth/register)](auth/register.md)  
[토큰 갱신 (POST /api/auth/refresh)](auth/refresh.md)

## Profile

[전체 프로필 개수 조회 (GET /api/profiles/count)](profile/count_profile.md)  
[성별 프로필 개수 조회 (GET /api/profiles/count?gender={gender})](profile/count_profile_by_gender.md)
[🔐 닉네임 추천 (GET /api/profiles/nickname/suggestion)](profile/suggest_nickname.md)  
[🔐 프로필 생성 (POST /api/profiles)](profile/create_profile.md)  
[🔐 나의 프로필 조회 (GET /api/profiles/uuid)](profile/get_profile_uuid.md)   
[🔐 구매한 프로필 아이디 조회 (GET /api/profiles/{profileId})](profile/get_profile_id.md)  
[🔐 랜덤 프로필 조회 (GET /api/profiles/random)](profile/get_random_profile.md)  
[🔐 연락처 구매 (POST /api/profiles/contact)](profile/consume_ticket.md)

## Viewer

[🔐 인증번호 발급 (POST /api/viewers/verification)](viewer/issue_verification.md)
[🔐 은행 입금 확인 요청 (POST /api/viewers/deposit)](viewer/issue_ticket_by_deposit_name.md)
[🔐 뷰어 조회 (GET /api/viewers/uuid)](viewer/get_viewer_uuid.md)

## Payment

[🔐 결제 초기화 (POST /api/viewers/payment/kakaopay/initiate)](payment/initiate_payment.md)  
[🔐 결제 승인 (POST /api/viewers/payment/kakaopay/approve)](payment/approve_payment.md)

## Admin

- 🔑 Admin APIs require `secretKey` for authentication.

[🔑 티켓 발급 (POST /api/viewers/tickets)](viewer/issue_ticket.md)  
[🔑 은행 입금 문자 티켓 발급 (POST /api/viewers/tickets/sms)](viewer/issue_ticket_by_bank_deposit_sms.md)   
[🔑 블랙리스트 추가 (POST /api/profiles/blacklist)](profile/add_blacklist.md)  
[🔑 블랙리스트 삭제 (DELETE /api/profiles/blacklist/{profileId})](profile/delete_blacklist.md)

## For Dev

**Note: These endpoints are only available in development environments and will return 404 in production.**

[🔑 개발용 토큰 발급 (GET /api/auth/dev-token)](auth/dev-token.md)  
[🔑 전체 프로필 조회 (GET /api/profiles)](profile/get_all_profile_for_admin.md)  
[🔑 전체 뷰어 조회 (GET /api/viewers)](viewer/get_all_viewer_for_admin.md)

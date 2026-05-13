import pytz
from datetime import datetime
from openai_client import openai_client


class SignalHandler:
    # 공통 로그 접두사
    NOTIFICATION_PREFIX = 'INFO com.yourssu.signal.infrastructure.logging.Notification -'
    
    # 시그널 관련 로그 패턴 상수
    CREATE_PROFILE_PREFIX = f'{NOTIFICATION_PREFIX} CreateProfile'
    FAILED_PROFILE_CONTACT_PREFIX = f'{NOTIFICATION_PREFIX} FailedProfileContactExceedsLimit'
    CONTACT_EXCEEDS_WARNING_PREFIX = f'{NOTIFICATION_PREFIX} ContactExceedsLimitWarning'
    ISSUE_TICKET_PREFIX = f'{NOTIFICATION_PREFIX} Issued ticket'
    RETRY_ISSUE_TICKET_PREFIX = f'{NOTIFICATION_PREFIX} RetryIssuedTicket'
    CONSUME_TICKET_PREFIX = f'{NOTIFICATION_PREFIX} Consumed ticket'
    ISSUE_TICKET_BY_BANK_DEPOSIT_PREFIX = f'{NOTIFICATION_PREFIX} IssueTicketByBankDepositSms'
    FAILED_BY_BANK_DEPOSIT_PREFIX = f'{NOTIFICATION_PREFIX} IssueFailedTicketByDepositAmount'
    FAILED_BY_UNMATCHED_VERIFICATION_PREFIX = f'{NOTIFICATION_PREFIX} IssueFailedTicketByUnMatchedVerification'
    PAY_NOTIFICATION_PREFIX = f'{NOTIFICATION_PREFIX} PayNotification'
    NO_FIRST_PURCHASED_TICKET_PREFIX = f'{NOTIFICATION_PREFIX} NoFirstPurchasedTicket'
    
    def __init__(self, config, notifier):
        self.config = config
        self.notifier = notifier

        # 핸들러 매핑
        self.handlers = {
            self.CREATE_PROFILE_PREFIX: self.create_profile_message,
            self.ISSUE_TICKET_PREFIX: self.create_issued_ticket_message,
            self.RETRY_ISSUE_TICKET_PREFIX: self.create_retry_issued_ticket_message,
            # self.CONSUME_TICKET_PREFIX: self.create_consumed_ticket_message,
            self.FAILED_PROFILE_CONTACT_PREFIX: self.create_failed_profile_contact_message,
            self.CONTACT_EXCEEDS_WARNING_PREFIX: self.create_contact_exceeds_warning_message,
            self.ISSUE_TICKET_BY_BANK_DEPOSIT_PREFIX: self.create_issue_ticket_message,
            self.FAILED_BY_BANK_DEPOSIT_PREFIX: self.create_failed_issue_ticket_message_amount,
            self.FAILED_BY_UNMATCHED_VERIFICATION_PREFIX: self.create_failed_issue_ticket_message_verification,
            self.PAY_NOTIFICATION_PREFIX: self.create_pay_notification_message,
            self.NO_FIRST_PURCHASED_TICKET_PREFIX: self.create_no_first_purchased_ticket_message
        }

    def _get_kst_now(self):
        """한국 시간 반환"""
        kst = pytz.timezone('Asia/Seoul')
        return datetime.now(pytz.utc).astimezone(kst)

    def _append_or_create_file(self, filename, content):
        """파일에 내용 추가"""
        with open(filename, 'a', encoding='utf-8') as f:
            f.write(content)

    def create_profile_message(self, line):
        """프로필 등록 완료 메시지 및 정책 위반 검사"""
        id, department, contact, nickname, introSentences = line[line.find('&') + 1:].split('&')
        message = f"""🩷 *프로필 등록 완료* 🩷
    -  💖 *식별 번호*: {id}
    -  🏢 *학과*: {department}
    -  📞 *연락처*: https://www.instagram.com/{contact.replace('@', '')}
    -  👤 *닉네임*: {nickname}
    -  📝 *자기소개*: {introSentences}
    """

        self.notifier.send_admin_notification(message)
        
        try:
            profile_data = {
                'id': id,
                'department': department,
                'contact': contact,
                'nickname': nickname,
                'introSentences': introSentences
            }
            violation_result = openai_client.check_policy_violation(profile_data)
            
            if violation_result.get('violation', False):
                reason = violation_result.get('reason', 'Policy violation detected by AI')

                violation_message = f"""<!channel> 🚨 *정책 위반 프로필 감지* 🚨
    -  💖 *프로필 ID*: {id}
    -  🚨 *위반 사유*: {reason}
    """
                self.notifier.send_admin_notification(violation_message)
            else:
                reason = violation_result.get('reason', '')
                if reason and 'Policy check failed' in reason:
                    failure_message = f"""❌ *정책 검사 실패* ❌
    -  💖 *프로필 ID*: {id}
    -  🚨 *실패 사유*: {reason}
    -  ⚠️ *조치*: 수동 확인 필요
    """
                    self.notifier.send_admin_notification(failure_message)
                else:
                    pass_message = f"""✅ *정책 검사 통과* ✅
    -  💖 *프로필 ID*: {id}
    -  🎉 *결과*: 정책 위반 없음
    """
                    self.notifier.send_admin_notification(pass_message)
                
        except Exception as e:
            error_message = f"""❌ *정책 검사 시스템 오류* ❌
    -  💖 *프로필 ID*: {id}
    -  🚨 *오류 내용*: {str(e)}
    """
            self.notifier.send_admin_notification(error_message)

    def create_failed_profile_contact_message(self, line):
        """프로필 등록 실패 메시지"""
        contact_policy = line[line.find('&') + 1:].strip()
        message = f"""🚨🚨 같은 연락처 등록 실패 - {self.config.environment.upper()} SERVER 🚨🚨
    - ⚔️ 중복 연락처 제한 기준: {contact_policy} 개
    """
        self._append_or_create_file("/app/logs/createProfiles.txt", message)
        self.notifier.send_admin_notification(message)

    def create_contact_exceeds_warning_message(self, line):
        """연락처 중복 경고 메시지"""
        contact_policy = line[line.find('&') + 1:].strip()
        message = f"""🚨 같은 연락처 등록 경고 - {self.config.environment.upper()} SERVER 🚨
    - ⚔️ 중복 연락처 경고 기준: {contact_policy} 개
    """
        self._append_or_create_file("/app/logs/createProfiles.txt", message)
        self.notifier.send_admin_notification(message)

    def create_issued_ticket_message(self, line):
        """이용권 발급 완료 메시지"""
        verification, uuid, ticket, available_ticket = line[line.find('&') + 1:].split(' ')
        now_kst = self._get_kst_now()

        ticket_policy_message = f"- 💰 현재 가격 정책: {self.config.ticket_price_policy}"
        ticket_registered_message = f"- 🌱 프로필 등록 완료 첫 구매 고객: {self.config.ticket_price_registered_policy}"

        message = f"""🩷 *이용권 발급 완료* 🩷

    -  💖 *인증 번호*: {str(verification).zfill(4)}
    -  😀 *식별 번호*: {uuid}
    -  🎁 *발급한 이용권*: {int(ticket)}장
    -  💝 *보유 이용권*: {int(available_ticket)}장
    -  💌 *발급 시간*: {now_kst.strftime('%Y-%m-%d %H:%M:%S')} KST

    *이용권 발급 방법 안내*
    *자동 발급*
        - 🎁 계좌번호: KB국민은행 93770201426024
        - 💌 받는 분 통장 표시: {str(verification).zfill(4)}
        {ticket_policy_message}
        {ticket_registered_message}

    *수동 발급*
    `/t {str(verification).zfill(4)} <개수>`
    입금 확인 후 이용권을 발급해주세요!
    """
        self.notifier.send_notification(message)

    def create_retry_issued_ticket_message(self, line):
        """결제 확인 요청 이용권 발급 완료 메시지"""
        verification, uuid, ticket, available_ticket, name = line[line.find('&') + 1:].split(' ')
        now_kst = self._get_kst_now()

        ticket_policy_message = f"- 💰 현재 가격 정책: {self.config.ticket_price_policy}"
        ticket_registered_message = f"- 🌱 프로필 등록 완료 첫 구매 고객: {self.config.ticket_price_registered_policy}"

        message = f"""💌 *결제 확인 요청 이용권 발급 완료* 💌
    -  💌 *받는 분 통장 표시*: {name}
    -  💖 *인증 번호*: {str(verification).zfill(4)}
    -  😀 *식별 번호*: {uuid}
    -  🎁 *발급한 이용권*: {int(ticket)}장
    -  💝 *보유 이용권*: {int(available_ticket)}장
    -  💌 *발급 시간*: {now_kst.strftime('%Y-%m-%d %H:%M:%S')} KST

    *이용권 발급 방법 안내*
    *자동 발급*
        - 🎁 계좌번호: KB국민은행 93770201426024
        - 💌 받는 분 통장 표시: {str(verification).zfill(4)}
        {ticket_policy_message}
        {ticket_registered_message}

    *수동 발급*
    `/t {str(verification).zfill(4)} <개수>`
    입금 확인 후 이용권을 발급해주세요!
    """
        self.notifier.send_notification(message)

    def create_consumed_ticket_message(self, line):
        """이용권 소비 메시지"""
        nickname, ticket = line[line.find('&') + 1:].split(' ')
        if ticket == '0':
            return

        now_kst = self._get_kst_now()
        message = f"""🩷 *누군가 {nickname}님께 시그널을 보냈어요.* 🩷
    -  💌 *보낸 시간*: {now_kst.strftime('%Y-%m-%d %H:%M:%S')} KST
    """
        self.notifier.send_notification(message)

    def create_issue_ticket_message(self, line):
        """입금 확인 완료 메시지"""
        parts = line[line.find('&') + 1:].strip().rsplit(' ', 2)
        name, deposit_amount, remaining_amount = parts[0], parts[1], parts[2]
        now_kst = self._get_kst_now()
        message = f"""💰 *입금 확인 완료* 💰
        -  💌 *받는 분 통장 표시*: {name}
        -  💰 *금액*: {deposit_amount.strip()}원
        -  🏦 *잔액*: {remaining_amount.strip()}원
        -  ⏰ *시간*: {now_kst.strftime('%Y-%m-%d %H:%M:%S')} KST
        """
        self.notifier.send_notification(message)

    def create_failed_issue_ticket_message_amount(self, line):
        """이용권 발급 실패 - 금액 오류"""
        name, depositAmount = line[line.find('&') + 1:].split(' ')
        now_kst = self._get_kst_now()

        ticket_policy_message = f"- 💰 현재 가격 정책: {self.config.ticket_price_policy}"
        ticket_registered_message = f"- 🌱 프로필 등록 완료 첫 구매 고객: {self.config.ticket_price_registered_policy}"

        message = f"""<!channel> 🚨 *이용권 발급 실패* 🚨
    💌 입금금액에 해당하는 티켓 가격 정보가 없습니다.
    -  💌 *받는 분 통장 표시*: {name}
    -  💰 *금액*: {depositAmount.strip()}원
    -  ⏰ *시간*: {now_kst.strftime('%Y-%m-%d %H:%M:%S')} KST
    {ticket_policy_message}
    {ticket_registered_message}
    """
        self.notifier.send_notification(message)

    def create_failed_issue_ticket_message_verification(self, line):
        """이용권 발급 실패 - 인증번호 오류"""
        name, depositAmount = line[line.find('&') + 1:].split(' ')
        now_kst = self._get_kst_now()

        message = f"""<!channel> 🚨 *이용권 발급 실패* 🚨
    💌 받는 분 통장 표시에 해당하는 인증번호가 없습니다.
    -  💌 *받는 분 통장 표시*: {name}
    -  💰 *금액*: {depositAmount.strip()}원
    -  ⏰ *시간*: {now_kst.strftime('%Y-%m-%d %H:%M:%S')} KST
    """
        self.notifier.send_notification(message)

    def create_pay_notification_message(self, line):
        """결제 확인 요청 메시지"""
        name, verification = line[line.find('&') + 1:].split(' ')
        now_kst = self._get_kst_now()

        message = f"""🚨🚨 *결제 확인 요청이 접수되었습니다.* 🚨🚨
        -  💌 *받는 분 통장 표시*: {name}
        -  💖 *인증 번호*: {verification}
        -  ⏰ *시간*: {now_kst.strftime('%Y-%m-%d %H:%M:%S')} KST
        """
        self.notifier.send_notification(message)

    def create_no_first_purchased_ticket_message(self, line):
        """첫 구매 실패 메시지"""
        name, depositAmount = line[line.find('&') + 1:].split(' ')
        now_kst = self._get_kst_now()

        message = f"""<!channel> 🚨 *현장 확인 필요! 프로필을 등록하지 않거나 첫번째 구매가 아닌 사용자입니다.* 🚨
        -  💌 *받는 분 통장 표시*: {name}
        -  💰 *금액*: {depositAmount}
        -  ⏰ *시간*: {now_kst.strftime('%Y-%m-%d %H:%M:%S')} KST
        """
        self.notifier.send_notification(message)

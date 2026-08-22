import json
import time
import pytz
import os
import requests
from datetime import datetime
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler
from dotenv import load_dotenv

load_dotenv()
ENVIRONMENT = os.getenv('ENVIRONMENT')
SLACK_TOKEN = os.getenv('SLACK_TOKEN')
SLACK_CHANNEL = os.getenv('SLACK_CHANNEL')
SLACK_ADMIN_CHANNEL = os.getenv('SLACK_ADMIN_CHANNEL')
SLACK_LOG_CHANNEL = os.getenv('SLACK_LOG_CHANNEL')

TICKET_PRICE_REGISTERED_POLICY = os.getenv('TICKET_PRICE_REGISTERED_POLICY')
TICKET_PRICE_POLICY = os.getenv('TICKET_PRICE_POLICY')

BANK_NAME = os.getenv('BANK_NAME')
BANK_ACCOUNT = os.getenv('BANK_ACCOUNT')

SLACK_WEBHOOK_URL = 'https://slack.com/api/chat.postMessage'

SERVER_RESTART = 'INFO org.springframework.boot.web.embedded.tomcat.TomcatWebServer - Tomcat started on port'
INTERNAL_ERROR_LOG_PREFIX = 'ERROR com.yourssu.signal.handler.InternalServerErrorControllerAdvice -'

CREATE_PROFILE_PREFIX = 'INFO com.yourssu.signal.infrastructure.Notification - CreateProfile'
FAILED_PROFILE_CONTACT_PREFIX = 'INFO com.yourssu.signal.infrastructure.Notification - FailedProfileContactExceedsLimit'
CONTACT_EXCEEDS_WARNING_PREFIX = 'INFO com.yourssu.signal.infrastructure.Notification - ContactExceedsLimitWarning'
ISSUE_TICKET_PREFIX = 'INFO com.yourssu.signal.infrastructure.Notification - Issued ticket'
RETRY_ISSUE_TICKET_PREFIX = 'INFO com.yourssu.signal.infrastructure.Notification - RetryIssuedTicket'
CONSUME_TICKET_PREFIX = 'INFO com.yourssu.signal.infrastructure.Notification - Consumed ticket'
ISSUE_TICKET_BY_BANK_DEPOSIT_PREFIX = 'INFO com.yourssu.signal.infrastructure.Notification - IssueTicketByBankDepositSms'
FAILED_BY_BANK_DEPOSIT_PREFIX = 'INFO com.yourssu.signal.infrastructure.Notification - IssueFailedTicketByDepositAmount'
FAILED_BY_UNMATCHED_VERIFICATION_PREFIX = 'INFO com.yourssu.signal.infrastructure.Notification - IssueFailedTicketByUnMatchedVerification'
PAY_NOTIFICATION_PREFIX = 'INFO com.yourssu.signal.infrastructure.Notification - PayNotification'
NO_FIRST_PURCHASED_TICKET_PREFIX = 'INFO com.yourssu.signal.infrastructure.Notification - NoFirstPurchasedTicket'


def to_ticket_price_message(message):
    return message.replace('n', '원/').replace('.', '장 ') + '장'


ticket_policy_message = f"- 💰 현재 가격 정책: {to_ticket_price_message(TICKET_PRICE_POLICY)}"
ticket_registered_message = f"- 🌱 프로필 등록 완료 첫 구매 고객: {to_ticket_price_message(TICKET_PRICE_REGISTERED_POLICY)}"


def create_server_restart_message(line):
    message = f"🟢 {ENVIRONMENT.upper()} SERVER RESTARTED - 시그널 API \n\n{ticket_policy_message}\n \n{ticket_registered_message}"
    send_slack_notification(message)


def create_internal_error_message(line):
    message = f"🚨ALERT ERROR - {ENVIRONMENT.upper()} SERVER🚨\n{line.replace(INTERNAL_ERROR_LOG_PREFIX, '')}"
    send_slack_log_notification(message)


def send_slack_log_notification(message):
    payload = {
        'channel': SLACK_LOG_CHANNEL,
        'text': message
    }
    headers = {
        'Authorization': f'Bearer {SLACK_TOKEN}',
        'Content-Type': 'application/json'
    }
    log = requests.post(SLACK_WEBHOOK_URL, json=payload, headers=headers)
    print(log.text)


def send_slack_admin_notification(message):
    payload = {
        'channel': SLACK_ADMIN_CHANNEL,
        'text': message
    }
    headers = {
        'Authorization': f'Bearer {SLACK_TOKEN}',
        'Content-Type': 'application/json'
    }
    log = requests.post(SLACK_WEBHOOK_URL, json=payload, headers=headers)
    print(log.text)


def create_profile_message(line):
    id, department, contact, nickname, introSentences = line[line.find('&') + 1:].split('&')
    message = f"""🩷 *프로필 등록 완료* 🩷
    -  💖 *식별 번호*: {id}
    -  🏢 *학과*: {department}
    -  📞 *연락처*: https://www.instagram.com/{contact.replace('@', '')}
    -  👤 *닉네임*: {nickname}
    -  📝 *자기소개*: {introSentences}
    """
    append_or_create_file("/home/ubuntu/signal-api/createProfiles.txt", message)
#    send_slack_admin_notification(message)


def create_failed_profile_contact_message(line):
    contact_policy = line[line.find('&') + 1:].strip()
    message = f"""🚨🚨 같은 연락처 등록 실패 - {ENVIRONMENT.upper()} SERVER 🚨🚨
    - ⚔️ 중복 연락처 제한 기준: {contact_policy} 개
    """
    append_or_create_file("/home/ubuntu/signal-api/createProfiles.txt", message)
    send_slack_log_notification(message)


def create_contact_exceeds_warning_message(line):
    contact_policy = line[line.find('&') + 1:].strip()
    message = f"""🚨 같은 연락처 등록 경고 - {ENVIRONMENT.upper()} SERVER 🚨
    - ⚔️ 중복 연락처 경고 기준: {contact_policy} 개
    """
    append_or_create_file("/home/ubuntu/signal-api/createProfiles.txt", message)
    send_slack_log_notification(message)


def create_issued_ticket_message(line):
    verification, uuid, ticket, available_ticket = line[line.find('&') + 1:].split(' ')

    # 한국 시간대 설정
    kst = pytz.timezone('Asia/Seoul')
    now_kst = datetime.now(pytz.utc).astimezone(kst)

    message = f"""🩷 *이용권 발급 완료* 🩷

    -  💖 *인증 번호*: {str(verification).zfill(4)}
    -  😀 *식별 번호*: {uuid}
    -  🎁 *발급한 이용권*: {int(ticket)}장
    -  💝 *보유 이용권*: {int(available_ticket)}장
    -  💌 *발급 시간*: {now_kst.strftime('%Y-%m-%d %H:%M:%S')} KST

    *이용권 발급 방법 안내*
    *자동 발급*
        - 🎁 계좌번호: {BANK_NAME} {BANK_ACCOUNT}
        - 💌 받는 분 통장 표시: {str(verification).zfill(4)}
        {ticket_policy_message}
        {ticket_registered_message}

    *수동 발급*
    `/t {str(verification).zfill(4)} <개수>`
    입금 확인 후 이용권을 발급해주세요!
    """
    send_slack_notification(message)


def create_retry_issued_ticket_message(line):
    verification, uuid, ticket, available_ticket, name = line[line.find('&') + 1:].split(' ')

    # 한국 시간대 설정
    kst = pytz.timezone('Asia/Seoul')
    now_kst = datetime.now(pytz.utc).astimezone(kst)

    message = f"""💌 *결제 확인 요청 이용권 발급 완료* 💌
    -  💌 *받는 분 통장 표시*: {name}
    -  💖 *인증 번호*: {str(verification).zfill(4)}
    -  😀 *식별 번호*: {uuid}
    -  🎁 *발급한 이용권*: {int(ticket)}장
    -  💝 *보유 이용권*: {int(available_ticket)}장
    -  💌 *발급 시간*: {now_kst.strftime('%Y-%m-%d %H:%M:%S')} KST

    *이용권 발급 방법 안내*
    *자동 발급*
        - 🎁 계좌번호: {BANK_NAME} {BANK_ACCOUNT}
        - 💌 받는 분 통장 표시: {str(verification).zfill(4)}
        {ticket_policy_message}
        {ticket_registered_message}

    *수동 발급*
    `/t {str(verification).zfill(4)} <개수>`
    입금 확인 후 이용권을 발급해주세요!
    """
    send_slack_notification(message)


def create_consumed_ticket_message(line):
    nickname, ticket = line[line.find('&') + 1:].split(' ')
    if ticket == '0':
        return

    kst = pytz.timezone('Asia/Seoul')
    now_kst = datetime.now(pytz.utc).astimezone(kst)
    message = f"""🩷 *누군가 {nickname}님께 시그널을 보냈어요.* 🩷
    -  💌 *보낸 시간*: {now_kst.strftime('%Y-%m-%d %H:%M:%S')} KST
    """
    send_slack_notification(message)


def create_issue_ticket_message(line):
    name, deposit_amount = line[line.find('&') + 1:].split(' ')
    kst = pytz.timezone('Asia/Seoul')
    now_kst = datetime.now(pytz.utc).astimezone(kst)
    message = f"""💰 *입금 확인 완료* 💰
        -  💌 *받는 분 통장 표시*: {name}
        -  💰 *금액*: {deposit_amount.strip()}원
        -  ⏰ *시간*: {now_kst.strftime('%Y-%m-%d %H:%M:%S')} KST
        """
    send_slack_notification(message)


def create_failed_issue_ticket_message_amount(line):
    name, depositAmount = line[line.find('&') + 1:].split(' ')

    kst = pytz.timezone('Asia/Seoul')
    now_kst = datetime.now(pytz.utc).astimezone(kst)
    message = f"""🚨 *이용권 발급 실패* 🚨
    💌 입금금액에 해당하는 티켓 가격 정보가 없습니다.
    -  💌 *받는 분 통장 표시*: {name}
    -  💰 *금액*: {depositAmount.strip()}원
    -  ⏰ *시간*: {now_kst.strftime('%Y-%m-%d %H:%M:%S')} KST
    {ticket_policy_message}
    {ticket_registered_message}
    """
    send_slack_notification(message)


def create_failed_issue_ticket_message_verification(line):
    name, depositAmount = line[line.find('&') + 1:].split(' ')
    kst = pytz.timezone('Asia/Seoul')
    now_kst = datetime.now(pytz.utc).astimezone(kst)

    message = f"""🚨 *이용권 발급 실패* 🚨
    💌 받는 분 통장 표시에 해당하는 인증번호가 없습니다.
    -  💌 *받는 분 통장 표시*: {name}
    -  💰 *금액*: {depositAmount.strip()}원
    -  ⏰ *시간*: {now_kst.strftime('%Y-%m-%d %H:%M:%S')} KST
    """
    send_slack_notification(message)


def create_pay_notification_message(line):
    name, verification = line[line.find('&') + 1:].split(' ')
    kst = pytz.timezone('Asia/Seoul')
    now_kst = datetime.now(pytz.utc).astimezone(kst)

    message = f"""🚨🚨 *결제 확인 요청이 접수되었습니다.* 🚨🚨
        -  💌 *받는 분 통장 표시*: {name}
        -  💖 *인증 번호*: {verification}
        -  ⏰ *시간*: {now_kst.strftime('%Y-%m-%d %H:%M:%S')} KST
        """
    send_slack_notification(message)


def create_no_first_purchased_ticket_message(line):
    name, depositAmount = line[line.find('&') + 1:].split(' ')
    kst = pytz.timezone('Asia/Seoul')
    now_kst = datetime.now(pytz.utc).astimezone(kst)

    message = f"""🚨 *현장 확인 필요! 프로필을 등록하지 않거나 첫번째 구매가 아닌 사용자입니다.* 🚨
        -  💌 *받는 분 통장 표시*: {name}
        -  💰 *금액*: {depositAmount}
        -  ⏰ *시간*: {now_kst.strftime('%Y-%m-%d %H:%M:%S')} KST
        """
    send_slack_notification(message)


handler = {
    SERVER_RESTART: create_server_restart_message,
    INTERNAL_ERROR_LOG_PREFIX: create_internal_error_message,
    CREATE_PROFILE_PREFIX: create_profile_message,
    ISSUE_TICKET_PREFIX: create_issued_ticket_message,
    RETRY_ISSUE_TICKET_PREFIX: create_retry_issued_ticket_message,
    # CONSUME_TICKET_PREFIX: create_consumed_ticket_message,
    FAILED_PROFILE_CONTACT_PREFIX: create_failed_profile_contact_message,
    CONTACT_EXCEEDS_WARNING_PREFIX: create_contact_exceeds_warning_message,
    ISSUE_TICKET_BY_BANK_DEPOSIT_PREFIX: create_issue_ticket_message,
    FAILED_BY_BANK_DEPOSIT_PREFIX: create_failed_issue_ticket_message_amount,
    FAILED_BY_UNMATCHED_VERIFICATION_PREFIX: create_failed_issue_ticket_message_verification,
    PAY_NOTIFICATION_PREFIX: create_pay_notification_message,
    NO_FIRST_PURCHASED_TICKET_PREFIX: create_no_first_purchased_ticket_message
}


def send_slack_notification(message):
    payload = {
        'channel': SLACK_CHANNEL,
        'text': message
    }
    headers = {
        'Authorization': f'Bearer {SLACK_TOKEN}',
        'Content-Type': 'application/json'
    }
    log = requests.post(SLACK_WEBHOOK_URL, json=payload, headers=headers)
    print(log.text)


def append_or_create_file(filename, content):
    with open(filename, 'a', encoding='utf-8') as f:
        f.write(content)


last_checked_line = dict()


def check(file_path):
    global last_checked_line

    with open(file_path, 'r') as file:
        lines = file.readlines()
        if file_path not in last_checked_line:
            last_checked_line[file_path] = len(lines)
        lines = lines[last_checked_line.get(file_path):]

    for line in lines:
        for prefix, handler_func in handler.items():
            if prefix in line:
                try:
                    handler_func(line)
                except Exception:
                    message = f"🚨ALERT ERROR - {ENVIRONMENT.upper()} SERVER🚨\nlogging: {line}"
                    print(message)
                    send_slack_log_notification(message)
                break

    last_checked_line[file_path] += len(lines)


class LogHandler(FileSystemEventHandler):
    def on_modified(self, event):
        if event.is_directory:
            return

        if event.src_path.endswith('.log'):  # 로그 파일만 감지
            check(event.src_path)


if __name__ == "__main__":
    path = "logs/"
    event_handler = LogHandler()
    observer = Observer()
    observer.schedule(event_handler, path, recursive=True)
    observer.start()
    message = f"Observer started: {datetime.now()}"
    print(message)
    send_slack_log_notification(message)
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        observer.stop()
    observer.join()

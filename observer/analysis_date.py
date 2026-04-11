import json
import math
import os
import requests

from collections import defaultdict
from dotenv import load_dotenv
from datetime import datetime, timedelta

load_dotenv()
SLACK_TOKEN = os.getenv('SLACK_TOKEN')
SLACK_CHANNEL = os.getenv('SLACK_CHANNEL')
SLACK_LOG_CHANNEL = 'C08SZDPGSRX'

SLACK_WEBHOOK_URL = 'https://slack.com/api/chat.postMessage'

CREATED_FIXTURE = "\"Status\":201"
CREATE_PROFILE_PREFIX = '{"Reply":{"Method":"POST /api/profiles - '
ISSUE_TICKET_PREFIX = 'INFO com.yourssu.signal.infrastructure.Notification - Issued ticket'
RETRY_ISSUE_TICKET_PREFIX = 'INFO com.yourssu.signal.infrastructure.Notification - RetryIssuedTicket'
CONSUME_TICKET_PREFIX = 'INFO com.yourssu.signal.infrastructure.Notification - Consumed ticket'

CREATE_PROFILE_KEY = "createProfile"
CONSUMED_TICKET_KEY = "consumedTicket"
ISSUED_TICKET_KEY = "issuedTicket"


def get_log_lines_from(start_datetime: datetime) -> list:
    now = datetime.now()
    time_threshold = start_datetime
    recent_lines = []
    date_list = []
    current_date_to_scan = start_datetime.date()
    while current_date_to_scan <= now.date():
        date_list.append(current_date_to_scan.strftime('%Y-%m-%d'))
        current_date_to_scan += timedelta(days=1)

    directories = [f'/home/ubuntu/signal-api/logs/{date_str}/' for date_str in date_list]

    for directory in directories:
        if not os.path.exists(directory):
            continue

        for filename in os.listdir(directory):
            if filename.endswith('.log'):
                filepath = os.path.join(directory, filename)
                try:
                    with open(filepath, 'r', encoding='utf-8') as file:
                        for line in file:
                            try:
                                timestamp_str = ' '.join(line.split(' ')[:2])
                                log_time = datetime.strptime(timestamp_str, '%Y-%m-%d %H:%M:%S.%f')
                                if log_time >= time_threshold and log_time <= now:
                                    recent_lines.append(line.strip())
                            except ValueError:
                                continue
                            except Exception:
                                continue
                except Exception as e:
                    print(f"파일 열기 실패: {filepath}, 에러: {e}")
    return recent_lines


def count_ip_addresses(log_lines) -> int:
    ips = set()
    for line in log_lines:
        try:
            json_str = line.split(' - ', 1)[1].strip()
            data = json.loads(json_str)
            x_real_ip = data.get('Request', {}).get('Headers', {}).get('x-real-ip')
            if x_real_ip:
                ips.add(x_real_ip)
        except Exception:
            continue
    return len(ips)


def create_count_visitor_message(start_time) -> int:
    recent_log_lines = get_log_lines_from(start_time)
    return count_ip_addresses(recent_log_lines)


def create_analysis_message(start_time, visitor_count, profile_count, issued_ticket_count, consume_ticket_count) -> str:
    return f""" *💌 시그널 최근 {hours} 시간 분석 보고서 💌*
    - *📅  분석 기간* : {start_time.strftime('%Y년 %m월 %d일 %H시 %M분')} ~ {datetime.now().strftime('%Y년 %m월 %d일 %H시 %M분')}
    - *👥  방문자 수* : {visitor_count} 명
    - *👤 등록한 프로필* : {profile_count} 개
    - *🎁  발급한 이용권* : {issued_ticket_count} 개
    - *💌  사용한 이용권* : {consume_ticket_count} 개
"""


def get_created_profile(line, dic):
    if CREATED_FIXTURE in line:
        dic[CREATE_PROFILE_KEY] += 1


def get_issued_ticket(line, dic):
    verification, uuid, ticket, available_ticket = line[line.find('&') + 1:].split(' ')
    dic[ISSUED_TICKET_KEY] += int(ticket)


def get_ticket_by_bank_deposit(line, dic):
    verification, uuid, ticket, available_ticket, name = line[line.find('&') + 1:].split(' ')
    dic[ISSUED_TICKET_KEY] += int(ticket)


def get_consumed_ticket_message(line, dic):
    dic[CONSUMED_TICKET_KEY] += 1


handler = {
    CREATE_PROFILE_PREFIX: get_created_profile,
    ISSUE_TICKET_PREFIX: get_issued_ticket,
    RETRY_ISSUE_TICKET_PREFIX: get_ticket_by_bank_deposit,
    CONSUME_TICKET_PREFIX: get_consumed_ticket_message,
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

def append_or_create_file(filename, content):
    with open(filename, 'a', encoding='utf-8') as f:
        f.write(content)


def run(start_time: datetime):
    dic = defaultdict(int)
    for line in get_log_lines_from(start_time):
        for prefix, handler_func in handler.items():
            if prefix in line:
                try:
                    handler_func(line, dic)
                except Exception:
                    continue
    visit_count = create_count_visitor_message(start_time)
    profile_count = dic[CREATE_PROFILE_KEY]
    issued_ticket_count = dic[ISSUED_TICKET_KEY]
    consume_ticket_count = dic[CONSUMED_TICKET_KEY]
    message = create_analysis_message(start_time=start_time,
                                      visitor_count=visit_count,
                                      profile_count=profile_count,
                                      issued_ticket_count=issued_ticket_count,
                                      consume_ticket_count=consume_ticket_count)
    send_slack_notification(message)
    append_or_create_file("/home/ubuntu/signal-api/analysis_date.txt", f"\n{message}\n")


def to_datetime(date_str, date_format="%Y-%m-%d %H:%M") -> datetime:
    try:
        return datetime.strptime(date_str, date_format)
    except ValueError:
        raise ValueError(f"Invalid date format: {date_str}. Expected format: {date_format}")


def get_total_hours(start_time: datetime) -> int:
    diff = datetime.now() - start_time
    total_hours = diff.total_seconds() / 3600
    return math.ceil(total_hours)


if __name__ == "__main__":
    start_time = to_datetime("2025-05-19 18:00")
    hours = get_total_hours(start_time)
    run(start_time)

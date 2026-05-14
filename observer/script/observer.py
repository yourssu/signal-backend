import time
import os
import glob
import requests
import pytz
from datetime import datetime
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler
from dotenv import load_dotenv
from log_handlers import LogHandlers
from signal_handler import SignalHandler


class Config:
    def __init__(self):
        load_dotenv(override=True)
        self.environment = os.getenv('ENVIRONMENT')
        self.slack_token = os.getenv('SLACK_TOKEN')
        self.slack_channel = os.getenv('SLACK_CHANNEL')
        self.slack_admin_channel = os.getenv('SLACK_ADMIN_CHANNEL')
        self.slack_log_channel = os.getenv('SLACK_LOG_CHANNEL')
        self.ticket_price_registered_policy = os.getenv('TICKET_PRICE_REGISTERED_POLICY')
        self.ticket_price_policy = os.getenv('TICKET_PRICE_POLICY')
        self.slack_webhook_url = 'https://slack.com/api/chat.postMessage'


class SlackNotifier:
    def __init__(self, config):
        self.config = config
        
    def _send_notification(self, channel: str, message: str):
        payload = {
            'channel': channel,
            'text': message
        }
        headers = {
            'Authorization': f'Bearer {self.config.slack_token}',
            'Content-Type': 'application/json'
        }
        try:
            response = requests.post(self.config.slack_webhook_url, json=payload, headers=headers, timeout=10)
            print(response.text)
        except Exception as e:
            print(f"Slack 알림 전송 실패: {e}")
        
    def send_notification(self, message: str):
        self._send_notification(self.config.slack_channel, message)
        
    def send_admin_notification(self, message: str):
        self._send_notification(self.config.slack_admin_channel, message)
        
    def send_log_notification(self, message: str):
        self._send_notification(self.config.slack_log_channel, message)


class TimeUtils:
    @staticmethod
    def get_kst_now() -> str:
        kst = pytz.timezone('Asia/Seoul')
        now_kst = datetime.now(pytz.utc).astimezone(kst)
        return now_kst.strftime('%Y-%m-%d %H:%M:%S')

config = Config()
notifier = SlackNotifier(config)
log_handlers = LogHandlers(config, notifier)
signal_handler = SignalHandler(config, notifier)






last_checked_line = dict()


def process_line_with_handlers(line, handlers):
    for prefix, handler_func in handlers.items():
        if prefix in line:
            try:
                handler_func(line)
            except Exception as e:
                error_message = f"🚨ALERT ERROR - {config.environment.upper()} SERVER🚨\nlogging: {line}\nError: {str(e)}"
                print(error_message)
                notifier.send_log_notification(error_message)
            break


def check(file_path):
    global last_checked_line

    with open(file_path, 'r', encoding='utf-8', errors='ignore') as file:
        lines = file.readlines()
        if file_path not in last_checked_line:
            last_checked_line[file_path] = len(lines)
        lines = lines[last_checked_line.get(file_path):]

    for line in lines:
        process_line_with_handlers(line, log_handlers.handlers)
        process_line_with_handlers(line, signal_handler.handlers)

    last_checked_line[file_path] += len(lines)


class LogHandler(FileSystemEventHandler):
    @staticmethod
    def on_created(event):
        if event.is_directory:
            return
        if event.src_path.endswith('.log'):
            # 새로 생성된 파일은 처음부터 처리 (날짜 변경 등으로 새 파일 생성 시)
            last_checked_line[event.src_path] = 0

    @staticmethod
    def on_modified(event):
        if event.is_directory:
            return

        if event.src_path.endswith('.log'):
            check(event.src_path)


if __name__ == "__main__":
    path = os.path.abspath("logs/")
    # 재시작 시 기존 로그 파일의 현재 줄 수를 미리 기록
    # (같은 날 재시작 시 기존 내용을 신규 로그로 오인하지 않도록)
    for existing_log in glob.glob(os.path.join(path, "**/*.log"), recursive=True):
        try:
            with open(existing_log, "r", encoding="utf-8", errors="ignore") as f:
                last_checked_line[existing_log] = len(f.readlines())
        except Exception:
            pass

    event_handler = LogHandler()
    observer = Observer()
    observer.schedule(event_handler, path, recursive=True)
    observer.start()
    start_message = f"Observer started: {TimeUtils.get_kst_now()}"
    print(start_message)
    notifier.send_log_notification(start_message)
    HEALTH_CHECK_INTERVAL = 30
    tick = 0
    try:
        while True:
            time.sleep(1)
            tick += 1
            if tick >= HEALTH_CHECK_INTERVAL:
                tick = 0
                if not observer.is_alive():
                    msg = f"🚨 Observer watchdog thread died: {TimeUtils.get_kst_now()}\n재시작 필요: docker restart signal-backend-container"
                    print(msg)
                    notifier.send_log_notification(msg)
    except KeyboardInterrupt:
        observer.stop()
    observer.join()

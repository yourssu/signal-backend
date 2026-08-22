import json
import os
import time
from datetime import datetime

from dotenv import load_dotenv

from log_cursor import DurableLogCursor
from log_handlers import LogHandlers
from log_router import handlers_for_path
from signal_handler import SignalHandler
from slack_notifier import SlackNotifier


class Config:
    def __init__(self):
        load_dotenv(override=True)
        self.environment = os.getenv("ENVIRONMENT", "dev").upper()
        self.project_name = os.getenv("PROJECT_NAME", "signal-backend")
        self.slack_token = os.getenv("SLACK_TOKEN")
        self.slack_channel = os.getenv("SLACK_CHANNEL")
        self.slack_admin_channel = os.getenv("SLACK_ADMIN_CHANNEL")
        self.slack_log_channel = os.getenv("SLACK_LOG_CHANNEL")
        self.ticket_price_registered_policy = os.getenv("TICKET_PRICE_REGISTERED_POLICY")
        self.ticket_price_policy = os.getenv("TICKET_PRICE_POLICY")
        self.slack_webhook_url = "https://slack.com/api/chat.postMessage"
        self.slack_queue_path = "/app/logs/state/slack-queue.jsonl"


class ObserverRuntime:
    BACKLOG_BYTES = 10 * 1024 * 1024
    BACKLOG_DURATION_SECONDS = 300

    def __init__(self, log_root="/app/logs", config=None):
        self.log_root = log_root
        self.state_root = os.path.join(log_root, "state")
        os.makedirs(self.state_root, exist_ok=True)
        self.config = config or Config()
        self.notifier = SlackNotifier(self.config)
        self.log_handlers = LogHandlers(self.config, self.notifier)
        self.signal_handler = SignalHandler(self.config, self.notifier)
        self.last_heartbeat = None
        self.started_at = time.time()
        self.heartbeat_alerted = False
        self.log_io_alerted = False
        self.offset_alerted = set()
        self.incident_threads = {}
        self.cursors = [
            DurableLogCursor(os.path.join(log_root, "events", "notification-events.log"), os.path.join(self.state_root, "event-offset.json"), True),
            DurableLogCursor(os.path.join(log_root, "app.log"), os.path.join(self.state_root, "app-offset.json"), True),
        ]

    def start_incident(self, key, message):
        sender = getattr(self.notifier, "start_log_incident", None)
        thread_ts = sender(message) if sender else None
        self.incident_threads[key] = thread_ts

    def resolve_incident(self, key, message):
        thread_ts = self.incident_threads.pop(key, None)
        sender = getattr(self.notifier, "reply_log_incident", None)
        if sender and thread_ts:
            sender(message, thread_ts)
        else:
            self.notifier.send_log_notification(message)

    def process_line(self, path, line):
        if "SIGNAL_HEARTBEAT" in line:
            self.last_heartbeat = time.time()
        handlers = handlers_for_path(path, self.log_handlers.handlers, self.signal_handler.handlers)
        if handlers is None:
            return
        for prefix, handler in handlers.items():
            if prefix in line:
                try:
                    handler(line)
                except Exception as error:
                    self.notifier.send_log_notification(
                        f"🟠 [{self.config.environment}] Observer 이벤트 처리 실패\n"
                        "```\n"
                        f"• 로그: {os.path.basename(path)}\n"
                        f"• 오류 유형: {type(error).__name__}\n"
                        "• 영향: 해당 이벤트 1건의 Slack 알림을 확인해야 합니다.\n```"
                    )
                return

    def poll(self):
        processed = 0
        for cursor in self.cursors:
            processed += cursor.poll(lambda line, path=cursor.path: self.process_line(path, line))
            if cursor.corrupted_reason and cursor.path not in self.offset_alerted:
                self.notifier.send_log_notification(
                    f"🟠 [{self.config.environment}] Observer 로그 읽기 위치 손상\n"
                    "```\n"
                    f"• 파일: {os.path.basename(cursor.path)}\n"
                    f"• 원인: {cursor.corrupted_reason}\n"
                    "• 조치: 안전한 위치로 복구해 처리를 계속합니다.\n```"
                )
                self.offset_alerted.add(cursor.path)
        self._write_health(processed)
        if processed:
            self.last_processed_at = self.observed_at()
        return processed

    def _write_health(self, processed):
        payload = {"checked_at": int(time.time()), "last_heartbeat": int(self.last_heartbeat) if self.last_heartbeat else None, "processed": processed}
        temporary = os.path.join(self.state_root, ".observer-health.tmp")
        health = os.path.join(self.state_root, "observer-health.json")
        with open(temporary, "w", encoding="utf-8") as file:
            json.dump(payload, file)
            file.flush()
            os.fsync(file.fileno())
        os.replace(temporary, health)

    def backlog_bytes(self):
        total = 0
        for cursor in self.cursors:
            try:
                total += max(0, os.path.getsize(cursor.path) - ((cursor.state or {}).get("offset", 0)))
            except OSError:
                pass
        return total

    def observed_at(self):
        return datetime.now().astimezone().isoformat(timespec="seconds")

    def run(self):
        backlog_alerted = False
        backlog_started_at = None
        maximum_backlog = 0
        self.last_processed_at = None
        while True:
            try:
                self.poll()
                if self.log_io_alerted:
                    self.resolve_incident("log-io",
                        f"🟢 [{self.config.environment}] Observer 로그 저장 복구\n"
                        "```\n• 상태: 처리 위치 저장과 로그 읽기가 다시 정상입니다.\n```"
                    )
                    self.log_io_alerted = False
            except OSError as error:
                if not self.log_io_alerted:
                    self.start_incident("log-io",
                        f"🔴 [{self.config.environment}] Observer 로그 처리 실패\n"
                        "```\n"
                        f"• 발생 시간: {self.observed_at()}\n"
                        f"• 컨테이너: {self.config.project_name}-observer\n"
                        f"• 오류 유형: {type(error).__name__}\n"
                        "• 영향: 처리 위치를 저장하지 못해 알림이 지연될 수 있습니다.\n"
                        "• 조치: 1초마다 자동 재시도하며, 복구 시 한 번만 알립니다.\n```"
                    )
                    self.log_io_alerted = True
                time.sleep(1)
                continue
            heartbeat_reference = self.last_heartbeat or self.started_at
            if time.time() - heartbeat_reference > 180 and not self.heartbeat_alerted:
                self.start_incident("heartbeat",
                    f"🔴 [{self.config.environment}] Spring → Observer Heartbeat 중단\n"
                    "```\n"
                    f"• 발생 시간: {self.observed_at()}\n"
                    f"• 확인 대상: {self.config.project_name}-spring, {self.config.project_name}-observer\n"
                    "• 기준: 180초 이상 신호 미수신\n"
                    "• 의미: Spring, 로그 공유, Observer 중 하나에 문제가 있을 수 있습니다.\n"
                    "• 조치: 감독 프로세스가 각 컨테이너 healthcheck를 확인합니다.\n```"
                )
                self.heartbeat_alerted = True
            elif time.time() - heartbeat_reference <= 180 and self.heartbeat_alerted:
                self.resolve_incident("heartbeat",
                    f"🟢 [{self.config.environment}] Spring → Observer Heartbeat 복구\n"
                    "```\n"
                    f"• 복구 시간: {self.observed_at()}\n"
                    "• 상태: 로그 이벤트 전달이 다시 정상입니다.\n```"
                )
                self.heartbeat_alerted = False
            backlog = self.backlog_bytes()
            if backlog > self.BACKLOG_BYTES:
                backlog_started_at = backlog_started_at or time.time()
                maximum_backlog = max(maximum_backlog, backlog)
            else:
                backlog_started_at = None
            if (
                backlog_started_at is not None
                and time.time() - backlog_started_at >= self.BACKLOG_DURATION_SECONDS
                and not backlog_alerted
            ):
                self.start_incident("backlog",
                    f"🟠 [{self.config.environment}] Observer 이벤트 처리 지연\n"
                    "```\n"
                    f"감지 시각   {self.observed_at()}\n"
                    f"지속 시간   {self.BACKLOG_DURATION_SECONDS}초 이상\n"
                    f"미처리량    {backlog // (1024 * 1024)} MB\n"
                    f"마지막 처리 {self.last_processed_at or '기록 없음'}\n"
                    "영향        Slack 알림 지연 가능\n```"
                )
                backlog_alerted = True
            elif backlog <= self.BACKLOG_BYTES and backlog_alerted:
                self.resolve_incident("backlog",
                    f"🟢 [{self.config.environment}] Observer 이벤트 적체 해소\n"
                    "```\n"
                    f"복구 시각   {self.observed_at()}\n"
                    f"최대 적체량 {maximum_backlog // (1024 * 1024)} MB\n"
                    f"남은 미처리 {backlog} bytes\n```"
                )
                backlog_alerted = False
                maximum_backlog = 0
            time.sleep(1)


if __name__ == "__main__":
    ObserverRuntime().run()

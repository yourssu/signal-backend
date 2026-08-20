import json
import os
import time
import shutil
from datetime import datetime, timezone

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
        self.disk_alerted = False
        self.offset_alerted = set()
        self.cursors = [
            DurableLogCursor(os.path.join(log_root, "events", "notification-events.log"), os.path.join(self.state_root, "event-offset.json"), True),
            DurableLogCursor(os.path.join(log_root, "app.log"), os.path.join(self.state_root, "app-offset.json"), True),
        ]

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
                    self.notifier.send_log_notification(f"[{self.config.environment}] ADMIN COMMAND FAILED: handler={type(error).__name__}")
                return

    def poll(self):
        processed = 0
        for cursor in self.cursors:
            processed += cursor.poll(lambda line, path=cursor.path: self.process_line(path, line))
            if cursor.corrupted_reason and cursor.path not in self.offset_alerted:
                self.notifier.send_log_notification(
                    f"[{self.config.environment}] OFFSET CORRUPTED: file={os.path.basename(cursor.path)} reason={cursor.corrupted_reason}"
                )
                self.offset_alerted.add(cursor.path)
        self._write_health(processed)
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

    def run(self):
        backlog = self.backlog_bytes()
        self.notifier.send_log_notification(
            f"[{self.config.environment}] OBSERVER STARTED: started_at={datetime.now(timezone.utc).isoformat()} "
            f"start_offset={sum((cursor.state or {}).get('offset', 0) for cursor in self.cursors)} backlog_bytes={backlog}"
        )
        backlog_alerted = False
        while True:
            try:
                self.poll()
            except OSError as error:
                self.notifier.send_log_notification(
                    f"[{self.config.environment}] LOG WRITE FAILED: error={type(error).__name__}"
                )
                time.sleep(1)
                continue
            free_bytes = shutil.disk_usage(self.log_root).free
            if free_bytes < 1024 * 1024 * 1024 and not self.disk_alerted:
                self.notifier.send_log_notification(f"[{self.config.environment}] DISK LOW: free_bytes={free_bytes}")
                self.disk_alerted = True
            elif free_bytes >= 1024 * 1024 * 1024:
                self.disk_alerted = False
            heartbeat_reference = self.last_heartbeat or self.started_at
            if time.time() - heartbeat_reference > 180 and not self.heartbeat_alerted:
                self.notifier.send_log_notification(f"[{self.config.environment}] HEARTBEAT MISSED: spring heartbeat older than 180s")
                self.heartbeat_alerted = True
            elif time.time() - heartbeat_reference <= 180:
                self.heartbeat_alerted = False
            backlog = self.backlog_bytes()
            if backlog > self.BACKLOG_BYTES and not backlog_alerted:
                self.notifier.send_log_notification(f"[{self.config.environment}] EVENT BACKLOG: pending_bytes={backlog}")
                backlog_alerted = True
            elif backlog <= self.BACKLOG_BYTES:
                backlog_alerted = False
            time.sleep(1)


if __name__ == "__main__":
    ObserverRuntime().run()

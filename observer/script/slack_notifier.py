import re

import requests

from durable_queue import DurableSlackQueue


class SlackNotifier:
    MAX_ATTEMPTS = 3

    def __init__(self, config):
        self.config = config
        queue_path = getattr(config, 'slack_queue_path', 'logs/state/slack-queue.jsonl')
        self.queue = DurableSlackQueue(queue_path)
        self.replaying = False
        self.last_message_ts = None

    def _send_notification(self, channel: str, message: str, queue_on_failure=True, thread_ts=None):
        payload = {
            'channel': channel,
            'text': message,
        }
        if thread_ts:
            payload['thread_ts'] = thread_ts
        headers = {
            'Authorization': f'Bearer {self.config.slack_token}',
            'Content-Type': 'application/json',
        }

        for attempt in range(1, self.MAX_ATTEMPTS + 1):
            self.last_message_ts = None
            failure = self._request(payload, headers)
            if failure is None:
                sent_ts = self.last_message_ts
                if not self.replaying:
                    self._replay_queue()
                self.last_message_ts = sent_ts
                return True

            if attempt == self.MAX_ATTEMPTS:
                print(f"Slack 알림 최종 실패 ({attempt}/{self.MAX_ATTEMPTS}): {failure}")
            else:
                print(f"Slack 알림 재시도 ({attempt}/{self.MAX_ATTEMPTS}): {failure}")

        if queue_on_failure:
            self.queue.enqueue(channel, message)
            print(f"SLACK DELIVERY FAILED: queued=1 pending={self.queue.pending_count()}")
        return False

    def _replay_queue(self):
        if self.queue.pending_count() == 0:
            return
        self.replaying = True
        try:
            completed, remaining = self.queue.replay(
                lambda queued_channel, queued_message: self._send_notification(
                    queued_channel, queued_message, queue_on_failure=False
                )
            )
        finally:
            self.replaying = False
        if completed:
            message = (
                f"🟢 [{self.config.environment.upper()}] Slack 알림 재전송 완료\n"
                "```\n"
                f"• 재전송 성공: {completed}건\n"
                f"• 남은 대기: {remaining}건\n```"
            )
            print(message)
            self._send_notification(self.config.slack_log_channel, message)

    def _request(self, payload, headers):
        try:
            response = requests.post(
                self.config.slack_webhook_url,
                json=payload,
                headers=headers,
                timeout=10,
            )
            if response.status_code != 200:
                return f"http_{response.status_code}"

            body = response.json()
            if body.get('ok') is True:
                self.last_message_ts = body.get('ts')
                return None
            return self._safe_error_code(body.get('error'))
        except Exception as error:
            return type(error).__name__

    @staticmethod
    def _safe_error_code(error):
        if isinstance(error, str) and re.fullmatch(r'[a-zA-Z0-9_.-]+', error):
            return error
        return 'unknown_error'

    def send_notification(self, message: str):
        return self._send_notification(self.config.slack_channel, message)

    def send_admin_notification(self, message: str):
        return self._send_notification(self.config.slack_admin_channel, message)

    def send_log_notification(self, message: str):
        return self._send_notification(self.config.slack_log_channel, message)

    def start_log_incident(self, message: str):
        if self._send_notification(self.config.slack_log_channel, message):
            return self.last_message_ts
        return None

    def reply_log_incident(self, message: str, thread_ts):
        return self._send_notification(
            self.config.slack_log_channel,
            message,
            thread_ts=thread_ts,
        )

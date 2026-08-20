import re

import requests


class SlackNotifier:
    MAX_ATTEMPTS = 3

    def __init__(self, config):
        self.config = config

    def _send_notification(self, channel: str, message: str):
        payload = {
            'channel': channel,
            'text': message,
        }
        headers = {
            'Authorization': f'Bearer {self.config.slack_token}',
            'Content-Type': 'application/json',
        }

        for attempt in range(1, self.MAX_ATTEMPTS + 1):
            failure = self._request(payload, headers)
            if failure is None:
                return True

            if attempt == self.MAX_ATTEMPTS:
                print(f"Slack 알림 최종 실패 ({attempt}/{self.MAX_ATTEMPTS}): {failure}")
            else:
                print(f"Slack 알림 재시도 ({attempt}/{self.MAX_ATTEMPTS}): {failure}")

        return False

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

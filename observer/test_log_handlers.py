import os
import sys
import unittest

SCRIPT_DIR = os.path.join(os.path.dirname(__file__), "script")
sys.path.insert(0, SCRIPT_DIR)

from log_handlers import LogHandlers


class Config:
    environment = "dev"
    ticket_price_policy = "test@2n1.small@3n2"
    ticket_price_registered_policy = "registered@1n1"


class RecordingNotifier:
    def __init__(self):
        self.messages = []

    def send_log_notification(self, message):
        self.messages.append(message)


class LogHandlersTest(unittest.TestCase):
    def test_server_restart_is_sent_to_monitoring_channel(self):
        notifier = RecordingNotifier()
        handlers = LogHandlers(Config(), notifier)

        handlers.create_server_restart_message("ignored log header")

        self.assertEqual(len(notifier.messages), 1)
        self.assertIn("🟢 [DEV] Spring API 기동 완료", notifier.messages[0])
        self.assertIn("요청 수신 가능", notifier.messages[0])


if __name__ == "__main__":
    unittest.main()

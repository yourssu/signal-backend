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

    def send_notification(self, message):
        self.messages.append(message)


class LogHandlersTest(unittest.TestCase):
    def test_server_restart_formats_ticket_policies_like_legacy_observer(self):
        notifier = RecordingNotifier()
        handlers = LogHandlers(Config(), notifier)

        handlers.create_server_restart_message("ignored log header")

        self.assertEqual(len(notifier.messages), 1)
        self.assertIn("test@2원/1장 small@3원/2장", notifier.messages[0])
        self.assertIn("registered@1원/1장", notifier.messages[0])
        self.assertNotIn("test@2n1.small@3n2", notifier.messages[0])


if __name__ == "__main__":
    unittest.main()

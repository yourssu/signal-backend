import os
import sys
import unittest
from unittest.mock import patch

SCRIPT_DIR = os.path.join(os.path.dirname(__file__), "..", "script")
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
    def test_server_restart_is_not_sent_separately_from_deployment_result(self):
        notifier = RecordingNotifier()
        handlers = LogHandlers(Config(), notifier)

        restart_prefix = "INFO org.springframework.boot.web.embedded.tomcat.TomcatWebServer - Tomcat started on port"

        self.assertNotIn(restart_prefix, handlers.handlers)
        self.assertEqual(notifier.messages, [])

    def test_same_internal_error_is_deduplicated_without_timestamp_or_trace_id(self):
        notifier = RecordingNotifier()
        handlers = LogHandlers(Config(), notifier)
        first = (
            "2026-08-21 10:00:00.000 [http] [traceId=trace-one] ERROR "
            "com.yourssu.signal.handler.InternalServerErrorControllerAdvice - IllegalStateException: same failure"
        )
        second = (
            "2026-08-21 10:01:00.000 [http] [traceId=trace-two] ERROR "
            "com.yourssu.signal.handler.InternalServerErrorControllerAdvice - IllegalStateException: same failure"
        )

        with patch("log_handlers.time.monotonic", side_effect=[1000, 1060]):
            handlers.create_internal_error_message(first)
            handlers.create_internal_error_message(second)

        self.assertEqual(len(notifier.messages), 1)
        self.assertIn("trace-one", notifier.messages[0])
        self.assertNotIn("trace-two", notifier.messages[0])
        self.assertIn("IllegalStateException: same failure", notifier.messages[0])


if __name__ == "__main__":
    unittest.main()

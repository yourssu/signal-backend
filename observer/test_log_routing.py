import os
import sys
import unittest

SCRIPT_DIR = os.path.join(os.path.dirname(__file__), "script")
sys.path.insert(0, SCRIPT_DIR)

from log_router import handlers_for_path


class LogRoutingTest(unittest.TestCase):
    def test_event_file_routes_only_to_signal_handlers(self):
        signal_handlers = {"event": object()}
        log_handlers = {"error": object()}

        self.assertIs(
            handlers_for_path("/app/logs/events/notification-events.log", log_handlers, signal_handlers),
            signal_handlers,
        )

    def test_application_file_routes_only_to_error_and_heartbeat_handlers(self):
        signal_handlers = {"event": object()}
        log_handlers = {"error": object()}

        self.assertIs(
            handlers_for_path("/app/logs/app.log", log_handlers, signal_handlers),
            log_handlers,
        )

    def test_rotated_event_archive_is_not_treated_as_application_log(self):
        self.assertIsNone(
            handlers_for_path(
                "/app/logs/events/archive/2026-08-20.0.log.gz",
                {"error": object()},
                {"event": object()},
            )
        )


if __name__ == "__main__":
    unittest.main()

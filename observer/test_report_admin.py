import importlib
import os
import sys
import types
import unittest
from unittest.mock import Mock, patch

SCRIPT_DIR = os.path.join(os.path.dirname(__file__), "script")
sys.path.insert(0, SCRIPT_DIR)


class FakeApp:
    def __init__(self, **_kwargs):
        pass

    def command(self, _name):
        return lambda function: function


sys.modules.setdefault("slack_bolt", types.SimpleNamespace(App=FakeApp))
sys.modules.setdefault("dotenv", types.SimpleNamespace(load_dotenv=lambda: None))
sys.modules.setdefault("requests", types.SimpleNamespace(post=Mock(), delete=Mock()))

admin = importlib.import_module("admin")


class ReportAdminTest(unittest.TestCase):
    def test_reply_report_calls_approve_api_with_environment_secret(self):
        command = {"channel_id": "admin-channel"}
        response = Mock(status_code=200)
        with patch.object(admin, "API_HOST", "http://127.0.0.1:9012"), \
                patch.object(admin, "ADMIN_ACCESS_KEY", "admin-secret"), \
                patch.object(admin.requests, "post", return_value=response) as post:
            actual = admin.reply_report("7", command)

        self.assertIs(response, actual)
        post.assert_called_once_with(
            "http://127.0.0.1:9012/api/reports/7/approve",
            json={"secretKey": "admin-secret"},
            headers={"Content-Type": "application/json"},
        )

    def test_report_command_approves_once_and_announces_success(self):
        ack, say, respond = Mock(), Mock(), Mock()
        command = {"text": "7", "channel_id": "admin-channel", "user_name": "admin"}
        with patch.object(admin, "SLACK_ADMIN_CHANNEL", "admin-channel"), \
                patch.object(admin, "reply_report", return_value=Mock(status_code=200)) as reply:
            admin.handle_report_command(ack, command, say, respond)

        ack.assert_called_once()
        reply.assert_called_once_with("7", command)
        say.assert_called_once()
        self.assertIn("신고 ID 7", say.call_args.args[0])

    def test_report_command_rejects_invalid_usage_without_api_call(self):
        ack, say, respond = Mock(), Mock(), Mock()
        with patch.object(admin, "SLACK_ADMIN_CHANNEL", "admin-channel"), \
                patch.object(admin, "reply_report") as reply:
            admin.handle_report_command(ack, {"text": "", "channel_id": "admin-channel"}, say, respond)

        reply.assert_not_called()
        self.assertIn("사용법", respond.call_args.args[0])

    def test_report_command_rejects_non_admin_channel(self):
        ack, say, respond = Mock(), Mock(), Mock()
        with patch.object(admin, "SLACK_ADMIN_CHANNEL", "admin-channel"), \
                patch.object(admin, "reply_report") as reply:
            admin.handle_report_command(ack, {"text": "7", "channel_id": "other-channel"}, say, respond)

        reply.assert_not_called()
        self.assertIn("관리자 채널", respond.call_args.args[0])


if __name__ == "__main__":
    unittest.main()

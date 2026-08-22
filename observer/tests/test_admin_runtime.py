import importlib
import os
import sys
import types
import unittest
from unittest.mock import Mock, patch

SCRIPT_DIR = os.path.join(os.path.dirname(__file__), "..", "script")
sys.path.insert(0, SCRIPT_DIR)


class FakeApp:
    def __init__(self, **_kwargs):
        self.commands = {}

    def command(self, name):
        def register(function):
            self.commands[name] = function
            return function
        return register


sys.modules.setdefault("slack_bolt", types.SimpleNamespace(App=FakeApp))
sys.modules.setdefault("dotenv", types.SimpleNamespace(load_dotenv=lambda: None))
sys.modules.setdefault("requests", types.SimpleNamespace(post=Mock(), delete=Mock()))

admin = importlib.import_module("admin")


class AdminRuntimeTest(unittest.TestCase):
    def test_api_requests_only_use_local_spring_container(self):
        response = Mock(status_code=200)
        with patch.object(admin, "API_HOST", "http://signal-backend-spring:9012"), \
                patch.object(admin, "ADMIN_ACCESS_KEY", "local-secret"), \
                patch.object(admin.requests, "post", return_value=response) as post:
            actual = admin.reply("1234", 2)

        self.assertIs(response, actual)
        post.assert_called_once_with(
            "http://signal-backend-spring:9012/api/viewers",
            json={"secretKey": "local-secret", "verificationCode": "1234", "ticket": 2},
            headers={"Content-Type": "application/json"},
        )

    def test_dev_ticket_command_calls_local_api_in_dev(self):
        ack, say, respond = Mock(), Mock(), Mock()
        response = Mock(status_code=200)
        command = {"text": "t 1234 2", "channel_id": "dev-payment-channel", "user_name": "tester"}
        with patch.object(admin, "ENVIRONMENT", "dev"), \
                patch.object(admin, "SLACK_CHANNEL", "dev-payment-channel"), \
                patch.object(admin, "reply", return_value=response) as reply:
            admin.app.commands["/dev"](ack, command, say, respond)

        ack.assert_called_once()
        reply.assert_called_once_with("1234", 2)
        self.assertIn("DEV 인증 성공", respond.call_args.args[0])

    def test_dev_blacklist_command_requires_admin_channel(self):
        ack, say, respond = Mock(), Mock(), Mock()
        with patch.object(admin, "ENVIRONMENT", "dev"), \
                patch.object(admin, "SLACK_ADMIN_CHANNEL", "admin-channel"), \
                patch.object(admin, "reply_add") as reply_add:
            admin.app.commands["/dev"](ack, {"text": "add 7", "channel_id": "other-channel"}, say, respond)

        reply_add.assert_not_called()
        self.assertIn("관리자 채널", respond.call_args.args[0])

    def test_dev_command_is_rejected_by_prod_admin(self):
        ack, say, respond = Mock(), Mock(), Mock()
        with patch.object(admin, "ENVIRONMENT", "prod"), patch.object(admin, "reply") as reply:
            admin.app.commands["/dev"](ack, {"text": "t 1234", "channel_id": "channel"}, say, respond)

        reply.assert_not_called()
        self.assertIn("DEV 환경", respond.call_args.args[0])

    def test_dev_report_command_calls_local_approve_api(self):
        ack, say, respond = Mock(), Mock(), Mock()
        response = Mock(status_code=200)
        with patch.object(admin, "ENVIRONMENT", "dev"), \
                patch.object(admin, "SLACK_ADMIN_CHANNEL", "admin-channel"), \
                patch.object(admin, "reply_report", return_value=response) as reply_report:
            admin.app.commands["/dev"](
                ack,
                {"text": "report 7", "channel_id": "admin-channel", "user_name": "tester"},
                say,
                respond,
            )

        reply_report.assert_called_once_with("7")
        self.assertIn("DEV 신고 승인 성공", say.call_args.args[0])


if __name__ == "__main__":
    unittest.main()

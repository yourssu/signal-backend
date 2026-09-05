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
        pass

    def command(self, _name):
        return lambda function: function


sys.modules.setdefault("slack_bolt", types.SimpleNamespace(App=FakeApp))
sys.modules.setdefault("dotenv", types.SimpleNamespace(load_dotenv=lambda: None))
sys.modules.setdefault("requests", types.SimpleNamespace(post=Mock(), delete=Mock()))

admin = importlib.import_module("admin")


class MeetingCancelAdminTest(unittest.TestCase):
    def test_reply_meeting_cancel_posts_secret_key_in_body(self):
        response = Mock(status_code=204)
        with patch.object(admin, "API_HOST", "http://signal-backend-spring:9012"), \
                patch.object(admin, "ADMIN_ACCESS_KEY", "test-local-secret"), \
                patch.object(admin.requests, "post", return_value=response) as post:
            actual = admin.reply_meeting_cancel("12")

        self.assertIs(response, actual)
        post.assert_called_once_with(
            "http://signal-backend-spring:9012/api/meetings/rooms/12/admin-cancel",
            json={"secretKey": "test-local-secret"},
            headers={"Content-Type": "application/json"},
            timeout=10,
        )

    def test_cancel_command_announces_success(self):
        ack, say, respond = Mock(), Mock(), Mock()
        command = {"text": "12", "channel_id": "admin-channel", "user_name": "admin"}
        with patch.object(admin, "ENVIRONMENT", "prod"), \
                patch.object(admin, "SLACK_ADMIN_CHANNEL", "admin-channel"), \
                patch.object(admin, "reply_meeting_cancel", return_value=Mock(status_code=204)) as reply:
            admin.handle_cancel_command(ack, command, say, respond)

        ack.assert_called_once()
        reply.assert_called_once_with("12")
        respond.assert_not_called()
        self.assertIn("12", say.call_args[0][0])

    def test_cancel_command_rejects_outside_admin_channel(self):
        ack, say, respond = Mock(), Mock(), Mock()
        command = {"text": "12", "channel_id": "other-channel"}
        with patch.object(admin, "ENVIRONMENT", "prod"), \
                patch.object(admin, "SLACK_ADMIN_CHANNEL", "admin-channel"), \
                patch.object(admin, "reply_meeting_cancel") as reply:
            admin.handle_cancel_command(ack, command, say, respond)

        reply.assert_not_called()
        say.assert_not_called()
        self.assertIn("관리자 채널", respond.call_args[0][0])

    def test_cancel_command_rejects_invalid_room_id(self):
        ack, say, respond = Mock(), Mock(), Mock()
        command = {"text": "abc", "channel_id": "admin-channel"}
        with patch.object(admin, "ENVIRONMENT", "prod"), \
                patch.object(admin, "SLACK_ADMIN_CHANNEL", "admin-channel"), \
                patch.object(admin, "reply_meeting_cancel") as reply:
            admin.handle_cancel_command(ack, command, say, respond)

        reply.assert_not_called()
        self.assertIn("사용법", respond.call_args[0][0])

    def test_cancel_command_reports_failure_body(self):
        ack, say, respond = Mock(), Mock(), Mock()
        command = {"text": "12", "channel_id": "admin-channel"}
        with patch.object(admin, "ENVIRONMENT", "prod"), \
                patch.object(admin, "SLACK_ADMIN_CHANNEL", "admin-channel"), \
                patch.object(
                    admin, "reply_meeting_cancel",
                    return_value=Mock(status_code=409, text='{"code":"ROOM_ALREADY_MATCHED"}'),
                ):
            admin.handle_cancel_command(ack, command, say, respond)

        say.assert_not_called()
        self.assertIn("ROOM_ALREADY_MATCHED", respond.call_args[0][0])

    def test_dev_cancel_subcommand_uses_same_api(self):
        ack, say, respond = Mock(), Mock(), Mock()
        command = {"text": "cancel 12", "channel_id": "admin-channel", "user_name": "admin"}
        with patch.object(admin, "ENVIRONMENT", "dev"), \
                patch.object(admin, "SLACK_ADMIN_CHANNEL", "admin-channel"), \
                patch.object(admin, "reply_meeting_cancel", return_value=Mock(status_code=204)) as reply:
            admin.handle_dev_command(ack, command, say, respond)

        reply.assert_called_once_with("12")
        self.assertIn("DEV", say.call_args[0][0])


class ReportAdminTest(unittest.TestCase):
    def test_reply_report_calls_local_spring_with_local_secret(self):
        response = Mock(status_code=200)
        with patch.object(admin, "API_HOST", "http://signal-backend-spring:9012"), \
                patch.object(admin, "ADMIN_ACCESS_KEY", "local-secret"), \
                patch.object(admin.requests, "post", return_value=response) as post:
            actual = admin.reply_report("7")

        self.assertIs(response, actual)
        post.assert_called_once_with(
            "http://signal-backend-spring:9012/api/reports/7/approve",
            json={"secretKey": "local-secret"},
            headers={"Content-Type": "application/json"},
        )

    def test_report_command_approves_once_and_announces_success(self):
        ack, say, respond = Mock(), Mock(), Mock()
        command = {"text": "7", "channel_id": "admin-channel", "user_name": "admin"}
        with patch.object(admin, "ENVIRONMENT", "prod"), \
                patch.object(admin, "SLACK_ADMIN_CHANNEL", "admin-channel"), \
                patch.object(
                    admin,
                    "reply_report",
                    return_value=Mock(status_code=200, json=Mock(return_value={"result": {"reportedProfileId": 46}})),
                ) as reply:
            admin.handle_report_command(ack, command, say, respond)

        ack.assert_called_once()
        reply.assert_called_once_with("7")
        say.assert_called_once()
        self.assertIn("*신고 ID*: 7", say.call_args.args[0])
        self.assertIn("*대상 프로필 ID*: 46", say.call_args.args[0])
        self.assertNotIn("REPORT_REWARD", say.call_args.args[0])

    def test_report_command_rejects_invalid_usage_without_api_call(self):
        ack, say, respond = Mock(), Mock(), Mock()
        with patch.object(admin, "ENVIRONMENT", "prod"), \
                patch.object(admin, "SLACK_ADMIN_CHANNEL", "admin-channel"), \
                patch.object(admin, "reply_report") as reply:
            admin.handle_report_command(ack, {"text": "", "channel_id": "admin-channel"}, say, respond)

        reply.assert_not_called()
        self.assertIn("사용법", respond.call_args.args[0])


if __name__ == "__main__":
    unittest.main()

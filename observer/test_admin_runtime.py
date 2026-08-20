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


class AdminRuntimeTest(unittest.TestCase):
    def test_ticket_command_routes_prod_and_dev_by_channel(self):
        response = Mock(status_code=200)
        with patch.object(admin, "SLACK_CHANNEL_PROD", "prod-channel"), \
                patch.object(admin, "API_HOST_PROD", "http://127.0.0.1:9012"), \
                patch.object(admin, "API_HOST_DEV", "https://dev.example"), \
                patch.object(admin, "SECRET_KEY_PROD", "prod-secret"), \
                patch.object(admin, "SECRET_KEY_DEV", "dev-secret"), \
                patch.object(admin.requests, "post", return_value=response) as post:
            actual = admin.reply("1234", 1, {"channel_id": "dev-channel"})

        self.assertIs(response, actual)
        post.assert_called_once_with(
            "https://dev.example/api/viewers",
            json={"secretKey": "dev-secret", "verificationCode": "1234", "ticket": 1},
            headers={"Content-Type": "application/json"},
        )

        with patch.object(admin, "SLACK_CHANNEL_PROD", "prod-channel"), \
                patch.object(admin, "API_HOST_PROD", "http://127.0.0.1:9012"), \
                patch.object(admin, "SECRET_KEY_PROD", "prod-secret"), \
                patch.object(admin.requests, "post", return_value=response) as post:
            admin.reply("5678", 2, {"channel_id": "prod-channel"})

        post.assert_called_once_with(
            "http://127.0.0.1:9012/api/viewers",
            json={"secretKey": "prod-secret", "verificationCode": "5678", "ticket": 2},
            headers={"Content-Type": "application/json"},
        )

    def test_admin_command_rejects_non_admin_channel(self):
        ack, say, respond = Mock(), Mock(), Mock()
        with patch.object(admin, "SLACK_CHANNEL_ADMIN", "admin-channel"), \
                patch.object(admin, "reply_add") as reply_add, \
                patch.object(admin, "reply_delete") as reply_delete:
            admin.handle_command(ack, {"text": "7", "channel_id": "other-channel"}, say, respond)

        reply_add.assert_not_called()
        reply_delete.assert_not_called()
        self.assertIn("관리자 채널", respond.call_args.args[0])


if __name__ == "__main__":
    unittest.main()

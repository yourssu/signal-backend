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
    def test_ticket_command_uses_local_spring_and_existing_admin_key(self):
        response = Mock(status_code=200)
        with patch.object(admin, "API_HOST", "http://127.0.0.1:9012"), \
                patch.object(admin, "ADMIN_ACCESS_KEY", "admin-secret"), \
                patch.object(admin.requests, "post", return_value=response) as post:
            actual = admin.reply("1234", 1, {"channel_id": "payment-channel"})

        self.assertIs(response, actual)
        post.assert_called_once_with(
            "http://127.0.0.1:9012/api/viewers",
            json={"secretKey": "admin-secret", "verificationCode": "1234", "ticket": 1},
            headers={"Content-Type": "application/json"},
        )

    def test_admin_command_rejects_non_admin_channel(self):
        ack, say, respond = Mock(), Mock(), Mock()
        with patch.object(admin, "SLACK_ADMIN_CHANNEL", "admin-channel"), \
                patch.object(admin, "reply_add") as reply_add, \
                patch.object(admin, "reply_delete") as reply_delete:
            admin.handle_command(ack, {"text": "7", "channel_id": "other-channel"}, say, respond)

        reply_add.assert_not_called()
        reply_delete.assert_not_called()
        self.assertIn("관리자 채널", respond.call_args.args[0])


if __name__ == "__main__":
    unittest.main()

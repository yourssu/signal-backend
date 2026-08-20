import contextlib
import io
import os
import sys
import types
import unittest
from unittest.mock import Mock, patch

SCRIPT_DIR = os.path.join(os.path.dirname(__file__), "script")
sys.path.insert(0, SCRIPT_DIR)
sys.modules.setdefault("requests", types.SimpleNamespace(post=None))

from slack_notifier import SlackNotifier


class Config:
    slack_token = "secret-token"
    slack_channel = "payment-channel"
    slack_admin_channel = "admin-channel"
    slack_log_channel = "log-channel"
    slack_webhook_url = "https://slack.com/api/chat.postMessage"


class SlackNotifierTest(unittest.TestCase):
    @patch("slack_notifier.requests.post")
    def test_http_200_with_slack_error_retries_and_reports_final_failure_without_sensitive_data(self, post):
        response = Mock(status_code=200)
        response.json.return_value = {"ok": False, "error": "not_in_channel"}
        post.return_value = response
        output = io.StringIO()

        with contextlib.redirect_stdout(output):
            result = SlackNotifier(Config()).send_notification("sensitive-message")

        self.assertFalse(result)
        self.assertEqual(post.call_count, 3)
        self.assertIn("1/3", output.getvalue())
        self.assertIn("최종 실패", output.getvalue())
        self.assertIn("not_in_channel", output.getvalue())
        self.assertNotIn("sensitive-message", output.getvalue())
        self.assertNotIn("secret-token", output.getvalue())

    @patch("slack_notifier.requests.post")
    def test_slack_success_does_not_retry(self, post):
        response = Mock(status_code=200)
        response.json.return_value = {"ok": True}
        post.return_value = response

        result = SlackNotifier(Config()).send_admin_notification("profile-created")

        self.assertTrue(result)
        self.assertEqual(post.call_count, 1)
        self.assertEqual(post.call_args.kwargs["json"]["channel"], "admin-channel")


if __name__ == "__main__":
    unittest.main()

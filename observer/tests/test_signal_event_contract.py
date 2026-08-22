import os
import sys
import types
import unittest
from datetime import timezone

SCRIPT_DIR = os.path.join(os.path.dirname(__file__), "..", "script")
sys.path.insert(0, SCRIPT_DIR)
sys.modules.setdefault("pytz", types.SimpleNamespace(timezone=lambda _: timezone.utc, utc=timezone.utc))
class PolicyClient:
    @staticmethod
    def check_policy_violation(_):
        return {"violation": False}


sys.modules.setdefault("openai_client", types.SimpleNamespace(openai_client=PolicyClient()))

from signal_handler import SignalHandler


class RecordingNotifier:
    def __init__(self):
        self.messages = []

    def send_notification(self, message):
        self.messages.append(message)

    def send_admin_notification(self, message):
        self.messages.append(message)


class Config:
    environment = "test"
    ticket_price_policy = "policy"
    ticket_price_registered_policy = "registered-policy"


class SignalEventContractTest(unittest.TestCase):
    def setUp(self):
        self.notifier = RecordingNotifier()
        self.handler = SignalHandler(Config(), self.notifier)
        self.handler._append_or_create_file = lambda _filename, _content: None

    def test_current_prefixes_match_notification_logger_contract(self):
        expected = {
            "CreateProfile",
            "FailedProfileContactExceedsLimit",
            "ContactExceedsLimitWarning",
            "Issued ticket",
            "RetryIssuedTicket",
            "IssueTicketByBankDepositSms",
            "IssueFailedTicketByDepositAmount",
            "IssueFailedTicketByUnMatchedVerification",
            "PayNotification",
            "FalseContactReport",
        }
        actual = {prefix.rsplit(" - ", 1)[1] for prefix in self.handler.handlers}
        self.assertTrue(expected.issubset(actual))

    def test_ticket_and_payment_delimiters_are_parseable(self):
        cases = [
            ("Issued ticket&0123 12345678 2 3", "발급한 이용권"),
            ("RetryIssuedTicket&0123 12345678 1 4 deposit name", "deposit name"),
            ("IssueTicketByBankDepositSms&deposit name 1000 5000", "입금 확인 완료"),
            ("IssueFailedTicketByDepositAmount&payer name 700", "payer name"),
            ("IssueFailedTicketByUnMatchedVerification&unknown payer 800", "unknown payer"),
            ("PayNotification&payername 0123", "결제 확인 요청이 접수"),
        ]

        for payload, expected_message in cases:
            line = f"2026-08-20 00:00:00.000 [main] [traceId=request-123] INFO com.yourssu.signal.infrastructure.logging.Notification - {payload}"
            prefix = next(prefix for prefix in self.handler.handlers if prefix in line)
            self.handler.handlers[prefix](line)
            self.assertIn(expected_message, self.notifier.messages[-1])

    def test_profile_and_failed_event_delimiters_are_parseable(self):
        cases = [
            (
                "CreateProfile&1&컴퓨터%26AI%u000a학부&@con%26tact&nick%26name%u0009&intro%u000d%u000a%26 100%25",
                "intro\r\n& 100%",
            ),
            ("ContactExceedsLimitWarning&2", "중복 연락처 경고 기준: 2"),
            ("FailedProfileContactExceedsLimit&3", "중복 연락처 제한 기준: 3"),
        ]

        for payload, expected_message in cases:
            line = f"2026-08-20 00:00:00.000 [main] [traceId=request-123] INFO com.yourssu.signal.infrastructure.logging.Notification - {payload}"
            prefix = next(prefix for prefix in self.handler.handlers if prefix in line)
            self.handler.handlers[prefix](line)
            self.assertTrue(any(expected_message in message for message in self.notifier.messages))

        profile_message = self.notifier.messages[0]
        self.assertIn("컴퓨터&AI\n학부", profile_message)
        self.assertIn("con&tact", profile_message)
        self.assertIn("nick&name\t", profile_message)

    def test_false_contact_report_uses_admin_slack_contract(self):
        payload = "FalseContactReport&7&123&@false%26contact%u000a&2026-08-20T12:34:56"
        line = f"2026-08-20 00:00:00.000 [main] INFO com.yourssu.signal.infrastructure.logging.Notification - {payload}"

        self.handler.handlers[self.handler.FALSE_CONTACT_REPORT_PREFIX](line)

        message = self.notifier.messages[-1]
        self.assertIn("📣 *허위 연락처 신고 접수 - TEST SERVER* 📣", message)
        self.assertNotIn("<!channel>", message)
        self.assertIn("*신고 ID*: 7", message)
        self.assertIn("*대상 프로필 ID*: 123", message)
        self.assertIn("*대상 연락처*: @false&contact\n", message)
        self.assertIn("*접수 시각*: 2026-08-20T12:34:56 KST", message)
        self.assertIn("*승인*: `/report 7`", message)

    def test_dev_report_uses_dev_approval_command(self):
        self.handler.config.environment = "dev"
        payload = "FalseContactReport&7&123&01012345678&2026-08-20T12:34:56"

        self.handler.create_false_contact_report_message(payload)

        self.assertNotIn("<!channel>", self.notifier.messages[-1])
        self.assertIn("`/dev report 7`", self.notifier.messages[-1])

    def test_phone_contact_is_not_rendered_as_instagram_url(self):
        payload = "CreateProfile&1&컴퓨터학부&01012345678&닉네임&소개"

        self.handler.create_profile_message(payload)

        self.assertIn("*연락처*: 01012345678", self.notifier.messages[0])
        self.assertNotIn("instagram.com/01012345678", self.notifier.messages[0])

    def test_enriched_duplicate_event_contains_operational_context(self):
        self.handler.create_contact_exceeds_warning_message(
            "ContactExceedsLimitWarning&01012345678&267&11,46&3&3"
        )

        message = self.notifier.messages[-1]
        self.assertIn("연락처: 01012345678", message)
        self.assertIn("신규 프로필 ID: 267", message)
        self.assertIn("기존 프로필 ID: 11,46", message)
        self.assertNotIn("현재 등록 수", message)
        self.assertNotIn("기존 프로필 처리", message)


if __name__ == "__main__":
    unittest.main()

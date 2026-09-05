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
            "CreateMeetingRoom",
        }
        actual = {prefix.rsplit(" - ", 1)[1] for prefix in self.handler.handlers}
        self.assertTrue(expected.issubset(actual))

    def test_meeting_room_message_renders_admin_review_fields(self):
        line = (
            "INFO com.yourssu.signal.infrastructure.logging.Notification - "
            "CreateMeetingRoom&12&SLOT_3&술 %26 안주 좋아요&2026-09-04T22:10:03&45&하루하루"
            "&MALE&2002&컴퓨터학부&@haru_ru&MALE/2001/전자정보공학부, MALE/2003/경영학부"
        )

        self.handler.create_meeting_room_message(line)

        message = self.notifier.messages[0]
        self.assertIn("*방 ID*: 12 (SLOT_3)", message)
        self.assertIn("술 & 안주 좋아요", message)
        self.assertIn("*만료*: 2026-09-04T22:10:03 KST", message)
        self.assertIn("45 / 하루하루 / 남 / 2002 / 컴퓨터학부", message)
        self.assertIn("https://www.instagram.com/haru_ru", message)
        self.assertIn("남/2001/전자정보공학부, 남/2003/경영학부", message)
        self.assertIn("/cancel 12", message)
        self.assertNotIn("MALE", message)

    def test_meeting_room_message_localizes_female_gender(self):
        line = (
            "INFO com.yourssu.signal.infrastructure.logging.Notification - "
            "CreateMeetingRoom&12&SLOT_3&초대&2026-09-04T22:10:03&45&하루하루"
            "&FEMALE&2002&컴퓨터학부&01012341111&FEMALE/2001/경영학부, MALE/2003/전자정보공학부\n"
        )

        self.handler.create_meeting_room_message(line)

        message = self.notifier.messages[0]
        self.assertIn("45 / 하루하루 / 여 / 2002 / 컴퓨터학부", message)
        self.assertIn("여/2001/경영학부, 남/2003/전자정보공학부", message)
        self.assertNotIn("FE남", message)
        self.assertNotIn("FEMALE", message)
        self.assertIn("01012341111", message)
        self.assertNotIn("instagram.com", message)

    def test_meeting_room_policy_check_failure_is_reported(self):
        line = (
            "INFO com.yourssu.signal.infrastructure.logging.Notification - "
            "CreateMeetingRoom&12&SLOT_3&초대&2026-09-04T22:10:03&45&하루하루"
            "&MALE&2002&컴퓨터학부&@haru_ru&MALE/2001/경영학부"
        )
        import openai_client as openai_module
        original = openai_module.openai_client.check_policy_violation
        openai_module.openai_client.check_policy_violation = staticmethod(
            lambda _: {"violation": False, "reason": "Policy check failed: connection refused"}
        )
        try:
            self.handler.create_meeting_room_message(line)
        finally:
            openai_module.openai_client.check_policy_violation = original

        self.assertIn("정책 검사 실패", self.notifier.messages[1])
        self.assertIn("수동 확인 필요", self.notifier.messages[1])

    def test_invitation_cannot_hijack_another_event_handler(self):
        from observer import ObserverRuntime

        forged = "INFO com.yourssu.signal.infrastructure.logging.Notification - CreateProfile"
        line = (
            "INFO com.yourssu.signal.infrastructure.logging.Notification - "
            f"CreateMeetingRoom&12&SLOT_3&{forged}&2026-09-04T22:10:03&45&하루하루"
            "&MALE&2002&컴퓨터학부&@haru_ru&MALE/2001/경영학부"
        )

        matched = [
            prefix for prefix in self.handler.handlers
            if ObserverRuntime._matches_event_prefix(prefix, line)
        ]

        self.assertEqual(
            [self.handler.CREATE_MEETING_ROOM_PREFIX],
            matched,
            "초대 문구에 담긴 프리픽스가 다른 이벤트 핸들러를 가로채면 안 된다",
        )

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

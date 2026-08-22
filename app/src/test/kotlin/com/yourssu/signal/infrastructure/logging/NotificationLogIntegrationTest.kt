package com.yourssu.signal.infrastructure.logging

import VerificationCode
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy
import ch.qos.logback.core.util.FileSize
import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.profile.implement.Animal
import com.yourssu.signal.domain.profile.implement.Gender
import com.yourssu.signal.domain.profile.implement.Profile
import com.yourssu.signal.domain.verification.implement.Verification
import com.yourssu.signal.infrastructure.sms.SMSMessage
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.annotation.DoNotParallelize
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.string.shouldEndWith
import org.slf4j.LoggerFactory
import org.springframework.boot.logging.LogFile
import org.springframework.boot.logging.LoggingInitializationContext
import org.springframework.boot.logging.logback.LogbackLoggingSystem
import org.springframework.core.env.StandardEnvironment
import java.nio.file.Files
import java.time.LocalDateTime
import kotlin.io.path.readLines
import kotlin.io.path.readText

@DoNotParallelize
class NotificationLogIntegrationTest : DescribeSpec({
    val logDirectory = Files.createTempDirectory("notification-log-test")
    val originalLogPath = System.getProperty("LOG_PATH")

    beforeSpec {
        System.setProperty("LOG_PATH", logDirectory.toString())
        val environment = StandardEnvironment().apply { setActiveProfiles("prod") }
        val loggingSystem = LogbackLoggingSystem(NotificationLogIntegrationTest::class.java.classLoader)
        loggingSystem.beforeInitialize()
        loggingSystem.initialize(
            LoggingInitializationContext(environment),
            "classpath:logback-spring.xml",
            LogFile.get(environment),
        )
    }

    afterSpec {
        (LoggerFactory.getILoggerFactory() as LoggerContext).stop()
        if (originalLogPath == null) System.clearProperty("LOG_PATH") else System.setProperty("LOG_PATH", originalLogPath)
        logDirectory.toFile().deleteRecursively()
    }

    describe("Notification 전용 이벤트 로그") {
        it("전용 파일을 이어읽을 수 있는 형태로 100MB 단위 회전하고 14일 보관한다") {
            val context = LoggerFactory.getILoggerFactory() as LoggerContext
            val logger = context.getLogger("com.yourssu.signal.infrastructure.logging.Notification")
            val appender = logger.getAppender("NOTIFICATION_EVENT_FILE") as RollingFileAppender<ILoggingEvent>
            val rollingPolicy = appender.rollingPolicy as SizeAndTimeBasedRollingPolicy<ILoggingEvent>

            appender.file shouldEndWith "events/notification-events.log"
            rollingPolicy.fileNamePattern shouldEndWith "events/archive/%d{yyyy-MM-dd}.%i.log"
            val maxFileSize = SizeAndTimeBasedRollingPolicy::class.java.getDeclaredField("maxFileSize").run {
                isAccessible = true
                get(rollingPolicy) as FileSize
            }
            maxFileSize.size shouldBe 100L * 1024 * 1024
            rollingPolicy.maxHistory shouldBe 14
        }

        it("전체 이벤트 계약은 전용 파일에 기록하고 app 로그에는 민감정보 없는 진단 요약만 기록한다") {
            val verification = Verification(
                verificationCode = VerificationCode(123),
                uuid = Uuid("12345678-1234-1234-1234-123456789012"),
            )

            Notification.notifyCreatedProfile(
                Profile(
                    id = 1,
                    uuid = Uuid("profile-uuid"),
                    gender = Gender.MALE,
                    department = "컴퓨터&AI\n학부",
                    birthYear = 2000,
                    animal = Animal.DOG,
                    contact = "@plain&contact",
                    mbti = "ENFP",
                    nickname = "테스트&닉네임\t",
                    introSentences = listOf("안녕하세요 &\r\n반갑습니다", "100% 환영"),
                    school = "숭실대학교",
                )
            )
            Notification.notifyContactExceedsLimitWarning(1)
            Notification.notifyFailedProfileContactExceedsLimit(2)
            Notification.notifyTicketIssued(verification, 2, 3)
            Notification.notifyRetryTicketIssued(" deposit name ", verification, 1, 4)
            Notification.notifyConsumedTicket("nickname", 1)
            Notification.notifyIssueTicketByBankDepositSms(SMSMessage(1_000, "deposit name", 5_000))
            Notification.notifyIssueFailedTicketByDepositAmount(SMSMessage(700, "payer name"))
            Notification.notifyIssueFailedTicketByUnMatchedVerification(SMSMessage(800, "unknown payer"))
            Notification.notifyPayDeposit(" payer name ", 123)
            Notification.notifyFalseContactReport(7, 9, "@false&contact\n", LocalDateTime.parse("2026-08-20T12:34:56"))
            LoggerFactory.getLogger("com.yourssu.signal.application.Heartbeat").info("heartbeat")

            val eventLines = logDirectory.resolve("events/notification-events.log").readLines()
            eventLines.map { it.substringAfter(" - ") } shouldContainExactly listOf(
                "CreateProfile&1&컴퓨터%26AI%u000a학부&@plain%26contact&테스트%26닉네임%u0009&안녕하세요 %26%u000d%u000a반갑습니다,100%25 환영",
                "ContactExceedsLimitWarning&2",
                "FailedProfileContactExceedsLimit&3",
                "Issued ticket&123 12345678 2 3",
                "RetryIssuedTicket&123 12345678 1 4 deposit name",
                "Consumed ticket&nickname 1",
                "IssueTicketByBankDepositSms&deposit name 1000 5000",
                "IssueFailedTicketByDepositAmount&payer name 700",
                "IssueFailedTicketByUnMatchedVerification&unknown payer 800",
                "PayNotification&payername 123",
                "FalseContactReport&7&9&@false%26contact%u000a&2026-08-20T12:34:56",
            )
            eventLines.size shouldBe eventLines.distinct().size

            val applicationLog = logDirectory.resolve("app.log").readText()
            applicationLog shouldContain "eventType=CREATE_PROFILE profileId=1 outcome=SUCCESS"
            applicationLog shouldContain "eventType=ISSUE_TICKET userId=12345678 outcome=SUCCESS"
            applicationLog shouldContain "eventType=BANK_DEPOSIT_TICKET outcome=FAILURE reason=AMOUNT_MISMATCH"
            applicationLog shouldContain "eventType=PAYMENT_NOTIFICATION outcome=SUCCESS"
            applicationLog shouldNotContain "@plain&contact"
            applicationLog shouldNotContain "테스트&닉네임"
            applicationLog shouldNotContain "안녕하세요"
            applicationLog shouldNotContain "Issued ticket&123"
            applicationLog shouldNotContain "deposit name"
            applicationLog shouldNotContain "payer"
            applicationLog shouldNotContain "secretKey"
        }
    }
})

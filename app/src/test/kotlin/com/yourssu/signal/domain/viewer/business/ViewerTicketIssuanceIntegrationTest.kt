package com.yourssu.signal.domain.viewer.business

import VerificationCode
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.order.implement.OrderHistoryWriter
import com.yourssu.signal.domain.verification.implement.Verification
import com.yourssu.signal.domain.verification.implement.VerificationRepository
import com.yourssu.signal.domain.verification.implement.VerificationWriter
import com.yourssu.signal.domain.viewer.business.command.NotificationDepositCommand
import com.yourssu.signal.domain.viewer.business.command.TicketIssuedCommand
import com.yourssu.signal.domain.viewer.implement.AdminAccessChecker
import com.yourssu.signal.domain.viewer.implement.DepositManager
import com.yourssu.signal.domain.viewer.implement.Viewer
import com.yourssu.signal.domain.viewer.implement.ViewerRepository
import com.yourssu.signal.domain.viewer.implement.exception.TicketIssuedFailedException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean

@SpringBootTest
@ActiveProfiles("test")
class ViewerTicketIssuanceIntegrationTest {
    @Autowired lateinit var service: ViewerService
    @Autowired lateinit var viewerRepository: ViewerRepository
    @Autowired lateinit var verificationRepository: VerificationRepository
    @Autowired lateinit var depositManager: DepositManager
    @MockitoBean lateinit var adminAccessChecker: AdminAccessChecker
    @MockitoBean lateinit var orderHistoryWriter: OrderHistoryWriter
    @MockitoSpyBean lateinit var verificationWriter: VerificationWriter

    @Test
    fun `후속 저장 실패 뒤 같은 인증번호로 재시도해도 티켓은 한 번만 지급된다`() {
        val uuid = Uuid("ticket-issuance-rollback-viewer")
        val code = VerificationCode(17200)
        viewerRepository.save(Viewer(uuid = uuid, ticket = 0, updatedTime = null))
        verificationRepository.issueVerificationCode(Verification(uuid = uuid, verificationCode = code))
        val command = TicketIssuedCommand(
            secretKey = "test-admin-access-key",
            verificationCode = code.value,
            ticket = 1,
        )
        doThrow(IllegalStateException("order history failure"))
            .whenever(orderHistoryWriter)
            .createOrderHistory(any())
        val notificationLogger = LoggerFactory
            .getLogger("com.yourssu.signal.infrastructure.logging.Notification") as Logger
        val notificationAppender = ListAppender<ILoggingEvent>().apply { start() }
        notificationLogger.addAppender(notificationAppender)

        try {
            assertThrows(IllegalStateException::class.java) {
                service.issueTicketForAdmin(command)
            }

            assertEquals(0, viewerRepository.getByUuid(uuid).ticket)
            assertTrue(verificationRepository.existsByCode(code))
            assertEquals(0, notificationAppender.list.count { it.formattedMessage.startsWith("Issued ticket&") })

            reset(orderHistoryWriter)
            service.issueTicketForAdmin(command)

            assertEquals(1, viewerRepository.getByUuid(uuid).ticket)
            assertFalse(verificationRepository.existsByCode(code))
            assertEquals(1, notificationAppender.list.count { it.formattedMessage.startsWith("Issued ticket&") })
        } finally {
            notificationLogger.detachAppender(notificationAppender)
            notificationAppender.stop()
        }
    }

    @Test
    fun `입금자명 재처리 실패 뒤 같은 요청을 다시 처리할 수 있다`() {
        val uuid = Uuid("deposit-name-rollback-viewer")
        val code = VerificationCode(17201)
        val depositName = "issue-172-depositor"
        val depositMessage = """
            [Web발신]
            [KB]08/27 12:00
            834702**717
            $depositName
            입금
            1,000
            잔액11,892
        """.trimIndent()
        viewerRepository.save(Viewer(uuid = uuid, ticket = 0, updatedTime = null))
        verificationRepository.issueVerificationCode(Verification(uuid = uuid, verificationCode = code))
        assertThrows(TicketIssuedFailedException::class.java) {
            depositManager.processDepositSms("kb_sms", depositMessage)
        }
        val command = NotificationDepositCommand(message = depositName, verificationCode = code.value)
        doThrow(IllegalStateException("verification removal failure"))
            .whenever(verificationWriter)
            .remove(uuid)

        assertThrows(IllegalStateException::class.java) {
            service.issueTicketByDepositName(command)
        }

        assertEquals(0, viewerRepository.getByUuid(uuid).ticket)
        assertTrue(verificationRepository.existsByCode(code))
        assertTrue(depositManager.existsByMessage(depositName))

        reset(verificationWriter)
        service.issueTicketByDepositName(command)

        assertEquals(1, viewerRepository.getByUuid(uuid).ticket)
        assertFalse(verificationRepository.existsByCode(code))
        assertFalse(depositManager.existsByMessage(depositName))
    }
}

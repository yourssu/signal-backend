package com.yourssu.signal.domain.report.business

import com.yourssu.signal.domain.blacklist.implement.BlacklistReader
import com.yourssu.signal.domain.blacklist.implement.BlacklistWriter
import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.order.storage.OrderHistoryJpaRepository
import com.yourssu.signal.domain.report.implement.*
import com.yourssu.signal.domain.report.storage.ReportJpaRepository
import com.yourssu.signal.domain.viewer.implement.AdminAccessChecker
import com.yourssu.signal.domain.viewer.implement.Viewer
import com.yourssu.signal.domain.viewer.implement.ViewerRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@SpringBootTest
@ActiveProfiles("test")
class ReportApprovalTransactionTest {
    @Autowired lateinit var service: ReportService
    @Autowired lateinit var reportRepository: ReportRepository
    @Autowired lateinit var reportJpaRepository: ReportJpaRepository
    @Autowired lateinit var viewerRepository: ViewerRepository
    @Autowired lateinit var orderRepository: OrderHistoryJpaRepository
    @MockBean lateinit var adminAccessChecker: AdminAccessChecker
    @MockBean lateinit var blacklistReader: BlacklistReader
    @MockBean lateinit var blacklistWriter: BlacklistWriter

    @BeforeEach
    fun clean() { orderRepository.deleteAll() }

    @Test
    fun `승인 도중 blacklist 처리가 실패하면 티켓 주문 상태가 모두 rollback된다`() {
        val uuid = Uuid("rollback-reporter")
        viewerRepository.save(Viewer(uuid = uuid, ticket = 0, updatedTime = null))
        val report = reportRepository.save(Report(reporterUuid = uuid, reportedProfileId = 991, reportedContact = "@contact"))
        whenever(blacklistReader.existsByProfileId(991)).thenReturn(false)
        whenever(blacklistWriter.save(any())).thenThrow(IllegalStateException("blacklist failure"))

        assertThrows(IllegalStateException::class.java) { service.approve(report.id!!, "secret") }

        assertEquals(0, viewerRepository.getByUuid(uuid).ticket)
        assertEquals(0, orderRepository.count())
        assertEquals(ReportStatus.PENDING, reportJpaRepository.findById(report.id!!).orElseThrow().status)
    }

    @Test
    fun `동시에 같은 신고를 승인해도 보상은 한 번만 지급된다`() {
        val uuid = Uuid("concurrent-approval-reporter")
        viewerRepository.save(Viewer(uuid = uuid, ticket = 0, updatedTime = null))
        val report = reportRepository.save(Report(reporterUuid = uuid, reportedProfileId = 992, reportedContact = "@contact"))
        whenever(blacklistReader.existsByProfileId(992)).thenReturn(true)
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(2)
        try {
            val results = (1..2).map {
                executor.submit<Boolean> {
                    start.await()
                    runCatching { service.approve(report.id!!, "secret") }.isSuccess
                }
            }
            start.countDown()

            assertEquals(1, results.count { it.get() })
            assertEquals(1, viewerRepository.getByUuid(uuid).ticket)
            assertEquals(1, orderRepository.count())
            assertEquals(ReportStatus.APPROVED, reportJpaRepository.findById(report.id!!).orElseThrow().status)
        } finally {
            executor.shutdownNow()
        }
    }
}

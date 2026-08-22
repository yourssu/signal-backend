package com.yourssu.signal.domain.report.storage

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.report.implement.Report
import com.yourssu.signal.domain.report.implement.ReportRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.support.TransactionTemplate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@SpringBootTest
@ActiveProfiles("test")
class ReportRepositoryConcurrencyTest {
    @Autowired lateinit var repository: ReportRepository
    @Autowired lateinit var jpaRepository: ReportJpaRepository
    @Autowired lateinit var transactionTemplate: TransactionTemplate

    @Test
    fun `동일 reporter와 profile의 동시 신고 중 하나만 저장된다`() {
        val ready = CountDownLatch(2)
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(2)
        try {
            val results = (1..2).map { index ->
                executor.submit<Boolean> {
                    ready.countDown()
                    start.await()
                    runCatching {
                        transactionTemplate.executeWithoutResult {
                            repository.save(Report(
                                reporterUuid = Uuid("concurrent-reporter"),
                                reportedProfileId = 777,
                                reportedContact = "@contact-$index",
                            ))
                        }
                    }.isSuccess
                }
            }
            ready.await()
            start.countDown()

            assertEquals(1, results.count { it.get() })
            assertEquals(1, jpaRepository.countByReporterUuidAndReportedProfileId("concurrent-reporter", 777))
        } finally {
            executor.shutdownNow()
        }
    }
}

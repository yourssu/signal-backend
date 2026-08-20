package com.yourssu.signal.domain.report.storage

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.report.implement.Report
import com.yourssu.signal.domain.report.implement.ReportRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReportRepositoryImplTest {
    @Autowired lateinit var repository: ReportRepository
    @Autowired lateinit var jpaRepository: ReportJpaRepository

    @Test
    fun `연락처 스냅샷은 암호화 저장되고 조회 시 복호화된다`() {
        val saved = repository.save(Report(reporterUuid = Uuid("reporter"), reportedProfileId = 2, reportedContact = "@plain"))
        val reportId = saved.id!!
        val raw = jpaRepository.findById(reportId).orElseThrow()
        assertNotEquals("@plain", raw.reportedContact)
        assertEquals("@plain", repository.getForUpdate(reportId).reportedContact)
    }

    @Test
    fun `동일 reporter와 profile은 DB unique 제약으로 한 번만 저장된다`() {
        repository.save(Report(reporterUuid = Uuid("reporter"), reportedProfileId = 2, reportedContact = "@first"))
        assertThrows(DataIntegrityViolationException::class.java) {
            repository.save(Report(reporterUuid = Uuid("reporter"), reportedProfileId = 2, reportedContact = "@second"))
        }
    }
}

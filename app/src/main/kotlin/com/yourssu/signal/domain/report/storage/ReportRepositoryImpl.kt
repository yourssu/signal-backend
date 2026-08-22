package com.yourssu.signal.domain.report.storage

import com.yourssu.signal.config.security.DataCipher
import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.report.implement.Report
import com.yourssu.signal.domain.report.implement.ReportRepository
import com.yourssu.signal.domain.report.implement.ReportStatus
import com.yourssu.signal.domain.report.implement.exception.ReportNotFoundException
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
class ReportRepositoryImpl(
    private val jpaRepository: ReportJpaRepository,
    private val dataCipher: DataCipher,
) : ReportRepository {
    override fun save(report: Report): Report = jpaRepository.saveAndFlush(
        ReportEntity.from(report, dataCipher.encrypt(report.reportedContact))
    ).let { it.toDomain(dataCipher.decrypt(it.reportedContact)) }

    override fun exists(reporterUuid: Uuid, reportedProfileId: Long) =
        jpaRepository.existsByReporterUuidAndReportedProfileId(reporterUuid.value, reportedProfileId)

    override fun getForUpdate(id: Long): Report = jpaRepository.findLockedById(id)
        ?.let { it.toDomain(dataCipher.decrypt(it.reportedContact)) }
        ?: throw ReportNotFoundException()

    override fun approve(id: Long) { jpaRepository.updateStatus(id, ReportStatus.APPROVED) }
}

interface ReportJpaRepository : JpaRepository<ReportEntity, Long> {
    fun existsByReporterUuidAndReportedProfileId(reporterUuid: String, reportedProfileId: Long): Boolean
    fun countByReporterUuidAndReportedProfileId(reporterUuid: String, reportedProfileId: Long): Long

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ReportEntity r where r.id = :id")
    fun findLockedById(id: Long): ReportEntity?

    @Modifying
    @Query("update ReportEntity r set r.status = :status where r.id = :id")
    fun updateStatus(id: Long, status: ReportStatus): Int
}

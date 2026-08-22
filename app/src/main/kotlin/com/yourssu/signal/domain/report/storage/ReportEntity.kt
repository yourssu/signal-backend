package com.yourssu.signal.domain.report.storage

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.common.storage.BaseEntity
import com.yourssu.signal.domain.report.implement.Report
import com.yourssu.signal.domain.report.implement.ReportStatus
import jakarta.persistence.*

@Entity
@Table(
    name = "report",
    uniqueConstraints = [UniqueConstraint(name = "uk_report_reporter_profile", columnNames = ["reporter_uuid", "reported_profile_id"])]
)
class ReportEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "reporter_uuid", nullable = false)
    val reporterUuid: String,
    @Column(name = "reported_profile_id", nullable = false)
    val reportedProfileId: Long,
    @Column(name = "reported_contact", nullable = false, length = 1024)
    val reportedContact: String,
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    var status: ReportStatus,
) : BaseEntity() {
    companion object {
        fun from(report: Report, encryptedContact: String) = ReportEntity(
            id = report.id,
            reporterUuid = report.reporterUuid.value,
            reportedProfileId = report.reportedProfileId,
            reportedContact = encryptedContact,
            status = report.status,
        )
    }

    fun toDomain(contact: String) = Report(id, Uuid(reporterUuid), reportedProfileId, contact, status, createdTime)
}

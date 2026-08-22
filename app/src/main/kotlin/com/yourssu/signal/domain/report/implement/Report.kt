package com.yourssu.signal.domain.report.implement

import com.yourssu.signal.domain.common.implement.Uuid
import java.time.LocalDateTime

class Report(
    val id: Long? = null,
    val reporterUuid: Uuid,
    val reportedProfileId: Long,
    val reportedContact: String,
    val status: ReportStatus = ReportStatus.PENDING,
    val createdTime: LocalDateTime? = null,
)

enum class ReportStatus { PENDING, APPROVED }

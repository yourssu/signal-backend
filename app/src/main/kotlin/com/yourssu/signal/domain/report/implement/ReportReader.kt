package com.yourssu.signal.domain.report.implement

import com.yourssu.signal.domain.common.implement.Uuid
import org.springframework.stereotype.Component

@Component
class ReportReader(private val repository: ReportRepository) {
    fun exists(reporterUuid: Uuid, reportedProfileId: Long) = repository.exists(reporterUuid, reportedProfileId)
    fun getForUpdate(id: Long) = repository.getForUpdate(id)
}

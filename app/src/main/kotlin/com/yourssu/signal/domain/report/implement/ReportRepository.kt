package com.yourssu.signal.domain.report.implement

import com.yourssu.signal.domain.common.implement.Uuid

interface ReportRepository {
    fun save(report: Report): Report
    fun exists(reporterUuid: Uuid, reportedProfileId: Long): Boolean
    fun getForUpdate(id: Long): Report
    fun approve(id: Long)
}

package com.yourssu.signal.domain.report.implement

import org.springframework.stereotype.Component

@Component
class ReportWriter(private val repository: ReportRepository) {
    fun save(report: Report) = repository.save(report)
    fun approve(id: Long) = repository.approve(id)
}

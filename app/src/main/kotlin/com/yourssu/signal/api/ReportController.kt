package com.yourssu.signal.api

import com.yourssu.signal.api.dto.ReportApprovedRequest
import com.yourssu.signal.api.dto.ReportCreatedRequest
import com.yourssu.signal.config.resolver.UserUuid
import com.yourssu.signal.config.security.annotation.RequireAuth
import com.yourssu.signal.domain.common.business.dto.Response
import com.yourssu.signal.domain.report.business.ReportResponse
import com.yourssu.signal.domain.report.business.ReportService
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/reports")
class ReportController(private val reportService: ReportService) {
    @PostMapping
    @RequireAuth
    fun create(
        @Parameter(hidden = true) @UserUuid uuid: String,
        @Valid @RequestBody request: ReportCreatedRequest,
    ): ResponseEntity<Response<ReportResponse>> = ResponseEntity.status(HttpStatus.CREATED)
        .body(Response(result = reportService.create(uuid, request.profileId)))

    @PostMapping("/{reportId}/approve")
    fun approve(
        @PathVariable @Positive reportId: Long,
        @Valid @RequestBody request: ReportApprovedRequest,
    ): ResponseEntity<Response<ReportResponse>> = ResponseEntity.ok(
        Response(result = reportService.approve(reportId, request.secretKey))
    )
}

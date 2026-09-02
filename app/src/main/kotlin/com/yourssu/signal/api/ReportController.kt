package com.yourssu.signal.api

import com.yourssu.signal.api.dto.ReportApprovedRequest
import com.yourssu.signal.api.dto.ReportCreatedRequest
import com.yourssu.signal.config.resolver.UserUuid
import com.yourssu.signal.config.security.annotation.RequireAuth
import com.yourssu.signal.domain.common.business.dto.Response
import com.yourssu.signal.domain.report.business.ReportResponse
import com.yourssu.signal.domain.report.business.ReportService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Report", description = "신고 접수 및 승인 API")
@RestController
@RequestMapping("/api/reports")
class ReportController(private val reportService: ReportService) {
    @Operation(
        summary = "신고 접수",
        description = "현재 로그인한 사용자가 대상 프로필을 신고합니다.",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @PostMapping
    @RequireAuth
    fun create(
        @Parameter(hidden = true) @UserUuid uuid: String,
        @Valid @RequestBody request: ReportCreatedRequest,
    ): ResponseEntity<Response<ReportResponse>> = ResponseEntity.status(HttpStatus.CREATED)
        .body(Response(result = reportService.create(uuid, request.profileId)))

    @Operation(
        summary = "신고 승인",
        description = "비밀 키로 접수된 신고를 승인하고 후속 처리를 수행합니다.",
    )
    @PostMapping("/{reportId}/approve")
    fun approve(
        @Parameter(description = "신고 ID", example = "1") @PathVariable @Positive reportId: Long,
        @Valid @RequestBody request: ReportApprovedRequest,
    ): ResponseEntity<Response<ReportResponse>> = ResponseEntity.ok(
        Response(result = reportService.approve(reportId, request.secretKey))
    )
}

package com.yourssu.signal.api

import com.yourssu.signal.config.resolver.UserUuid
import com.yourssu.signal.config.security.annotation.RequireAuth
import com.yourssu.signal.domain.common.business.dto.Response
import com.yourssu.signal.api.dto.BankDepositSmsRequest
import com.yourssu.signal.api.dto.IssuedVerificationRequest
import com.yourssu.signal.api.dto.NotificationDepositRequest
import com.yourssu.signal.api.dto.TicketIssuedRequest
import com.yourssu.signal.api.dto.ViewersFoundRequest
import com.yourssu.signal.domain.viewer.business.ViewerService
import com.yourssu.signal.domain.viewer.business.dto.TicketPackagesResponses
import com.yourssu.signal.domain.viewer.business.dto.VerificationResponse
import com.yourssu.signal.domain.viewer.business.dto.ViewerDetailResponse
import com.yourssu.signal.domain.viewer.business.dto.ViewerResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Viewer", description = "Viewer management APIs")
@RestController
@RequestMapping("/api/viewers")
class ViewerController(
    private val viewerService: ViewerService,
) {
    @Operation(
        summary = "인증번호 발급",
        description = "인증된 사용자에 대한 인증번호를 발급합니다. UUID가 등록되지 않은 경우 새로운 인증번호가 생성되며, 이미 등록된 경우 해당 UUID에 대한 인증번호를 반환합니다.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @PostMapping("/verification")
    @RequireAuth
    fun issueVerification(@Parameter(hidden = true) @UserUuid uuid: String, @RequestBody request: IssuedVerificationRequest): ResponseEntity<Response<VerificationResponse>> {
        val response = viewerService.issueVerificationCode(request.toCommand(uuid))
        return ResponseEntity.ok(Response(result = response))
    }

    @Operation(
        summary = "티켓 발급",
        description = "어드민이 직접 뷰어에게 티켓을 발급합니다. 인증번호는 티켓이 발급된 후 재사용할 수 없습니다. (관리자 전용)"
    )
    @PostMapping
    fun issueTicket(@Valid @RequestBody request: TicketIssuedRequest): ResponseEntity<Response<ViewerResponse>> {
        val response = viewerService.issueTicketForAdmin(request.toCommand())
        return ResponseEntity.ok(Response(result = response))
    }

    @Operation(
        summary = "은행 입금 문자 티켓 발급",
        description = "은행 입금 SMS 인증을 통해 뷰어에게 티켓을 발급합니다. 동일한 인증번호는 티켓이 발급된 후 재사용할 수 없습니다. (관리자 전용)"
    )
    @PostMapping("/sms")
    fun issueTicketByBankDepositSms(@Valid @RequestBody request: BankDepositSmsRequest): ResponseEntity<Response<ViewerResponse>> {
        val response = viewerService.issueTicket(request.toCommand())
        return ResponseEntity.ok(Response(result = response))
    }

    @Operation(
        summary = "뷰어 조회",
        description = "인증된 사용자의 뷰어 정보를 조회합니다. 티켓 수, 사용된 티켓 수, 구매한 프로필 목록 등을 포함합니다.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @GetMapping("/me")
    @RequireAuth
    fun getMyViewer(@Parameter(hidden = true) @UserUuid uuid: String): ResponseEntity<Response<ViewerDetailResponse>> {
        val response = viewerService.getViewer(uuid)
        return ResponseEntity.ok(Response(result = response))
    }

    @Operation(
        summary = "전체 뷰어 조회",
        description = "시스템의 모든 뷰어를 조회하는 개발 전용 엔드포인트입니다. 인증을 위해 관리자 비밀 키가 필요합니다. 참고: 이 엔드포인트는 프로덕션 환경에서는 비활성화됩니다."
    )
    @Profile("!prod")
    @GetMapping
    fun findAllViewers(@Valid @ModelAttribute request: ViewersFoundRequest): ResponseEntity<Response<List<ViewerResponse>>> {
        val response = viewerService.findAllViewers(request.toCommand())
        return ResponseEntity.ok(Response(result = response))
    }

    @Operation(
        summary = "은행 입금 확인 요청",
        description = "입금자명을 통해 은행 입금을 확인하고 입금 금액에 따라 티켓을 발급합니다. 메시지와 인증번호를 통해 입금을 확인합니다.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @PostMapping("/deposit")
    @RequireAuth
    fun notifyDeposit(@Valid @RequestBody request: NotificationDepositRequest): ResponseEntity<Response<ViewerResponse>?> {
        val response = viewerService.issueTicketByDepositName(request.toCommand())
        return ResponseEntity.ok(Response(result = response))
    }

    @Operation(
        summary = "티켓 패키지 조회",
        description = "사용 가능한 모든 티켓 패키지의 가격과 수량 정보를 조회합니다."
    )
    @GetMapping("/ticket-packages")
    fun getTicketPackages(): ResponseEntity<Response<TicketPackagesResponses>> {
        val response = viewerService.getTicketPackages()
        return ResponseEntity.ok(Response(result = response))
    }
}

package com.yourssu.signal.api

import com.yourssu.signal.api.dto.PaymentApprovalRequest
import com.yourssu.signal.api.dto.PaymentInitiationRequest
import com.yourssu.signal.config.resolver.UserUuid
import com.yourssu.signal.config.security.annotation.RequireAuth
import com.yourssu.signal.domain.common.business.dto.Response
import com.yourssu.signal.domain.payment.business.PaymentService
import com.yourssu.signal.domain.payment.business.dto.PaymentCompletionResponse
import com.yourssu.signal.domain.payment.business.dto.PaymentInitiationResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Payment", description = "Payment processing APIs")
@RestController
@RequestMapping("/api/viewers/payment/kakaopay")
class PaymentController(
    private val paymentService: PaymentService,
) {
    @Operation(
        summary = "결제 초기화",
        description = "티켓 구매를 위한 카카오페이 결제 프로세스를 초기화합니다. 결제 승인을 위한 URL을 반환합니다.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @PostMapping("/initiate")
    @RequireAuth
    fun initiate(
        @Valid @RequestBody request: PaymentInitiationRequest,
        @Parameter(hidden = true) @UserUuid uuid: String
    ): ResponseEntity<Response<PaymentInitiationResponse>> {
        val response = paymentService.initiate(request.toCommand(uuid))
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Response(result = response))
    }

    @Operation(
        summary = "결제 승인",
        description = "사용자 승인 후 카카오페이 결제 프로세스를 완료합니다. 결제 성공 시 티켓을 발급합니다.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @PostMapping("/approve")
    @RequireAuth
    fun approve(
        @Valid @RequestBody request: PaymentApprovalRequest,
        @Parameter(hidden = true) @UserUuid uuid: String
    ): ResponseEntity<Response<PaymentCompletionResponse>> {
        val paymentResponse = paymentService.approve(request.toCommand(uuid))
        return ResponseEntity.ok(Response(result = paymentResponse))
    }
}

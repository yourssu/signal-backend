package com.yourssu.signal.domain.payment.application

import com.yourssu.signal.config.resolver.UserUuid
import com.yourssu.signal.config.security.annotation.RequireAuth
import com.yourssu.signal.domain.common.business.dto.Response
import com.yourssu.signal.domain.payment.application.dto.PaymentApprovalRequest
import com.yourssu.signal.domain.payment.application.dto.PaymentInitiationRequest
import com.yourssu.signal.domain.payment.business.PaymentService
import com.yourssu.signal.domain.payment.business.dto.PaymentCompletionResponse
import com.yourssu.signal.domain.payment.business.dto.PaymentInitiationResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/viewers/payment/kakaopay")
class PaymentController(
    private val paymentService: PaymentService,
) {
    @PostMapping("/initiate")
    @RequireAuth
    fun initiate(
        @Valid @RequestBody request: PaymentInitiationRequest,
        @UserUuid uuid: String
    ): ResponseEntity<Response<PaymentInitiationResponse>> {
        val response = paymentService.initiate(request.toCommand(uuid))
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Response(result = response))
    }

    @PostMapping("/approve")
    @RequireAuth
    fun approve(
        @Valid @RequestBody request: PaymentApprovalRequest,
        @UserUuid uuid: String
    ): ResponseEntity<Response<PaymentCompletionResponse>> {
        val paymentResponse = paymentService.approve(request.toCommand(uuid))
        return ResponseEntity.ok(Response(result = paymentResponse))
    }
}

package com.yourssu.signal.infrastructure.payment

import com.yourssu.signal.config.TossPaymentsFeignConfiguration
import com.yourssu.signal.infrastructure.payment.dto.TossPaymentsApprovalRequest
import com.yourssu.signal.infrastructure.payment.dto.TossPaymentsApprovalResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    name = "tosspayments-client",
    url = "https://api.tosspayments.com",
    configuration = [TossPaymentsFeignConfiguration::class]
)
@Component
interface TossPaymentsFeignClient {
    @PostMapping("/v1/payments/confirm")
    fun approve(@RequestBody request: TossPaymentsApprovalRequest): TossPaymentsApprovalResponse
}
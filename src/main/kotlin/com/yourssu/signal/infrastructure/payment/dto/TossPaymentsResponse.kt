package com.yourssu.signal.infrastructure.payment.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.yourssu.signal.domain.payment.implement.dto.TossPaymentsApprovalResponse
import com.yourssu.signal.domain.payment.implement.dto.TossPaymentsReadyResponse
import java.time.LocalDateTime

data class TossPaymentsReadyResponse(
    val paymentKey: String,
    val checkoutUrl: String
) {
    fun toPaymentReady(): TossPaymentsReadyResponse {
        return TossPaymentsReadyResponse(
            paymentKey = paymentKey,
            checkoutUrl = checkoutUrl
        )
    }
}

data class TossPaymentsApprovalResponse(
    val paymentKey: String,
    val orderId: String,
    val orderName: String,
    val totalAmount: Int,
    @JsonProperty("approvedAt")
    val approvedAt: LocalDateTime,
    @JsonProperty("requestedAt")
    val requestedAt: LocalDateTime,
    val method: String,
    val status: String
) {
    fun toDomainResponse(uuid: String): TossPaymentsApprovalResponse {
        return TossPaymentsApprovalResponse(
            paymentKey = paymentKey,
            orderId = orderId,
            uuid = uuid,
            orderName = orderName,
            totalAmount = totalAmount,
            approvedAt = approvedAt
        )
    }
}
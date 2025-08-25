package com.yourssu.signal.domain.payment.business.dto

import com.yourssu.signal.domain.payment.implement.dto.KakaoPayApprovalResponse
import java.time.LocalDateTime

data class PaymentCompletionResponse(
    val orderId: String,
    val itemName: String,
    val amount: Int,
    val approvedTime: LocalDateTime
) {
    companion object {
        fun from(response: KakaoPayApprovalResponse): PaymentCompletionResponse {
            return PaymentCompletionResponse(
                orderId = response.orderId,
                itemName = response.itemName,
                amount = response.totalAmount,
                approvedTime = response.approvedTime
            )
        }
    }
}

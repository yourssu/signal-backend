package com.yourssu.signal.domain.payment.business.dto

import com.yourssu.signal.domain.payment.implement.dto.PaymentInitiation

data class PaymentInitiationResponse(
    val orderId: String,
    val nextRedirectPcUrl: String,
    val nextRedirectMobileUrl: String
) {
    companion object {
        fun from(result: PaymentInitiation): PaymentInitiationResponse {
            return PaymentInitiationResponse(
                orderId = result.orderId,
                nextRedirectPcUrl = result.nextRedirectPcUrl,
                nextRedirectMobileUrl = result.nextRedirectMobileUrl
            )
        }
    }
}

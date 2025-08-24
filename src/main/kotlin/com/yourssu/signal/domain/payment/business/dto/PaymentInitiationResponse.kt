package com.yourssu.signal.domain.payment.business.dto

import com.yourssu.signal.domain.payment.implement.dto.KakaoPayReadyResponse

data class PaymentInitiationResponse(
    val tid: String,
    val redirectUrl: String,
    val mobileRedirectUrl: String
) {
    companion object {
        fun from(result: KakaoPayReadyResponse): PaymentInitiationResponse {
            return PaymentInitiationResponse(
                tid = result.tid,
                redirectUrl = result.nextRedirectPcUrl,
                mobileRedirectUrl = result.nextRedirectMobileUrl
            )
        }
    }
}

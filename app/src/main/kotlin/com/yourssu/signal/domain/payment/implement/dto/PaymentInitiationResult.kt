package com.yourssu.signal.domain.payment.implement.dto

data class PaymentInitiationResult(
    val tid: String,
    val redirectUrl: String,
    val mobileRedirectUrl: String
) {
    companion object {
        fun from(response: KakaoPayReadyResponse): PaymentInitiationResult {
            return PaymentInitiationResult(
                tid = response.tid,
                redirectUrl = response.nextRedirectPcUrl,
                mobileRedirectUrl = response.nextRedirectMobileUrl
            )
        }
    }
}

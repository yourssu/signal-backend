package com.yourssu.signal.infrastructure.payment.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.yourssu.signal.domain.payment.implement.dto.KakaoPayReadyResponse
import java.time.LocalDateTime

data class KakaoPayReadyResponse(
    val tid: String,
    @JsonProperty("next_redirect_pc_url")
    val nextRedirectPcUrl: String,
    @JsonProperty("next_redirect_mobile_url")
    val nextRedirectMobileUrl: String,
    @JsonProperty("created_at")
    val createdAt: LocalDateTime
) {
    fun toPaymentReady(): KakaoPayReadyResponse {
        return KakaoPayReadyResponse(
            tid = tid,
            nextRedirectPcUrl = nextRedirectPcUrl,
            nextRedirectMobileUrl = nextRedirectMobileUrl
        )
    }
}

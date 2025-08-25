package com.yourssu.signal.domain.payment.implement.dto

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.payment.implement.domain.KakaoPayOrder

data class KakaoPayReadyResponse(
    val tid: String,
    val nextRedirectPcUrl: String,
    val nextRedirectMobileUrl: String
) {
    fun toOrder(request: OrderReadyRequest): KakaoPayOrder {
        return KakaoPayOrder(
            tid = tid,
            viewerUuid = Uuid(request.viewerUuid),
            orderId = request.orderId,
            itemName = request.itemName,
            amount = request.price,
            quantity = request.quantity,
        )
    }

    fun toPaymentInitiation(orderId: String): PaymentInitiation {
        return PaymentInitiation(
            orderId = orderId,
            nextRedirectPcUrl = nextRedirectPcUrl,
            nextRedirectMobileUrl = nextRedirectMobileUrl
        )
    }
}

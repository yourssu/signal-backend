package com.yourssu.signal.domain.payment.implement.dto

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.payment.implement.domain.TossPaymentsOrder

data class TossPaymentsReadyResponse(
    val paymentKey: String,
    val checkoutUrl: String
) {
    fun toOrder(request: OrderReadyRequest): TossPaymentsOrder {
        return TossPaymentsOrder(
            paymentKey = paymentKey,
            uuid = Uuid(request.uuid),
            orderId = request.orderId,
            itemName = request.itemName,
            amount = request.price,
            quantity = request.quantity,
        )
    }

    fun toPaymentInitiation(orderId: String): PaymentInitiation {
        return PaymentInitiation(
            orderId = orderId,
            nextRedirectPcUrl = checkoutUrl,
            nextRedirectMobileUrl = checkoutUrl
        )
    }
}
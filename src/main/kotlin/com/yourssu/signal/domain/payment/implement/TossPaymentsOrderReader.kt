package com.yourssu.signal.domain.payment.implement

import com.yourssu.signal.domain.payment.implement.domain.TossPaymentsOrder
import org.springframework.stereotype.Component

@Component
class TossPaymentsOrderReader(
    val tossPaymentsOrderRepository: TossPaymentsOrderRepository
) {
    fun getByOrderId(orderId: String): TossPaymentsOrder {
        return tossPaymentsOrderRepository.getByOrderId(orderId)
    }
}
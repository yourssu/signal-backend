package com.yourssu.signal.domain.payment.implement

import com.yourssu.signal.domain.payment.implement.domain.TossPaymentsOrder
import com.yourssu.signal.domain.payment.implement.domain.OrderStatus
import com.yourssu.signal.domain.payment.implement.exception.InvalidPaymentStatusException
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class TossPaymentsOrderWriter(
    private val tossPaymentsOrderRepository: TossPaymentsOrderRepository
) {
    fun create(tossPaymentsOrder: TossPaymentsOrder): TossPaymentsOrder {
        return tossPaymentsOrderRepository.save(tossPaymentsOrder)
    }
    
    fun approve(order: TossPaymentsOrder, approvedTime: LocalDateTime): TossPaymentsOrder {
        if (order.status != OrderStatus.READY) {
            throw InvalidPaymentStatusException(order.status)
        }
        order.complete(approvedTime)
        return tossPaymentsOrderRepository.save(order)
    }
}
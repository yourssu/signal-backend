package com.yourssu.signal.domain.payment.implement

import com.yourssu.signal.domain.payment.implement.domain.KakaoPayOrder
import com.yourssu.signal.domain.payment.implement.domain.OrderStatus
import com.yourssu.signal.domain.payment.implement.exception.InvalidPaymentStatusException
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class KakaoPayOrderWriter(
    private val kakaoPayOrderRepository: KakaoPayOrderRepository
) {
    fun create(kakaoPayOrder: KakaoPayOrder): KakaoPayOrder {
        return kakaoPayOrderRepository.save(kakaoPayOrder)
    }
    
    fun approve(order: KakaoPayOrder, aid: String, approvedTime: LocalDateTime): KakaoPayOrder {
        if (order.status != OrderStatus.READY) {
            throw InvalidPaymentStatusException(order.status)
        }
        order.complete(aid, approvedTime)
        return kakaoPayOrderRepository.save(order)
    }
}

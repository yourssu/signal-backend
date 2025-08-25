package com.yourssu.signal.domain.payment.implement

import com.yourssu.signal.domain.payment.implement.domain.KakaoPayOrder
import org.springframework.stereotype.Component

@Component
class KakaoPayOrderReader(
    val kakaoPayOrderRepository: KakaoPayOrderRepository
) {
    fun getByOrderId(orderId: String): KakaoPayOrder {
        return kakaoPayOrderRepository.getByOrderId(orderId)
    }
}

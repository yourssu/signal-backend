package com.yourssu.signal.domain.order.implement

import org.springframework.stereotype.Component

@Component
class OrderHistoryReader(
    private val orderHistoryRepository: OrderHistoryRepository,
) {
    fun getByOrderId(orderId: String): OrderHistory {
        return orderHistoryRepository.getByOrderId(orderId)
    }

    fun findByViewerUuid(viewerUuid: String): List<OrderHistory> {
        return orderHistoryRepository.findByViewerUuid(viewerUuid)
    }

    fun existsByOrderId(orderId: String): Boolean {
        return orderHistoryRepository.existsByOrderId(orderId)
    }
}

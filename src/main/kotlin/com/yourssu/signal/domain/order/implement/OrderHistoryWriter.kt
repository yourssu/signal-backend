package com.yourssu.signal.domain.order.implement

import com.yourssu.signal.domain.order.implement.OrderStatus
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class OrderHistoryWriter(
    private val orderHistoryRepository: OrderHistoryRepository,
) {
    fun createOrderHistory(orderHistory: OrderHistory): OrderHistory {
        return orderHistoryRepository.save(orderHistory)
    }

    fun completeOrder(orderId: String): OrderHistory {
        val orderHistory = orderHistoryRepository.getByOrderId(orderId)
        orderHistoryRepository.updateStatus(orderHistory.id!!, OrderStatus.COMPLETED)
        return orderHistoryRepository.getByOrderId(orderHistory.orderId)
    }

    fun cancelOrder(orderId: String): OrderHistory {
        val orderHistory = orderHistoryRepository.getByOrderId(orderId)
        orderHistoryRepository.updateStatus(orderHistory.id!!, OrderStatus.CANCELLED)
        return orderHistoryRepository.getByOrderId(orderHistory.orderId)
    }

    fun refundOrder(orderId: String): OrderHistory {
        val orderHistory = orderHistoryRepository.getByOrderId(orderId)
        orderHistoryRepository.updateStatus(orderHistory.id!!, OrderStatus.REFUNDED)
        return orderHistoryRepository.getByOrderId(orderHistory.orderId)
    }
}

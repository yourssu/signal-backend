package com.yourssu.signal.domain.order.implement

import com.yourssu.signal.domain.order.implement.domain.OrderStatus

interface OrderHistoryRepository {
    fun save(orderHistory: OrderHistory): OrderHistory
    fun getByOrderId(orderId: String): OrderHistory
    fun findByViewerUuid(viewerUuid: String): List<OrderHistory>
    fun existsByOrderId(orderId: String): Boolean
    fun updateStatus(id: Long, status: OrderStatus)
}

package com.yourssu.signal.domain.order.implement


import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.order.implement.OrderStatus

class OrderHistory(
    val id: Long? = null,
    val orderId: String = Uuid.randomUUID().value,
    val uuid: Uuid,
    val amount: Int,
    val quantity: Int,
    val orderType: OrderType,
    val status: OrderStatus,
)

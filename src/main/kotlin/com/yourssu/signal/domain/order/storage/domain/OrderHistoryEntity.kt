package com.yourssu.signal.domain.order.storage.domain

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.common.storage.BaseEntity
import com.yourssu.signal.domain.order.implement.OrderHistory
import com.yourssu.signal.domain.order.implement.OrderType
import com.yourssu.signal.domain.order.implement.domain.OrderStatus
import jakarta.persistence.*

@Entity
@Table(name = "order_history")
class OrderHistoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true, name = "order_id")
    val orderId: String,

    @Column(nullable = false, name = "viewer_uuid")
    val uuid: String,

    @Column(nullable = false)
    val amount: Int,

    @Column(nullable = false)
    val quantity: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "order_type")
    var orderType: OrderType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status")
    var status: OrderStatus,
): BaseEntity() {
    companion object {
        fun from(orderHistory: OrderHistory) = OrderHistoryEntity(
            id = orderHistory.id,
            orderId = orderHistory.orderId,
            uuid = orderHistory.uuid.value,
            amount = orderHistory.amount,
            quantity = orderHistory.quantity,
            orderType = orderHistory.orderType,
            status = orderHistory.status,
        )
    }

    fun toDomain() = OrderHistory(
        id = id,
        orderId = orderId,
        uuid = Uuid(uuid),
        amount = amount,
        quantity = quantity,
        orderType = orderType,
        status = status,
    )
}

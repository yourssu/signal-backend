package com.yourssu.signal.domain.payment.storage

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.common.storage.BaseEntity
import com.yourssu.signal.domain.payment.implement.KakaoPayOrder
import com.yourssu.signal.domain.payment.implement.OrderStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "kakaopay_order")
class KakaoPayOrderEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val uuid: String,

    @Column(nullable = false, unique = true)
    val tid: String,

    @Column(nullable = false)
    val orderId: String,

    @Column(nullable = false)
    val itemName: String,

    @Column(nullable = false)
    val amount: Int,

    @Column(nullable = false)
    val quantity: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus,

    @Column
    var aid: String? = null,

    @Column
    var approvedTime: LocalDateTime? = null,

    @Column
    var canceledTime: LocalDateTime? = null,

    @Column
    var failedTime: LocalDateTime? = null
) : BaseEntity() {
    fun toDomain(): KakaoPayOrder {
        return KakaoPayOrder(
            id = id,
            uuid = Uuid(uuid),
            tid = tid,
            orderId = orderId,
            itemName = itemName,
            amount = amount,
            quantity = quantity,
            status = status,
            aid = aid,
            approvedTime = approvedTime,
            canceledTime = canceledTime,
            failedTime = failedTime
        )
    }

    companion object {
        fun from(kakaoPayOrder: KakaoPayOrder): KakaoPayOrderEntity {
            return KakaoPayOrderEntity(
                id = kakaoPayOrder.id,
                uuid = kakaoPayOrder.uuid.value,
                tid = kakaoPayOrder.tid,
                orderId = kakaoPayOrder.orderId,
                itemName = kakaoPayOrder.itemName,
                amount = kakaoPayOrder.amount,
                quantity = kakaoPayOrder.quantity,
                status = kakaoPayOrder.status,
                aid = kakaoPayOrder.aid,
                approvedTime = kakaoPayOrder.approvedTime,
                canceledTime = kakaoPayOrder.canceledTime,
                failedTime = kakaoPayOrder.failedTime
            )
        }
    }
}

package com.yourssu.signal.domain.payment.implement.domain

import com.yourssu.signal.domain.common.implement.Uuid
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class KakaoPayOrder(
    val id: Long? = null,
    val orderId: String,
    val viewerUuid: Uuid,
    val tid: String,
    val itemName: String,
    val amount: Int,
    val quantity: Int,
    var status: OrderStatus = OrderStatus.READY,
    var aid: String? = null,
    var approvedTime: LocalDateTime? = null,
    var canceledTime: LocalDateTime? = null,
    var failedTime: LocalDateTime? = null
) {
    companion object {
        private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmmss")
        
        fun generateOrderId(viewerUuid: String): String {
            val timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER)
            return "${viewerUuid}_${timestamp}"
        }

        fun generateItemName(quantity: Int, price: Int): String {
            return "시그널 티켓 ${quantity}장 (${price}원)"
        }
    }
    
    fun complete(aid: String, approvedTime: LocalDateTime) {
        this.status = OrderStatus.COMPLETED
        this.aid = aid
        this.approvedTime = approvedTime
    }
    
    fun cancel() {
        this.status = OrderStatus.CANCELED
        this.canceledTime = LocalDateTime.now()
    }
    
    fun fail() {
        this.status = OrderStatus.FAILED
        this.failedTime = LocalDateTime.now()
    }
}



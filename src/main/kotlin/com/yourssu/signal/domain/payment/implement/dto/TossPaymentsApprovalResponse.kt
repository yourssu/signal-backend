package com.yourssu.signal.domain.payment.implement.dto

import java.time.LocalDateTime

data class TossPaymentsApprovalResponse(
    val paymentKey: String,
    val orderId: String,
    val uuid: String,
    val orderName: String,
    val totalAmount: Int,
    val approvedAt: LocalDateTime
)
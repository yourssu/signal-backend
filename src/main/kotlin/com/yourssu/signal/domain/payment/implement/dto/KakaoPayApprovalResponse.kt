package com.yourssu.signal.domain.payment.implement.dto

import java.time.LocalDateTime

data class KakaoPayApprovalResponse(
    val aid: String,
    val tid: String,
    val orderId: String,
    val uuid: String,
    val itemName: String,
    val totalAmount: Int,
    val approvedTime: LocalDateTime
)

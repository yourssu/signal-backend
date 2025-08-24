package com.yourssu.signal.domain.payment.implement.dto

data class OrderApprovalRequest(
    val tid: String,
    val orderId: String,
    val viewerUuid: String,
    val pgToken: String
)

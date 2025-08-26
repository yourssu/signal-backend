package com.yourssu.signal.domain.payment.implement.dto

data class OrderReadyRequest(
    val uuid: String,
    val orderId: String,
    val itemName: String,
    val quantity: Int,
    val price: Int
)

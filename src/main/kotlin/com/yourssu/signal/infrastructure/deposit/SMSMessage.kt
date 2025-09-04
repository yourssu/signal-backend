package com.yourssu.signal.infrastructure.deposit

data class SMSMessage(
    val depositAmount: Int,
    val name: String,
    val remainingAmount: Int? = null
) {
}

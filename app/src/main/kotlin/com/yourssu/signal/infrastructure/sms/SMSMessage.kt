package com.yourssu.signal.infrastructure.sms

data class SMSMessage(
    val depositAmount: Int,
    val name: String,
    val remainingAmount: Int? = null
) {
}

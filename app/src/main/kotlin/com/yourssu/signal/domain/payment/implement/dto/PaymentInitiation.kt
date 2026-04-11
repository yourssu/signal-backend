package com.yourssu.signal.domain.payment.implement.dto

data class PaymentInitiation(
    val orderId: String,
    val nextRedirectPcUrl: String,
    val nextRedirectMobileUrl: String,
){
}

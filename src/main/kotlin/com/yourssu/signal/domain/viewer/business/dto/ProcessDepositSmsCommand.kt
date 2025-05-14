package com.yourssu.signal.domain.viewer.business.dto

class ProcessDepositSmsCommand(
    val secretKey: String,
    val message: String,
    val type: String,
) {
}

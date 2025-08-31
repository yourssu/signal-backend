package com.yourssu.signal.domain.viewer.business.command

class ProcessDepositSmsCommand(
    val secretKey: String,
    val message: String,
    val type: String,
) {
}

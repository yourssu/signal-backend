package com.yourssu.signal.domain.payment.business.command

import com.yourssu.signal.domain.common.implement.Uuid

data class PaymentApprovalCommand(
    val uuid: String,
    val tid: String,
    val pgToken: String
) {
    fun toUuid(): Uuid {
        return Uuid(uuid)
    }
}

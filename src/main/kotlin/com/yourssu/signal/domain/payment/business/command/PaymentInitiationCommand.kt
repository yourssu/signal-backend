package com.yourssu.signal.domain.payment.business.command

import com.yourssu.signal.domain.common.implement.Uuid

data class PaymentInitiationCommand(
    val uuid: String,
    val quantity: Int,
    val price: Int
) {
    fun toUuid(): Uuid {
        return Uuid(uuid)
    }
}

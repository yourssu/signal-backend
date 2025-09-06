package com.yourssu.signal.domain.payment.business.command

import com.yourssu.signal.domain.common.implement.Uuid

class PaymentInitiationCommand(
    val uuid: String,
    val packageId: String,
) {
    fun toUuid(): Uuid {
        return Uuid(uuid)
    }
}

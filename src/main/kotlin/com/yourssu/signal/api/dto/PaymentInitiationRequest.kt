package com.yourssu.signal.api.dto.dto

import com.yourssu.signal.domain.payment.business.command.PaymentInitiationCommand
import jakarta.validation.constraints.Positive

data class PaymentInitiationRequest(
    @field:Positive
    val quantity: Int,

    @field:Positive
    val price: Int,
) {
    fun toCommand(uuid: String): PaymentInitiationCommand {
        return PaymentInitiationCommand(
            uuid = uuid,
            quantity = quantity,
            price = price
        )
    }
}

package com.yourssu.signal.domain.payment.application.dto

import com.yourssu.signal.domain.payment.business.command.PaymentInitiationCommand
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class PaymentInitiationRequest(
    @field:NotBlank
    val uuid: String,

    @field:Positive
    val quantity: Int,

    @field:Positive
    val price: Int,
) {
    fun toCommand(): PaymentInitiationCommand {
        return PaymentInitiationCommand(
            uuid = uuid,
            quantity = quantity,
            price = price
        )
    }
}

package com.yourssu.signal.api.dto

import com.yourssu.signal.domain.payment.business.command.PaymentInitiationCommand
import jakarta.validation.constraints.NotBlank

data class PaymentInitiationRequest(
    @field:NotBlank
    val packageId: String,
) {
    fun toCommand(uuid: String): PaymentInitiationCommand {
        return PaymentInitiationCommand(
            uuid = uuid,
            packageId = packageId,
        )
    }
}

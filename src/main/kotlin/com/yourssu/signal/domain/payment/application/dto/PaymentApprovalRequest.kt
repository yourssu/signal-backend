package com.yourssu.signal.domain.payment.application.dto

import com.yourssu.signal.domain.payment.business.command.PaymentApprovalCommand
import jakarta.validation.constraints.NotBlank

data class PaymentApprovalRequest(
    @field:NotBlank
    val orderId: String,
    
    @field:NotBlank
    val pgToken: String
) {
    fun toCommand(uuid: String): PaymentApprovalCommand {
        return PaymentApprovalCommand(
            uuid = uuid,
            orderId = orderId,
            pgToken = pgToken
        )
    }
}

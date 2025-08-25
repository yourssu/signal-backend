package com.yourssu.signal.api.dto.dto

import com.yourssu.signal.domain.payment.business.command.PaymentApprovalCommand
import jakarta.validation.constraints.NotBlank

data class PaymentApprovalRequest(
    @field:NotBlank
    val tid: String,
    
    @field:NotBlank
    val pgToken: String
) {
    fun toCommand(uuid: String): PaymentApprovalCommand {
        return PaymentApprovalCommand(
            uuid = uuid,
            tid = tid,
            pgToken = pgToken
        )
    }
}

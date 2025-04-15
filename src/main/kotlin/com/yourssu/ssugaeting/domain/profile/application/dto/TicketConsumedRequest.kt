package com.yourssu.ssugaeting.domain.profile.application.dto

import com.yourssu.ssugaeting.domain.profile.business.command.TicketConsumedCommand
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class TicketConsumedRequest(
    @field:NotNull
    val profileId: Long,

    @field:NotBlank
    val uuid: String,
){
    fun toCommand(): TicketConsumedCommand {
        return TicketConsumedCommand(
            profileId = profileId,
            uuid = uuid,
        )
    }

}

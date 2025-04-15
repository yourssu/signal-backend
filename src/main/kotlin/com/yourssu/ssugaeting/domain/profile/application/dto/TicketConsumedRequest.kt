package com.yourssu.ssugaeting.domain.profile.application.dto

import com.yourssu.ssugaeting.domain.profile.business.TicketConsumedCommand
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

class TicketConsumedRequest(
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

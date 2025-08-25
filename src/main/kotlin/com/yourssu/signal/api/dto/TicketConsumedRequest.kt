package com.yourssu.signal.api.dto

import com.yourssu.signal.domain.profile.business.command.TicketConsumedCommand
import jakarta.validation.constraints.NotNull

data class TicketConsumedRequest(
    @field:NotNull
    val profileId: Long,
){
    fun toCommand(uuid: String): TicketConsumedCommand {
        return TicketConsumedCommand(
            profileId = profileId,
            uuid = uuid,
        )
    }

}

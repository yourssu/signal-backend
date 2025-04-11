package com.yourssu.ssugaeting.domain.profile.application.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

class TicketConsumedRequest(
    @field:NotNull
    val profileId: Long,

    @field:NotBlank
    val uuid: String,
){

}

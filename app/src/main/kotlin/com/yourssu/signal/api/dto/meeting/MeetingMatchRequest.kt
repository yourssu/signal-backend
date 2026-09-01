package com.yourssu.signal.api.dto.meeting

import com.yourssu.signal.domain.meeting.business.command.MeetingMatchCommand
import com.yourssu.signal.domain.profile.support.ContactFormat
import jakarta.validation.Valid
import jakarta.validation.constraints.Size

data class MeetingMatchRequest(
    @field:Valid
    val representative: MeetingMemberRequest,

    @field:ContactFormat
    val contact: String,

    @field:Size(min = 1, max = 3)
    @field:Valid
    val companions: List<MeetingMemberRequest>,
) {
    fun toCommand(uuid: String, roomId: Long) = MeetingMatchCommand(
        uuid = uuid,
        roomId = roomId,
        representative = representative.toCommand(),
        contact = contact,
        companions = companions.map { it.toCommand() },
    )
}

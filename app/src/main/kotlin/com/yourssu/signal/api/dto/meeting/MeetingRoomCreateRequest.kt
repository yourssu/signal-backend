package com.yourssu.signal.api.dto.meeting

import com.yourssu.signal.domain.meeting.business.command.MeetingRoomCreateCommand
import com.yourssu.signal.domain.meeting.implement.MeetingRoom
import com.yourssu.signal.domain.meeting.implement.MeetingSlot
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class MeetingRoomCreateRequest(
    @field:NotBlank
    val slot: String,

    @field:NotBlank
    @field:Size(max = MeetingRoom.MAX_INVITATION_LENGTH)
    val invitation: String,

    @field:Size(min = 1, max = 3)
    @field:Valid
    val companions: List<MeetingMemberRequest>,
) {
    fun toCommand(uuid: String) = MeetingRoomCreateCommand(
        uuid = uuid,
        slot = MeetingSlot.of(slot),
        invitation = invitation,
        companions = companions.map { it.toCommand() },
    )
}

package com.yourssu.signal.domain.meeting.business.command

import com.yourssu.signal.domain.meeting.implement.MeetingSlot
import com.yourssu.signal.domain.profile.implement.Gender

data class MeetingMemberCommand(
    val gender: Gender,
    val birthYear: Int,
    val department: String,
)

data class MeetingRoomCreateCommand(
    val uuid: String,
    val slot: MeetingSlot,
    val invitation: String,
    val companions: List<MeetingMemberCommand>,
) {
    val partySize: Int = companions.size + 1
}

data class MeetingMatchCommand(
    val uuid: String,
    val roomId: Long,
    val representative: MeetingMemberCommand,
    val contact: String,
    val companions: List<MeetingMemberCommand>,
)

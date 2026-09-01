package com.yourssu.signal.domain.meeting.implement

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.profile.implement.Gender

data class MeetingMember(
    val id: Long? = null,
    val roomId: Long,
    val teamSide: MeetingTeamSide,
    val memberOrder: Int,
    val userUuid: Uuid?,
    val gender: Gender,
    val birthYear: Int,
    val department: String,
)

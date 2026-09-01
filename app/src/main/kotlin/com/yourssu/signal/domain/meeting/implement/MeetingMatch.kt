package com.yourssu.signal.domain.meeting.implement

import com.yourssu.signal.domain.common.implement.Uuid
import java.time.LocalDateTime

data class MeetingMatch(
    val id: Long? = null,
    val roomId: Long,
    val applicantUuid: Uuid,
    val creatorContact: String,
    val applicantContact: String,
    val matchedAt: LocalDateTime,
)

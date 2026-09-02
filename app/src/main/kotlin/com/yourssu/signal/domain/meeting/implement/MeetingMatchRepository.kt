package com.yourssu.signal.domain.meeting.implement

import com.yourssu.signal.domain.common.implement.Uuid
import java.time.LocalDate

interface MeetingMatchRepository {
    fun save(match: MeetingMatch): MeetingMatch
    fun findByRoomId(roomId: Long): MeetingMatch?
    fun findAllByApplicantUuid(applicantUuid: Uuid): List<MeetingMatch>
    fun existsByApplicantUuidAndMatchedDate(applicantUuid: Uuid, matchedDate: LocalDate): Boolean
}

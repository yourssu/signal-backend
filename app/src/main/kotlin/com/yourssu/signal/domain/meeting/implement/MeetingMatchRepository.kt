package com.yourssu.signal.domain.meeting.implement

import com.yourssu.signal.domain.common.implement.Uuid

interface MeetingMatchRepository {
    fun save(match: MeetingMatch): MeetingMatch
    fun findByRoomId(roomId: Long): MeetingMatch?
    fun findAllByApplicantUuid(applicantUuid: Uuid): List<MeetingMatch>
}

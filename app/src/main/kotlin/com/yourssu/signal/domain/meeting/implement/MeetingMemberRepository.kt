package com.yourssu.signal.domain.meeting.implement

interface MeetingMemberRepository {
    fun saveAll(members: List<MeetingMember>): List<MeetingMember>
    fun findAllByRoomId(roomId: Long): List<MeetingMember>
}

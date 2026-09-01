package com.yourssu.signal.domain.meeting.implement

import com.yourssu.signal.domain.profile.implement.ProfileValidator

object MeetingTeamValidator {
    fun validate(partySize: Int, members: List<MeetingMember>) {
        if (members.size != partySize || members.count { it.memberOrder == 0 } != 1) {
            throw InvalidCompanionCountException()
        }
        if (members.any { (it.memberOrder == 0) != (it.userUuid != null) }) {
            throw InvalidMeetingMemberException()
        }
        if (members.map { it.memberOrder }.sorted() != (0 until partySize).toList()) {
            throw InvalidMeetingMemberException()
        }
        members.forEach {
            ProfileValidator.validateBirthYear(it.birthYear)
            ProfileValidator.validateDepartment(it.department)
        }
    }
}

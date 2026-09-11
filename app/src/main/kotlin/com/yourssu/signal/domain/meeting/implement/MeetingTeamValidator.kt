package com.yourssu.signal.domain.meeting.implement

import com.yourssu.signal.domain.profile.implement.ProfileValidator

object MeetingTeamValidator {
    fun validateGenderComposition(creatorMembers: List<MeetingMember>, applicantMembers: List<MeetingMember>) {
        val creatorGenders = creatorMembers.map { it.gender }.toSet()
        if (creatorGenders.size != 1) return
        val applicantGenders = applicantMembers.map { it.gender }.toSet()
        if (applicantGenders != setOf(creatorGenders.first().opposite())) {
            throw SameGenderMatchNotAllowedException()
        }
    }

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

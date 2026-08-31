package com.yourssu.signal.domain.meeting.implement

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.profile.implement.Gender
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec

class MeetingTeamValidatorTest : DescribeSpec({
    fun member(order: Int) = MeetingMember(
        roomId = 1L,
        teamSide = MeetingTeamSide.CREATOR,
        memberOrder = order,
        userUuid = if (order == 0) Uuid("representative") else null,
        gender = Gender.MALE,
        birthYear = 2000,
        department = "컴퓨터학부",
    )

    describe("팀 구성") {
        it("대표 한 명과 인원수보다 한 명 적은 동행만 허용한다") {
            shouldNotThrowAny { MeetingTeamValidator.validate(3, listOf(member(0), member(1), member(2))) }
            shouldThrow<InvalidCompanionCountException> {
                MeetingTeamValidator.validate(3, listOf(member(0), member(1)))
            }
        }

        it("동행에게 UUID를 부여하지 않는다") {
            shouldThrow<InvalidMeetingMemberException> {
                MeetingTeamValidator.validate(2, listOf(member(0), member(1).copy(userUuid = Uuid("friend"))))
            }
        }
    }
})

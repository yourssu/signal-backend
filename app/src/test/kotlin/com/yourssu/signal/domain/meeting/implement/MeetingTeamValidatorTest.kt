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

    fun team(side: MeetingTeamSide, vararg genders: Gender) = genders.mapIndexed { index, gender ->
        member(index).copy(
            teamSide = side,
            userUuid = if (index == 0) Uuid("representative") else null,
            gender = gender,
        )
    }

    describe("팀 성별 구성") {
        it("동성으로만 구성된 방에는 이성으로만 구성된 팀이 신청할 수 있다") {
            shouldNotThrowAny {
                MeetingTeamValidator.validateGenderComposition(
                    team(MeetingTeamSide.CREATOR, Gender.MALE, Gender.MALE),
                    team(MeetingTeamSide.APPLICANT, Gender.FEMALE, Gender.FEMALE),
                )
            }
        }

        it("동성으로만 구성된 방에 같은 성별 팀은 신청할 수 없다") {
            shouldThrow<SameGenderMatchNotAllowedException> {
                MeetingTeamValidator.validateGenderComposition(
                    team(MeetingTeamSide.CREATOR, Gender.FEMALE, Gender.FEMALE),
                    team(MeetingTeamSide.APPLICANT, Gender.FEMALE, Gender.FEMALE),
                )
            }
        }

        it("동성으로만 구성된 방에 혼성 팀은 신청할 수 없다") {
            shouldThrow<SameGenderMatchNotAllowedException> {
                MeetingTeamValidator.validateGenderComposition(
                    team(MeetingTeamSide.CREATOR, Gender.MALE, Gender.MALE),
                    team(MeetingTeamSide.APPLICANT, Gender.FEMALE, Gender.MALE),
                )
            }
        }

        it("혼성으로 구성된 방에는 성별 구성과 관계없이 신청할 수 있다") {
            shouldNotThrowAny {
                MeetingTeamValidator.validateGenderComposition(
                    team(MeetingTeamSide.CREATOR, Gender.MALE, Gender.FEMALE),
                    team(MeetingTeamSide.APPLICANT, Gender.MALE, Gender.MALE),
                )
            }
        }
    }

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

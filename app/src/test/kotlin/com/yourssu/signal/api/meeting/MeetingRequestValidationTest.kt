package com.yourssu.signal.api.meeting

import com.yourssu.signal.api.dto.meeting.MeetingMatchRequest
import com.yourssu.signal.api.dto.meeting.MeetingMemberRequest
import com.yourssu.signal.api.dto.meeting.MeetingRoomCreateRequest
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import jakarta.validation.Validation

class MeetingRequestValidationTest : DescribeSpec({
    val validator = Validation.buildDefaultValidatorFactory().validator
    val member = MeetingMemberRequest(
        birthYear = 2003,
        department = "컴퓨터학부",
        gender = "MALE",
    )

    describe("미팅 방 생성 요청") {
        it("인원수를 요청 필드로 받지 않는다") {
            MeetingRoomCreateRequest::class.java.declaredFields.map { it.name } shouldNotContain "partySize"
        }

        it("동행 친구가 세 명이면 허용한다") {
            val request = MeetingRoomCreateRequest(
                slot = "SLOT_1",
                invitation = "같이 축제를 즐겨요",
                companions = listOf(member, member, member),
            )

            validator.validate(request).map { it.propertyPath.toString() } shouldNotContain "companions"
        }

        it("생성자와 동행 친구 수를 합산해 인원수를 계산한다") {
            (1..3).forEach { companionCount ->
                val request = MeetingRoomCreateRequest(
                    slot = "SLOT_1",
                    invitation = "같이 축제를 즐겨요",
                    companions = List(companionCount) { member },
                )

                request.toCommand("creator").partySize shouldBe companionCount + 1
            }
        }

        it("대표자를 제외한 동행 친구가 없으면 거부한다") {
            val request = MeetingRoomCreateRequest(
                slot = "SLOT_1",
                invitation = "같이 축제를 즐겨요",
                companions = emptyList(),
            )

            validator.validate(request).map { it.propertyPath.toString() } shouldContain "companions"
        }

        it("동행 친구가 네 명이면 거부한다") {
            val request = MeetingRoomCreateRequest(
                slot = "SLOT_1",
                invitation = "같이 축제를 즐겨요",
                companions = listOf(member, member, member, member),
            )

            validator.validate(request).map { it.propertyPath.toString() } shouldContain "companions"
        }
    }

    describe("미팅 참여 요청") {
        it("연락처 형식이 올바르지 않으면 거부한다") {
            val request = MeetingMatchRequest(
                representative = member,
                contact = "0101234567",
                companions = listOf(member),
            )

            validator.validate(request).map { it.propertyPath.toString() } shouldContain "contact"
        }

        it("UUID를 요청 필드로 노출하지 않는다") {
            MeetingMatchRequest::class.java.declaredFields.none { it.name == "uuid" } shouldBe true
            MeetingRoomCreateRequest::class.java.declaredFields.none { it.name == "uuid" } shouldBe true
        }
    }
})

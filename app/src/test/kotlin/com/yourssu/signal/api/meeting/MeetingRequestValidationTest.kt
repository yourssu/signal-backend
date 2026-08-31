package com.yourssu.signal.api.meeting

import com.yourssu.signal.api.dto.meeting.MeetingMatchRequest
import com.yourssu.signal.api.dto.meeting.MeetingMemberRequest
import com.yourssu.signal.api.dto.meeting.MeetingRoomCreateRequest
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
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
        it("지원하지 않는 인원수를 거부한다") {
            val request = MeetingRoomCreateRequest(
                slot = "SLOT_1",
                partySize = 5,
                invitation = "같이 축제를 즐겨요",
                companions = listOf(member),
            )

            validator.validate(request).map { it.propertyPath.toString() } shouldContain "partySize"
        }

        it("대표자를 제외한 동행 친구가 없으면 거부한다") {
            val request = MeetingRoomCreateRequest(
                slot = "SLOT_1",
                partySize = 2,
                invitation = "같이 축제를 즐겨요",
                companions = emptyList(),
            )

            validator.validate(request).map { it.propertyPath.toString() } shouldContain "companions"
        }
    }

    describe("미팅 참여 요청") {
        it("연락처 형식이 올바르지 않으면 거부한다") {
            val request = MeetingMatchRequest(
                representative = member,
                contact = "01000000000",
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

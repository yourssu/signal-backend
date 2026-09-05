package com.yourssu.signal.api.meeting

import com.yourssu.signal.domain.meeting.business.dto.MeetingBoardResponse
import com.yourssu.signal.domain.meeting.business.dto.MeetingMemberResponse
import com.yourssu.signal.domain.meeting.business.dto.MeetingMyRoomResponse
import com.yourssu.signal.domain.meeting.business.dto.MeetingLatestMatchResponse
import com.yourssu.signal.domain.meeting.business.dto.MeetingRoomDetailResponse
import com.yourssu.signal.domain.meeting.business.dto.MeetingRoomResponse
import com.yourssu.signal.domain.meeting.business.dto.MeetingRoomSummaryResponse
import com.yourssu.signal.domain.meeting.business.dto.MeetingSlotResponse
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class MeetingContactExposureContractTest : DescribeSpec({
    describe("매칭 전 연락처 노출 계약") {
        it("보드와 방 상세 응답 타입에는 연락처 필드가 없다") {
            val preMatchResponseTypes = listOf(
                MeetingBoardResponse::class.java,
                MeetingSlotResponse::class.java,
                MeetingRoomSummaryResponse::class.java,
                MeetingRoomDetailResponse::class.java,
                MeetingRoomResponse::class.java,
                MeetingMemberResponse::class.java,
                MeetingLatestMatchResponse::class.java,
                MeetingMyRoomResponse::class.java,
            )

            preMatchResponseTypes
                .flatMap { it.declaredFields.toList() }
                .none { it.name.contains("contact", ignoreCase = true) } shouldBe true
        }
    }
})

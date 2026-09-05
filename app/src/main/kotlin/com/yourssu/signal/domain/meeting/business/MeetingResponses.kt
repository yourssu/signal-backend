package com.yourssu.signal.domain.meeting.business.dto

import com.yourssu.signal.domain.meeting.implement.MeetingRoomStatus
import com.yourssu.signal.domain.meeting.implement.MeetingSlot
import com.yourssu.signal.domain.meeting.implement.MeetingTeamSide
import com.yourssu.signal.domain.profile.implement.Animal
import com.yourssu.signal.domain.profile.implement.Gender
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

data class MeetingBoardResponse(
    val creationEligibility: MeetingCreationEligibilityResponse,
    val slots: List<MeetingSlotResponse>,
    val latestMatch: MeetingLatestMatchResponse?,
    val myRoom: MeetingMyRoomResponse?,
)

data class MeetingMyRoomResponse(
    val roomId: Long,
    @field:Schema(allowableValues = ["OPEN", "MATCHED"])
    val status: MeetingRoomStatus,
    val teamSide: MeetingTeamSide,
)

data class MeetingLatestMatchResponse(
    val roomId: Long,
    val creatorNickname: String,
    val creatorAnimal: Animal,
    val matchedAt: OffsetDateTime,
    val visibleUntil: OffsetDateTime,
)

data class MeetingCreationEligibilityResponse(
    val canCreate: Boolean,
    val reason: String? = null,
)

data class MeetingSlotResponse(
    val slot: MeetingSlot,
    val room: MeetingRoomSummaryResponse?,
)

data class MeetingRoomSummaryResponse(
    val id: Long,
    val creatorAnimal: Animal,
    val partySize: Int,
    val invitation: String,
    val expiresAt: OffsetDateTime,
)

data class MeetingRoomResponse(
    val id: Long,
    val slot: MeetingSlot,
    val creatorAnimal: Animal,
    val partySize: Int,
    val invitation: String,
    val status: MeetingRoomStatus,
    val expiresAt: OffsetDateTime,
)

data class MeetingRoomDetailResponse(
    val room: MeetingRoomResponse,
    val members: List<MeetingMemberResponse>,
)

data class MeetingMemberResponse(
    val teamSide: MeetingTeamSide,
    val memberOrder: Int,
    val gender: Gender,
    val birthYear: Int,
    val department: String,
)

data class MeetingMatchResponse(
    val roomId: Long,
    val status: MeetingRoomStatus,
    val counterpartContact: String,
)

data class MeetingResultResponse(
    val roomId: Long,
    val counterpartContact: String,
)

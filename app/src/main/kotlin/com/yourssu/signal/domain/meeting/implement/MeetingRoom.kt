package com.yourssu.signal.domain.meeting.implement

import com.yourssu.signal.domain.common.implement.Uuid
import java.time.LocalDate
import java.time.LocalDateTime

data class MeetingRoom(
    val id: Long? = null,
    val slot: MeetingSlot,
    val activeSlot: MeetingSlot?,
    val creatorUuid: Uuid,
    val partySize: Int,
    val invitation: String,
    val status: MeetingRoomStatus,
    val creationDate: LocalDate,
    val expiresAt: LocalDateTime,
    val matchedAt: LocalDateTime? = null,
    val cancelledAt: LocalDateTime? = null,
    val expiredAt: LocalDateTime? = null,
    val createdTime: LocalDateTime? = null,
) {
    init {
        if (partySize !in 2..4) throw InvalidPartySizeException()
        if (invitation.isBlank() || invitation.length > MAX_INVITATION_LENGTH) throw InvalidMeetingInvitationException()
    }

    fun match(now: LocalDateTime): MeetingRoom {
        validateOpen()
        return copy(status = MeetingRoomStatus.MATCHED, activeSlot = null, matchedAt = now)
    }

    fun cancel(now: LocalDateTime): MeetingRoom {
        validateOpen()
        return copy(status = MeetingRoomStatus.CANCELLED, activeSlot = null, cancelledAt = now)
    }

    fun expire(now: LocalDateTime): MeetingRoom {
        validateOpen()
        return copy(status = MeetingRoomStatus.EXPIRED, activeSlot = null, expiredAt = now)
    }

    fun isExpired(now: LocalDateTime): Boolean = !expiresAt.isAfter(now)

    private fun validateOpen() {
        when (status) {
            MeetingRoomStatus.OPEN -> Unit
            MeetingRoomStatus.MATCHED -> throw RoomAlreadyMatchedException()
            MeetingRoomStatus.CANCELLED -> throw RoomCancelledException()
            MeetingRoomStatus.EXPIRED -> throw RoomExpiredException()
        }
    }

    companion object {
        const val MAX_INVITATION_LENGTH = 500
    }
}

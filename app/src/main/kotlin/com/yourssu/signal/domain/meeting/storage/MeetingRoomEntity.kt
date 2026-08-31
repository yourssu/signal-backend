package com.yourssu.signal.domain.meeting.storage

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.common.storage.BaseEntity
import com.yourssu.signal.domain.meeting.implement.MeetingRoom
import com.yourssu.signal.domain.meeting.implement.MeetingRoomStatus
import com.yourssu.signal.domain.meeting.implement.MeetingSlot
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(
    name = "meeting_room",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_meeting_room_active_slot", columnNames = ["active_slot"]),
        UniqueConstraint(
            name = "uk_meeting_room_creator_date",
            columnNames = ["creator_uuid", "creation_date"],
        ),
    ],
)
class MeetingRoomEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val slot: MeetingSlot,

    @Enumerated(EnumType.STRING)
    @Column(name = "active_slot", length = 20)
    val activeSlot: MeetingSlot?,

    @Column(name = "creator_uuid", nullable = false, length = 36)
    val creatorUuid: String,

    @Column(name = "party_size", nullable = false)
    val partySize: Int,

    @Column(nullable = false, length = 500)
    val invitation: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val status: MeetingRoomStatus,

    @Column(name = "creation_date", nullable = false)
    val creationDate: LocalDate,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: LocalDateTime,

    @Column(name = "matched_at")
    val matchedAt: LocalDateTime? = null,

    @Column(name = "cancelled_at")
    val cancelledAt: LocalDateTime? = null,

    @Column(name = "expired_at")
    val expiredAt: LocalDateTime? = null,

    createdTime: LocalDateTime? = null,
) : BaseEntity(createdTime = createdTime) {
    companion object {
        fun from(room: MeetingRoom) = MeetingRoomEntity(
            id = room.id,
            slot = room.slot,
            activeSlot = room.activeSlot,
            creatorUuid = room.creatorUuid.value,
            partySize = room.partySize,
            invitation = room.invitation,
            status = room.status,
            creationDate = room.creationDate,
            expiresAt = room.expiresAt,
            matchedAt = room.matchedAt,
            cancelledAt = room.cancelledAt,
            expiredAt = room.expiredAt,
            createdTime = room.createdTime,
        )
    }

    fun toDomain() = MeetingRoom(
        id = id,
        slot = slot,
        activeSlot = activeSlot,
        creatorUuid = Uuid(creatorUuid),
        partySize = partySize,
        invitation = invitation,
        status = status,
        creationDate = creationDate,
        expiresAt = expiresAt,
        matchedAt = matchedAt,
        cancelledAt = cancelledAt,
        expiredAt = expiredAt,
        createdTime = createdTime,
    )
}

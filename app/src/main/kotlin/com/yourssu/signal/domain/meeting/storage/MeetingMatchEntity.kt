package com.yourssu.signal.domain.meeting.storage

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.common.storage.BaseEntity
import com.yourssu.signal.domain.meeting.implement.MeetingMatch
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Entity
@Table(
    name = "meeting_match",
    uniqueConstraints = [UniqueConstraint(name = "uk_meeting_match_room_id", columnNames = ["room_id"])],
)
class MeetingMatchEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "room_id", nullable = false)
    val roomId: Long,

    @Column(name = "applicant_uuid", nullable = false, length = 36)
    val applicantUuid: String,

    @Column(name = "creator_contact", nullable = false, length = 1024)
    val creatorContact: String,

    @Column(name = "applicant_contact", nullable = false, length = 1024)
    val applicantContact: String,

    @Column(name = "matched_at", nullable = false)
    val matchedAt: LocalDateTime,
) : BaseEntity() {
    companion object {
        fun from(match: MeetingMatch, creatorContact: String, applicantContact: String) = MeetingMatchEntity(
            id = match.id,
            roomId = match.roomId,
            applicantUuid = match.applicantUuid.value,
            creatorContact = creatorContact,
            applicantContact = applicantContact,
            matchedAt = match.matchedAt,
        )
    }

    fun toDomain(creatorContact: String, applicantContact: String) = MeetingMatch(
        id = id,
        roomId = roomId,
        applicantUuid = Uuid(applicantUuid),
        creatorContact = creatorContact,
        applicantContact = applicantContact,
        matchedAt = matchedAt,
    )
}

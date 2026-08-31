package com.yourssu.signal.domain.meeting.storage

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.common.storage.BaseEntity
import com.yourssu.signal.domain.meeting.implement.MeetingMember
import com.yourssu.signal.domain.meeting.implement.MeetingTeamSide
import com.yourssu.signal.domain.profile.implement.Gender
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "meeting_member",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_meeting_member_room_side_order",
            columnNames = ["room_id", "team_side", "member_order"],
        ),
    ],
)
class MeetingMemberEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "room_id", nullable = false)
    val roomId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "team_side", nullable = false, length = 20)
    val teamSide: MeetingTeamSide,

    @Column(name = "member_order", nullable = false)
    val memberOrder: Int,

    @Column(name = "user_uuid", length = 36)
    val userUuid: String?,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    val gender: Gender,

    @Column(name = "birth_year", nullable = false)
    val birthYear: Int,

    @Column(nullable = false)
    val department: String,
) : BaseEntity() {
    companion object {
        fun from(member: MeetingMember) = MeetingMemberEntity(
            id = member.id,
            roomId = member.roomId,
            teamSide = member.teamSide,
            memberOrder = member.memberOrder,
            userUuid = member.userUuid?.value,
            gender = member.gender,
            birthYear = member.birthYear,
            department = member.department,
        )
    }

    fun toDomain() = MeetingMember(
        id = id,
        roomId = roomId,
        teamSide = teamSide,
        memberOrder = memberOrder,
        userUuid = userUuid?.let(::Uuid),
        gender = gender,
        birthYear = birthYear,
        department = department,
    )
}

package com.yourssu.signal.domain.meeting.business

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.meeting.business.command.MeetingMatchCommand
import com.yourssu.signal.domain.meeting.business.command.MeetingMemberCommand
import com.yourssu.signal.domain.meeting.business.command.MeetingRoomCreateCommand
import com.yourssu.signal.domain.meeting.business.dto.*
import com.yourssu.signal.domain.meeting.implement.*
import com.yourssu.signal.domain.profile.implement.ProfileReader
import com.yourssu.signal.domain.profile.implement.ProfileValidator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class MeetingService(
    private val meetingRoomRepository: MeetingRoomRepository,
    private val meetingMemberRepository: MeetingMemberRepository,
    private val meetingMatchRepository: MeetingMatchRepository,
    private val profileReader: ProfileReader,
    private val expirationManager: MeetingExpirationManager,
    private val clock: Clock,
) {
    fun getBoard(uuid: String): MeetingBoardResponse {
        expirationManager.expireDueRooms(LocalDateTime.now(clock))
        val queryNow = LocalDateTime.now(clock)
        val userUuid = Uuid(uuid)
        val openRooms = meetingRoomRepository.findAllOpen(queryNow).associateBy { it.slot }
        val latestMatch = meetingRoomRepository.findLatestMatchedAfter(queryNow.minusSeconds(MATCH_NOTICE_SECONDS))
        val reason = when {
            !profileReader.existsByUuid(userUuid) -> PROFILE_REQUIRED
            meetingRoomRepository.existsByCreatorUuidAndCreationDate(userUuid, LocalDate.now(clock)) -> DAILY_CREATION_LIMIT_EXCEEDED
            else -> null
        }
        return MeetingBoardResponse(
            creationEligibility = MeetingCreationEligibilityResponse(reason == null, reason),
            slots = MeetingSlot.selectableEntries.map { slot ->
                MeetingSlotResponse(
                    slot = slot,
                    room = openRooms[slot]?.toSummary(),
                )
            },
            latestMatch = latestMatch?.toLatestMatchResponse(),
        )
    }

    @Transactional(rollbackFor = [com.yourssu.signal.handler.Error::class])
    fun createRoom(command: MeetingRoomCreateCommand): MeetingRoomResponse {
        val uuid = Uuid(command.uuid)
        if (!profileReader.existsByUuid(uuid)) throw ProfileRequiredException()
        if (meetingRoomRepository.existsByCreatorUuidAndCreationDate(uuid, LocalDate.now(clock))) {
            throw DailyCreationLimitExceededException()
        }
        val now = LocalDateTime.now(clock)
        meetingRoomRepository.expireDueRoomInSlot(command.slot, now)
        if (meetingRoomRepository.findOpenBySlot(command.slot, now) != null) throw SlotAlreadyOccupiedException()

        val profile = profileReader.getByUuid(uuid)
        val room = meetingRoomRepository.save(
            MeetingRoom(
                slot = command.slot,
                activeSlot = command.slot,
                creatorUuid = uuid,
                creatorAnimal = profile.animal,
                partySize = command.partySize,
                invitation = command.invitation,
                status = MeetingRoomStatus.OPEN,
                creationDate = LocalDate.now(clock),
                expiresAt = now.plusHours(1),
            )
        )

        val members = buildMembers(
            roomId = room.id!!,
            teamSide = MeetingTeamSide.CREATOR,
            representativeUuid = uuid,
            representative = MeetingMemberCommand(profile.gender, profile.birthYear, profile.department),
            companions = command.companions,
        )
        MeetingTeamValidator.validate(command.partySize, members)
        meetingMemberRepository.saveAll(members)
        return room.toResponse()
    }

    @Transactional(
        rollbackFor = [com.yourssu.signal.handler.Error::class],
        noRollbackFor = [RoomExpiredException::class],
    )
    fun getRoom(@Suppress("UNUSED_PARAMETER") uuid: String, roomId: Long): MeetingRoomDetailResponse {
        val room = meetingRoomRepository.findByIdForUpdate(roomId) ?: throw MeetingRoomNotFoundException()
        validateOpen(room, LocalDateTime.now(clock))
        return MeetingRoomDetailResponse(
            room = room.toResponse(),
            members = meetingMemberRepository.findAllByRoomId(roomId).map { it.toResponse() },
        )
    }

    @Transactional(
        rollbackFor = [com.yourssu.signal.handler.Error::class],
        noRollbackFor = [RoomExpiredException::class],
    )
    fun match(command: MeetingMatchCommand): MeetingMatchResponse {
        val room = meetingRoomRepository.findByIdForUpdate(command.roomId) ?: throw MeetingRoomNotFoundException()
        val lockedNow = LocalDateTime.now(clock)
        validateOpen(room, lockedNow)
        ProfileValidator.validateContact(command.contact)
        val applicantUuid = Uuid(command.uuid)
        if (applicantUuid == room.creatorUuid) throw SelfMatchNotAllowedException()
        val applicantMembers = buildMembers(
            roomId = room.id!!,
            teamSide = MeetingTeamSide.APPLICANT,
            representativeUuid = applicantUuid,
            representative = command.representative,
            companions = command.companions,
        )
        MeetingTeamValidator.validate(room.partySize, applicantMembers)
        val creatorContact = profileReader.getByUuid(room.creatorUuid).contact
        val meetingMatch = meetingMatchRepository.save(
            MeetingMatch(
                roomId = room.id,
                applicantUuid = applicantUuid,
                creatorContact = creatorContact,
                applicantContact = command.contact,
                matchedAt = lockedNow,
            )
        )
        meetingMemberRepository.saveAll(applicantMembers)
        meetingRoomRepository.save(room.match(lockedNow))
        return MeetingMatchResponse(meetingMatch.roomId, MeetingRoomStatus.MATCHED, meetingMatch.creatorContact)
    }

    @Transactional(
        rollbackFor = [com.yourssu.signal.handler.Error::class],
        noRollbackFor = [RoomExpiredException::class],
    )
    fun cancel(uuid: String, roomId: Long) {
        val room = meetingRoomRepository.findByIdForUpdate(roomId) ?: throw MeetingRoomNotFoundException()
        val lockedNow = LocalDateTime.now(clock)
        validateOpen(room, lockedNow)
        if (room.creatorUuid != Uuid(uuid)) throw MeetingRoomCancelForbiddenException()
        meetingRoomRepository.save(room.cancel(lockedNow))
    }

    @Transactional(
        rollbackFor = [com.yourssu.signal.handler.Error::class],
        noRollbackFor = [RoomExpiredException::class],
    )
    fun getResult(uuid: String, roomId: Long): MeetingResultResponse {
        val room = meetingRoomRepository.findByIdForUpdate(roomId) ?: throw MeetingRoomNotFoundException()
        validateMatched(room, LocalDateTime.now(clock))
        val match = meetingMatchRepository.findByRoomId(roomId) ?: throw MeetingRoomNotFoundException()
        val contact = when (Uuid(uuid)) {
            room.creatorUuid -> match.applicantContact
            match.applicantUuid -> match.creatorContact
            else -> throw MeetingResultForbiddenException()
        }
        return MeetingResultResponse(roomId, contact)
    }

    private fun buildMembers(
        roomId: Long,
        teamSide: MeetingTeamSide,
        representativeUuid: Uuid,
        representative: MeetingMemberCommand,
        companions: List<MeetingMemberCommand>,
    ): List<MeetingMember> = listOf(representative).plus(companions).mapIndexed { index, member ->
        MeetingMember(
            roomId = roomId,
            teamSide = teamSide,
            memberOrder = index,
            userUuid = representativeUuid.takeIf { index == 0 },
            gender = member.gender,
            birthYear = member.birthYear,
            department = member.department,
        )
    }

    private fun validateOpen(room: MeetingRoom, now: LocalDateTime) {
        expireIfDue(room, now)
        when (room.status) {
            MeetingRoomStatus.OPEN -> Unit
            MeetingRoomStatus.MATCHED -> throw RoomAlreadyMatchedException()
            MeetingRoomStatus.CANCELLED -> throw RoomCancelledException()
            MeetingRoomStatus.EXPIRED -> throw RoomExpiredException()
        }
    }

    private fun validateMatched(room: MeetingRoom, now: LocalDateTime) {
        expireIfDue(room, now)
        when (room.status) {
            MeetingRoomStatus.MATCHED -> Unit
            MeetingRoomStatus.OPEN -> throw MeetingRoomNotFoundException()
            MeetingRoomStatus.CANCELLED -> throw RoomCancelledException()
            MeetingRoomStatus.EXPIRED -> throw RoomExpiredException()
        }
    }

    private fun expireIfDue(room: MeetingRoom, now: LocalDateTime) {
        if (room.status == MeetingRoomStatus.OPEN && room.isExpired(now)) {
            meetingRoomRepository.save(room.expire(now))
            throw RoomExpiredException()
        }
    }

    private fun MeetingRoom.toResponse() = MeetingRoomResponse(
        id = id!!,
        slot = slot,
        creatorAnimal = creatorAnimal,
        partySize = partySize,
        invitation = invitation,
        status = status,
        expiresAt = expiresAt.atZone(clock.zone).toOffsetDateTime(),
    )

    private fun MeetingRoom.toSummary() = MeetingRoomSummaryResponse(
        id!!,
        creatorAnimal,
        partySize,
        invitation,
        expiresAt.atZone(clock.zone).toOffsetDateTime(),
    )

    private fun MeetingMember.toResponse() = MeetingMemberResponse(teamSide, memberOrder, gender, birthYear, department)

    private fun MeetingRoom.toLatestMatchResponse(): MeetingLatestMatchResponse? {
        val matchedTime = matchedAt ?: return null
        return MeetingLatestMatchResponse(
            roomId = id!!,
            creatorNickname = profileReader.getNicknameByUuid(creatorUuid),
            creatorAnimal = creatorAnimal,
            matchedAt = matchedTime.atZone(clock.zone).toOffsetDateTime(),
            visibleUntil = matchedTime.plusSeconds(MATCH_NOTICE_SECONDS).atZone(clock.zone).toOffsetDateTime(),
        )
    }

    companion object {
        const val MATCH_NOTICE_SECONDS = 30L
        const val PROFILE_REQUIRED = "PROFILE_REQUIRED"
        const val DAILY_CREATION_LIMIT_EXCEEDED = "DAILY_CREATION_LIMIT_EXCEEDED"
    }
}

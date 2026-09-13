package com.yourssu.signal.domain.meeting.implement

import com.yourssu.signal.domain.common.implement.Uuid
import java.time.LocalDate
import java.time.LocalDateTime

interface MeetingRoomRepository {
    fun save(room: MeetingRoom): MeetingRoom
    fun findById(id: Long): MeetingRoom?
    fun findByIdForUpdate(id: Long): MeetingRoom?
    fun findOpenBySlot(slot: MeetingSlot, now: LocalDateTime): MeetingRoom?
    fun findAllOpen(now: LocalDateTime): List<MeetingRoom>
    fun findLatestMatchedAfter(since: LocalDateTime): MeetingRoom?
    fun existsByCreatorUuidAndCreationDate(creatorUuid: Uuid, creationDate: LocalDate): Boolean
    fun existsOpenByCreatorUuid(creatorUuid: Uuid, now: LocalDateTime): Boolean
    fun findOpenByCreatorUuid(creatorUuid: Uuid, now: LocalDateTime): MeetingRoom?
    fun existsMatchedByCreatorUuidAndMatchedDate(creatorUuid: Uuid, matchedDate: LocalDate): Boolean
    fun findMatchedByCreatorUuidAndMatchedDate(creatorUuid: Uuid, matchedDate: LocalDate): MeetingRoom?
    fun expireDueRooms(now: LocalDateTime): Int
    fun expireDueRoomInSlot(slot: MeetingSlot, now: LocalDateTime): Int
}

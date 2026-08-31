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
    fun existsByCreatorUuidAndCreationDate(creatorUuid: Uuid, creationDate: LocalDate): Boolean
    fun expireDueRooms(now: LocalDateTime): Int
}

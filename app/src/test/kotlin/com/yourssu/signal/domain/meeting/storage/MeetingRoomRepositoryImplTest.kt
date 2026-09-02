package com.yourssu.signal.domain.meeting.storage

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.meeting.implement.DailyCreationLimitExceededException
import com.yourssu.signal.domain.meeting.implement.MeetingRoom
import com.yourssu.signal.domain.meeting.implement.MeetingRoomRepository
import com.yourssu.signal.domain.meeting.implement.MeetingRoomStatus
import com.yourssu.signal.domain.meeting.implement.MeetingSlot
import com.yourssu.signal.domain.meeting.implement.SlotAlreadyOccupiedException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MeetingRoomRepositoryImplTest {
    @Autowired lateinit var repository: MeetingRoomRepository
    @Autowired lateinit var jpaRepository: MeetingRoomJpaRepository

    @BeforeEach
    fun clean() {
        jpaRepository.deleteAll()
    }

    @Test
    fun `활성 방은 슬롯을 점유하고 종료된 방은 같은 슬롯을 다시 사용할 수 있다`() {
        val now = LocalDateTime.of(2026, 8, 31, 12, 0)
        val first = repository.save(room("creator-1", MeetingSlot.SLOT_1, now))

        repository.save(first.match(now.plusMinutes(10)))
        val second = repository.save(room("creator-2", MeetingSlot.SLOT_1, now.plusMinutes(11)))

        assertEquals(2, jpaRepository.countBySlot(MeetingSlot.SLOT_1))
        assertNull(jpaRepository.findById(first.id!!).orElseThrow().activeSlot)
        assertEquals(MeetingSlot.SLOT_1, second.activeSlot)
    }

    @Test
    fun `같은 슬롯에는 활성 방을 하나만 저장할 수 있다`() {
        val now = LocalDateTime.of(2026, 8, 31, 12, 0)
        repository.save(room("slot-creator-1", MeetingSlot.SLOT_2, now))

        assertThrows(SlotAlreadyOccupiedException::class.java) {
            repository.save(room("slot-creator-2", MeetingSlot.SLOT_2, now))
        }
    }

    @Test
    fun `같은 생성자는 같은 한국 날짜에 방을 하나만 저장할 수 있다`() {
        val now = LocalDateTime.of(2026, 8, 31, 12, 0)
        repository.save(room("daily-creator", MeetingSlot.SLOT_3, now))

        assertThrows(DailyCreationLimitExceededException::class.java) {
            repository.save(room("daily-creator", MeetingSlot.SLOT_4, now.plusHours(1)))
        }
    }

    @Test
    fun `만료 대상 방은 상태를 변경하고 활성 슬롯을 반환한다`() {
        val now = LocalDateTime.of(2026, 8, 31, 12, 0)
        val due = repository.save(room("expired-creator", MeetingSlot.SLOT_5, now.minusHours(2)))
        repository.save(room("open-creator", MeetingSlot.SLOT_6, now))

        assertEquals(1, repository.expireDueRooms(now))

        val expired = repository.findById(due.id!!)
        assertNotNull(expired)
        assertEquals(MeetingRoomStatus.EXPIRED, expired!!.status)
        assertNull(expired.activeSlot)
        assertEquals(now, expired.expiredAt)
        assertEquals(listOf(MeetingSlot.SLOT_6), repository.findAllOpen(now).map { it.slot })
    }

    @Test
    fun `선택한 슬롯의 만료 방만 상태를 변경하고 슬롯을 반환한다`() {
        val now = LocalDateTime.of(2026, 8, 31, 12, 0)
        val due = repository.save(room("expired-slot-creator", MeetingSlot.SLOT_3, now.minusHours(2)))
        val otherDue = repository.save(room("other-expired-creator", MeetingSlot.SLOT_4, now.minusHours(2)))

        assertEquals(1, repository.expireDueRoomInSlot(MeetingSlot.SLOT_3, now))

        assertEquals(MeetingRoomStatus.EXPIRED, repository.findById(due.id!!)!!.status)
        assertNull(repository.findById(due.id!!)!!.activeSlot)
        assertEquals(MeetingRoomStatus.OPEN, repository.findById(otherDue.id!!)!!.status)
        assertEquals(MeetingSlot.SLOT_4, repository.findById(otherDue.id!!)!!.activeSlot)
    }

    @Test
    fun `기준 시각 이후 매칭된 방 중 가장 최신 방을 반환한다`() {
        val now = LocalDateTime.of(2026, 8, 31, 12, 0)
        val old = repository.save(room("old-match", MeetingSlot.SLOT_1, now.minusMinutes(1)))
        repository.save(old.match(now.minusSeconds(31)))
        val recent = repository.save(room("recent-match", MeetingSlot.SLOT_2, now.minusSeconds(20)))
        repository.save(recent.match(now.minusSeconds(10)))
        repository.save(room("still-open", MeetingSlot.SLOT_3, now))

        val result = repository.findLatestMatchedAfter(now.minusSeconds(30))

        assertEquals(recent.id, result?.id)
    }

    @Test
    fun `정확히 기준 시각에 매칭된 방은 반환하지 않는다`() {
        val now = LocalDateTime.of(2026, 8, 31, 12, 0)
        val boundary = repository.save(room("boundary-match", MeetingSlot.SLOT_1, now.minusSeconds(30)))
        repository.save(boundary.match(now.minusSeconds(30)))

        assertNull(repository.findLatestMatchedAfter(now.minusSeconds(30)))
    }

    @Test
    fun `매칭 시각이 같으면 나중에 저장된 방을 반환한다`() {
        val now = LocalDateTime.of(2026, 8, 31, 12, 0)
        val first = repository.save(room("tie-first", MeetingSlot.SLOT_1, now.minusSeconds(20)))
        repository.save(first.match(now.minusSeconds(10)))
        val second = repository.save(room("tie-second", MeetingSlot.SLOT_2, now.minusSeconds(20)))
        repository.save(second.match(now.minusSeconds(10)))

        assertEquals(second.id, repository.findLatestMatchedAfter(now.minusSeconds(30))?.id)
    }

    private fun room(creator: String, slot: MeetingSlot, createdAt: LocalDateTime) = MeetingRoom(
        slot = slot,
        activeSlot = slot,
        creatorUuid = Uuid(creator),
        partySize = 3,
        invitation = "같이 만나요",
        status = MeetingRoomStatus.OPEN,
        creationDate = LocalDate.from(createdAt),
        expiresAt = createdAt.plusHours(1),
    )
}

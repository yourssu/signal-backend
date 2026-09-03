package com.yourssu.signal.domain.meeting.storage

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.meeting.implement.MeetingMatch
import com.yourssu.signal.domain.meeting.implement.MeetingMatchRepository
import com.yourssu.signal.domain.meeting.implement.RoomAlreadyMatchedException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
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
class MeetingMatchRepositoryImplTest {
    @Autowired lateinit var repository: MeetingMatchRepository
    @Autowired lateinit var jpaRepository: MeetingMatchJpaRepository

    @Test
    fun `양쪽 대표 연락처는 암호화 저장되고 도메인 조회에서만 복호화된다`() {
        val saved = repository.save(match(roomId = 1001))
        val raw = jpaRepository.findById(saved.id!!).orElseThrow()

        assertNotEquals("010-1111-2222", raw.creatorContact)
        assertNotEquals("010-3333-4444", raw.applicantContact)
        assertEquals("010-1111-2222", repository.findByRoomId(1001)!!.creatorContact)
        assertEquals("010-3333-4444", repository.findByRoomId(1001)!!.applicantContact)
    }

    @Test
    fun `한 방에는 성공 매칭을 하나만 저장할 수 있다`() {
        repository.save(match(roomId = 1002))

        assertThrows(RoomAlreadyMatchedException::class.java) {
            repository.save(match(roomId = 1002, applicantUuid = "other-applicant"))
        }
    }

    @Test
    fun `신청자의 당일 성공 매칭 이력만 조회한다`() {
        repository.save(match(roomId = 1003, applicantUuid = "daily-applicant"))

        assertTrue(repository.existsByApplicantUuidAndMatchedDate(Uuid("daily-applicant"), LocalDate.of(2026, 8, 31)))
        assertFalse(repository.existsByApplicantUuidAndMatchedDate(Uuid("daily-applicant"), LocalDate.of(2026, 9, 1)))
        assertFalse(repository.existsByApplicantUuidAndMatchedDate(Uuid("daily-applicant"), LocalDate.of(2026, 8, 30)))
        assertFalse(repository.existsByApplicantUuidAndMatchedDate(Uuid("other-applicant"), LocalDate.of(2026, 8, 31)))
    }

    @Test
    fun `자정 경계의 매칭은 해당 날짜에만 포함된다`() {
        repository.save(
            match(roomId = 1004, applicantUuid = "midnight-applicant")
                .copy(matchedAt = LocalDateTime.of(2026, 8, 31, 0, 0))
        )

        assertTrue(repository.existsByApplicantUuidAndMatchedDate(Uuid("midnight-applicant"), LocalDate.of(2026, 8, 31)))
        assertFalse(repository.existsByApplicantUuidAndMatchedDate(Uuid("midnight-applicant"), LocalDate.of(2026, 8, 30)))
    }

    private fun match(roomId: Long, applicantUuid: String = "applicant") = MeetingMatch(
        roomId = roomId,
        applicantUuid = Uuid(applicantUuid),
        creatorContact = "010-1111-2222",
        applicantContact = "010-3333-4444",
        matchedAt = LocalDateTime.of(2026, 8, 31, 12, 0),
    )
}

package com.yourssu.signal.domain.meeting.storage

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.meeting.implement.MeetingMatch
import com.yourssu.signal.domain.meeting.implement.MeetingMatchRepository
import com.yourssu.signal.domain.meeting.implement.MeetingRoom
import com.yourssu.signal.domain.meeting.implement.MeetingRoomRepository
import com.yourssu.signal.domain.meeting.implement.MeetingRoomStatus
import com.yourssu.signal.domain.meeting.implement.MeetingSlot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SpringBootTest
@ActiveProfiles("test")
class MeetingRepositoryConcurrencyTest {
    @Autowired lateinit var repository: MeetingRoomRepository
    @Autowired lateinit var jpaRepository: MeetingRoomJpaRepository
    @Autowired lateinit var matchRepository: MeetingMatchRepository
    @Autowired lateinit var matchJpaRepository: MeetingMatchJpaRepository
    @Autowired lateinit var transactionTemplate: TransactionTemplate

    @BeforeEach
    fun clean() {
        matchJpaRepository.deleteAll()
        jpaRepository.deleteAll()
    }

    @Test
    fun `동일 슬롯 동시 생성은 하나만 성공한다`() {
        val now = LocalDateTime.of(2026, 8, 31, 12, 0)
        val ready = CountDownLatch(2)
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(2)
        try {
            val results = (1..2).map { index ->
                executor.submit<Boolean> {
                    ready.countDown()
                    start.await()
                    runCatching {
                        transactionTemplate.executeWithoutResult {
                            repository.save(room("slot-race-$index", MeetingSlot.SLOT_7, now))
                        }
                    }.isSuccess
                }
            }
            ready.await(5, TimeUnit.SECONDS)
            start.countDown()

            assertEquals(1, results.count { it.get(10, TimeUnit.SECONDS) })
            assertEquals(1, jpaRepository.countByActiveSlot(MeetingSlot.SLOT_7))
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun `동일 생성자의 서로 다른 슬롯 동시 생성도 하나만 성공한다`() {
        val now = LocalDateTime.of(2026, 8, 31, 12, 0)
        val ready = CountDownLatch(2)
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(2)
        try {
            val slots = listOf(MeetingSlot.SLOT_8, MeetingSlot.SLOT_9)
            val results = slots.map { slot ->
                executor.submit<Boolean> {
                    ready.countDown()
                    start.await()
                    runCatching {
                        transactionTemplate.executeWithoutResult {
                            repository.save(room("creator-race", slot, now))
                        }
                    }.isSuccess
                }
            }
            ready.await(5, TimeUnit.SECONDS)
            start.countDown()

            assertEquals(1, results.count { it.get(10, TimeUnit.SECONDS) })
            assertEquals(1, jpaRepository.countByCreatorUuidAndCreationDate("creator-race", LocalDate.from(now)))
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun `동일 방 동시 매칭은 방 잠금으로 하나만 성공한다`() {
        val now = LocalDateTime.of(2026, 8, 31, 12, 0)
        val room = repository.save(room("match-creator", MeetingSlot.SLOT_10, now))
        val roomId = room.id!!
        val ready = CountDownLatch(2)
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(2)
        try {
            val results = (1..2).map { index ->
                executor.submit<Boolean> {
                    ready.countDown()
                    start.await()
                    runCatching {
                        transactionTemplate.executeWithoutResult {
                            val locked = repository.findByIdForUpdate(roomId)!!
                            val matched = locked.match(now.plusMinutes(index.toLong()))
                            matchRepository.save(
                                MeetingMatch(
                                    roomId = roomId,
                                    applicantUuid = Uuid("match-applicant-$index"),
                                    creatorContact = "010-1111-2222",
                                    applicantContact = "010-3333-444$index",
                                    matchedAt = now.plusMinutes(index.toLong()),
                                )
                            )
                            repository.save(matched)
                        }
                    }.isSuccess
                }
            }
            ready.await(5, TimeUnit.SECONDS)
            start.countDown()

            assertEquals(1, results.count { it.get(10, TimeUnit.SECONDS) })
            assertEquals(MeetingRoomStatus.MATCHED, repository.findById(roomId)!!.status)
            assertEquals(1, matchJpaRepository.countByRoomId(roomId))
        } finally {
            executor.shutdownNow()
        }
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
